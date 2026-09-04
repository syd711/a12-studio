package de.a12.studio.ui.previewapp;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;

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

      List<File> modelFiles = new ArrayList<>();
      collectModelFiles(project.getRoot(), excludedPaths, modelFiles);
      if (modelFiles.isEmpty()) {
        showAlert(StudioBundle.get("deploy_models_no_models"));
        return;
      }

      byte[] zip = buildModelsZip(modelFiles);

      String apiBase = "http://localhost:" + PreviewAppInstallation.SERVER_PORT + "/api";
      HttpClient httpClient = HttpClient.newBuilder().connectTimeout(REQUEST_TIMEOUT).build();

      String token = login(httpClient, apiBase);
      uploadModels(httpClient, apiBase, token, zip);

      showAlert(StudioBundle.get("deploy_models_success"));
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

  private static void collectModelFiles(ProjectItem item, Set<String> excludedPaths, List<File> out) {
    if (excludedPaths.contains(item.getPath())) {
      return;
    }
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectModelFiles(child, excludedPaths, out);
      }
    }
    else if (item.getModel() != null) {
      out.add(item.getFile());
    }
  }

  private static byte[] buildModelsZip(List<File> modelFiles) throws IOException {
    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
    try (ZipOutputStream zipOut = new ZipOutputStream(bytes)) {
      for (File modelFile : modelFiles) {
        zipOut.putNextEntry(new ZipEntry(modelFile.getName()));
        zipOut.write(Files.readAllBytes(modelFile.toPath()));
        zipOut.closeEntry();
      }
    }
    return bytes.toByteArray();
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
