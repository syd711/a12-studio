package de.a12.studio.ui.previewapp;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.models.util.MasterDetailModuleGenerator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Deploys all models of the currently open project to the locally running Preview App server
 * (see {@link PreviewAppProcess}), the same server SME's own "Deploy" action targets for local
 * testing: authenticate against the server's UAA LOCAL login endpoint ({@code user/local/login}),
 * then PUT a flat zip of the project's model JSON files (one entry per model, named
 * {@code <id>.json}, matching the model-id-equals-filename convention) to its {@code v2/models}
 * endpoint.
 *
 * <p>Every model's content is resolved as-saved-on-disk, except Application Models: for those,
 * any {@code module-masterdetail} {@link ModelReference} in the header gets expanded into a
 * generated {@link Module} (see {@link MasterDetailModuleGenerator}) appended to a copy of {@code
 * content.modules} before serializing, mirroring SME's own {@code toFileContentForUpload} step -
 * the source model on disk (and any open editor tab) is never mutated. That resolved content is
 * then run through {@link ModelConversionService} (WCF -&gt; RMC conversion) before zipping - see
 * {@link #buildConvertedModelsZip}.
 *
 * <p>Uses the Preview App's fixed local-only "admin"/"a12" account - the documented default
 * credentials for every Preview App user, with full deploy authorization (systemAdmin /
 * MODEL_MANAGE) - since the Preview App is a throwaway local testing tool, not a production
 * server, and SME itself relies on these same fixed credentials to deploy to it.
 */
@Slf4j
public class PreviewAppDeployer {

  private static final String DEPLOY_USERNAME = "admin";
  private static final String DEPLOY_PASSWORD = "a12";

  private static final String LOGIN_TOKEN_HEADER = "access_token";
  private static final String AUTHORIZATION_HEADER = "Authorization";
  private static final String AUTHORIZATION_TOKEN_TYPE = "UAABearer";

  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(30);

  private static volatile boolean deploying;

  private PreviewAppDeployer() {
  }

  public static boolean isDeploying() {
    return deploying;
  }

  /**
   * Bundles and uploads {@code project}'s models on a background thread. {@code onFinished}, if
   * given, runs on the FX thread once the deploy attempt (success or failure) has completed.
   */
  public static void deploy(Project project, Runnable onFinished) {
    if (deploying) {
      return;
    }
    deploying = true;

    Thread deployThread = new Thread(() -> doDeploy(project, onFinished), "Preview App Deploy");
    deployThread.setDaemon(true);
    deployThread.start();
  }

  private static void doDeploy(Project project, Runnable onFinished) {
    try {
      Set<String> excludedPaths = Set.copyOf(
          project.getSettings().getProjectRootSettings().getGeneral().getDeploymentExclusions());

      List<ProjectItem> modelItems = new ArrayList<>();
      collectModelItems(project.getRoot(), excludedPaths, modelItems);
      if (modelItems.isEmpty()) {
        showAlert(StudioBundle.get("deploy_models_no_models"));
        return;
      }

      byte[] zip = buildConvertedModelsZip(modelItems);

      String apiBase = "http://localhost:" + PreviewAppInstallation.SERVER_PORT + "/api";
      HttpClient httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();

      String token = login(httpClient, apiBase);
      uploadModels(httpClient, apiBase, token, zip);
    }
    catch (Exception e) {
      log.error("Failed to deploy models to the Preview App server: {}", e.getMessage(), e);
      showAlert(StudioBundle.get("deploy_models_failed"), e.getMessage());
    }
    finally {
      deploying = false;
      if (onFinished != null) {
        Platform.runLater(onFinished);
      }
    }
  }

  private static void collectModelItems(ProjectItem item, Set<String> excludedPaths, List<ProjectItem> out) {
    if (excludedPaths.contains(item.getPath())) {
      return;
    }
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectModelItems(child, excludedPaths, out);
      }
    }
    else if (item.getModel() != null) {
      out.add(item);
    }
  }

  // Package-private (rather than private) so PreviewAppDeployerTest can exercise it directly against a
  // hand-built list of ProjectItems, without needing a full Project/ProjectSettings fixture.
  static byte[] buildModelsZip(List<ProjectItem> modelItems) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zipOut = new ZipOutputStream(bytes)) {
      for (ProjectItem item : modelItems) {
        zipOut.putNextEntry(new ZipEntry(item.getFile().getName()));
        zipOut.write(modelFileContent(item, modelItems));
        zipOut.closeEntry();
      }
    }
    return bytes.toByteArray();
  }

  /**
   * Like {@link #buildModelsZip(List)}, but every model is first run through {@link
   * ModelConversionService} (WCF -&gt; RMC conversion, injecting the {@code __meta} metadata group)
   * before zipping - the real Preview App server rejects raw designer-time model JSON when
   * computing documents against it (see {@link ModelConversionService}'s class doc). Each item's
   * content is first resolved via {@link #modelFileContent} (so the existing Application-Model /
   * Master-Detail expansion still applies) and written to a scratch staging directory, since
   * WcfCli converts a whole directory in one pass rather than individual files.
   */
  private static byte[] buildConvertedModelsZip(List<ProjectItem> modelItems) throws IOException, PreviewAppException {
    File wcfCliDir = WcfCliInstallation.resolve();
    File javaExecutable = PreviewAppInstallation.resolve().getJavaExecutable();

    File stagingDir = Files.createTempDirectory("a12-studio-deploy-staging-").toFile();
    File outputDir = Files.createTempDirectory("a12-studio-deploy-converted-").toFile();
    try {
      for (ProjectItem item : modelItems) {
        Files.write(new File(stagingDir, item.getFile().getName()).toPath(), modelFileContent(item, modelItems));
      }

      File convertedModelsDir = ModelConversionService.convert(
          javaExecutable, wcfCliDir, stagingDir, outputDir, line -> PreviewAppProcess.getInstance().appendLog(line));

      return zipConvertedModels(convertedModelsDir, modelItems);
    }
    finally {
      FileUtils.deleteDirectory(stagingDir);
      FileUtils.deleteDirectory(outputDir);
    }
  }

  private static byte[] zipConvertedModels(File convertedModelsDir, List<ProjectItem> modelItems) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zipOut = new ZipOutputStream(bytes)) {
      for (ProjectItem item : modelItems) {
        String fileName = item.getFile().getName();
        File convertedFile = new File(convertedModelsDir, fileName);
        if (!convertedFile.isFile()) {
          throw new IOException("Model conversion did not produce an output file for \"" + fileName + "\".");
        }
        zipOut.putNextEntry(new ZipEntry(fileName));
        zipOut.write(Files.readAllBytes(convertedFile.toPath()));
        zipOut.closeEntry();
      }
    }
    return bytes.toByteArray();
  }

  /**
   * The bytes to upload for {@code item}: the file as saved on disk, except for an Application Model that
   * references one or more Master-Detail Module Models, which gets its generated modules appended first (see
   * {@link MasterDetailModuleGenerator}) and is re-serialized instead of read verbatim.
   */
  private static byte[] modelFileContent(ProjectItem item, List<ProjectItem> modelItems) throws IOException {
    if (item.getModel() instanceof ApplicationModel applicationModel) {
      List<ModelReference> masterDetailReferences = applicationModel.getModelReferences().stream()
          .filter(reference -> reference.getModelType() == ModelType.MASTERDETAIL)
          .toList();
      if (!masterDetailReferences.isEmpty()) {
        return JsonSettings.objectMapper.writeValueAsBytes(
            withGeneratedModules(applicationModel, masterDetailReferences, modelItems));
      }
    }
    return Files.readAllBytes(item.getFile().toPath());
  }

  /**
   * A deep copy of {@code applicationModel} with a generated {@link Module} appended for each of {@code
   * masterDetailReferences}, in order. Operates on a copy (round-tripped through JSON) so the model instance
   * backing an open editor tab is never mutated by a deploy.
   */
  private static ApplicationModel withGeneratedModules(ApplicationModel applicationModel,
      List<ModelReference> masterDetailReferences, List<ProjectItem> modelItems) throws IOException {
    ApplicationModel deployModel = JsonSettings.objectMapper.readValue(
        JsonSettings.objectMapper.writeValueAsBytes(applicationModel), ApplicationModel.class);
    for (ModelReference reference : masterDetailReferences) {
      MasterDetailModel masterDetailModel = findMasterDetailModel(reference.getReference(), modelItems);
      deployModel.getContent().getModules()
          .add(MasterDetailModuleGenerator.createModule(reference.getReference(), masterDetailModel));
    }
    return deployModel;
  }

  private static MasterDetailModel findMasterDetailModel(String id, List<ProjectItem> modelItems) throws IOException {
    return modelItems.stream()
        .map(ProjectItem::getModel)
        .filter(candidate -> candidate instanceof MasterDetailModel && id.equals(candidate.getId()))
        .map(MasterDetailModel.class::cast)
        .findFirst()
        .orElseThrow(() -> new IOException(
            "Application Model references unknown Master-Detail Module Model \"" + id + "\"."));
  }

  private static String login(HttpClient httpClient, String apiBase) throws IOException, InterruptedException {
    String body = "{\"username\":\"" + DEPLOY_USERNAME + "\",\"password\":\"" + DEPLOY_PASSWORD + "\"}";
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(apiBase + "/user/local/login"))
        .timeout(REQUEST_TIMEOUT)
        .header("Content-Type", "application/json")
        .POST(HttpRequest.BodyPublishers.ofString(body, StandardCharsets.UTF_8))
        .build();

    HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
    if (response.statusCode() != 200) {
      throw new IOException("Login to the Preview App server failed with status " + response.statusCode() + ".");
    }

    return response.headers().firstValue(LOGIN_TOKEN_HEADER)
        .orElseThrow(() -> new IOException(
            "Preview App server login response did not contain an \"" + LOGIN_TOKEN_HEADER + "\" header."));
  }

  private static void uploadModels(HttpClient httpClient, String apiBase, String token, byte[] zip)
      throws IOException, InterruptedException {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create(apiBase + "/v2/models"))
        .timeout(REQUEST_TIMEOUT)
        .header("Content-Type", "application/zip")
        .header(AUTHORIZATION_HEADER, AUTHORIZATION_TOKEN_TYPE + " " + token)
        .PUT(HttpRequest.BodyPublishers.ofByteArray(zip))
        .build();

    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() / 100 != 2) {
      throw new IOException("Deploying models failed with status " + response.statusCode() + ": " + response.body());
    }
  }

  private static void showAlert(String message) {
    Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, message));
  }

  private static void showAlert(String message, String detail) {
    Platform.runLater(() -> WidgetFactory.showAlert(Studio.stage, message, detail));
  }
}
