package de.a12.studio.ui.preview;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeBackend;
import de.a12.studio.ui.previewapp.SmeInstallation;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Form Engine preview end to end: real sessions over real project models, expanded by the real Simple Model
 * Editor backend of the developer's A12 installation. Skipped when there is none.
 */
class FormEnginePreviewTest {

  private static boolean smeAvailable;

  @TempDir
  Path workspace;

  @BeforeAll
  static void checkInstallation() {
    try {
      SmeInstallation.resolve();
      smeAvailable = true;
    }
    catch (PreviewAppException e) {
      smeAvailable = false;
    }
  }

  @AfterAll
  static void tearDown() throws Exception {
    SmeBackend.getInstance().stop();
    setCurrentProject(new Project());
  }

  @Test
  void aFormModelIsPreviewedAgainstItsExpandedDocumentModel() throws Exception {
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");
    Project project = loadWorkspace("basic");
    ProjectItem form = project.getRoot().findByPath(workspace.resolve("models").resolve("Invoice_FM.json").toString());

    FormEnginePreviewSession session = new FormModelPreviewSession(form);
    FormEnginePreviewSession.Snapshot first = session.snapshot(null, null);

    assertNotNull(first.formModel());
    assertTrue(first.formModel().contains("\"Invoice_FM\""), "the Form Model itself");
    // Addresses_DM is included into Invoice_DM: only the expanded model contains its fields.
    assertNotNull(first.documentModel());
    assertTrue(first.documentModel().contains("BillingAddress"), "included fields are expanded");
    assertNotNull(first.validationCode());
    assertTrue(first.validationCode().contains("ValidationRuntime"), "generated validation code");

    FormEnginePreviewSession.Snapshot unchanged = session.snapshot(first.formModelRevision(), first.documentModelRevision());
    assertNull(unchanged.formModel());
    assertNull(unchanged.documentModel());
    assertNull(unchanged.validationCode());

    FormModel live = (FormModel) form.getModel();
    live.getContent().getScreens().get(0).setName("RenamedScreen");
    FormEnginePreviewSession.Snapshot edited = session.snapshot(first.formModelRevision(), first.documentModelRevision());
    assertNotNull(edited.formModel(), "an edit that was not saved is picked up");
    assertTrue(edited.formModel().contains("RenamedScreen"));
    assertNull(edited.documentModel(), "the Document Model did not change, so it is not sent again");
  }

  @Test
  void aFormModelBoundToACombinationModelIsPreviewed() throws Exception {
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");
    Project project = loadWorkspace("advanced_new");
    ProjectItem form = findByName(project, "PersonEmployee_Fm.json");

    FormEnginePreviewSession.Snapshot snapshot = new FormModelPreviewSession(form).snapshot(null, null);

    assertNotNull(snapshot.documentModel());
    assertTrue(snapshot.documentModel().contains("PersonEmployee_Cm"), "the expanded combination");
    assertNotNull(snapshot.validationCode());
  }

  @Test
  void adHocTestingReducesTheDocumentModelAndGeneratesAForm() throws Exception {
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");
    Project project = loadWorkspace("basic");
    ProjectItem documentModel = project.getRoot().findByPath(workspace.resolve("models").resolve("Invoice_DM.json").toString());

    // F107 is a field of the model itself, include_d7e3e the Include of Order_DM (its children only exist expanded).
    FormEnginePreviewSession.Snapshot snapshot =
        new AdHocTestPreviewSession(documentModel, Set.of("F107", "include_d7e3e")).snapshot(null, null);

    assertNotNull(snapshot.documentModel());
    Set<String> elementNames = new HashSet<>();
    collectElementNames(JsonSettings.objectMapper.readTree(snapshot.documentModel()).path("content").path("modelRoot"), elementNames);
    assertTrue(elementNames.contains("OrderingDate"), "the selected Include's fields: " + elementNames);
    assertTrue(elementNames.contains("BillingAddress"), "the selected field: " + elementNames);
    assertFalse(elementNames.contains("PaymentInfo"), "everything else is cut away: " + elementNames);
    assertFalse(elementNames.contains("Addresses"), "everything else is cut away: " + elementNames);
    assertNotNull(snapshot.formModel());
    assertTrue(snapshot.formModel().contains("\"form\""), "a generated Form Model");
    assertTrue(snapshot.formModel().contains("\"screens\""));
    assertTrue(snapshot.formModel().contains("\"subHeaderBox\"") && snapshot.formModel().contains("\"footerBox\""),
        "the Form Engine rejects a model without them: " + snapshot.formModel());
    assertNotNull(snapshot.validationCode());
  }

  @Test
  void aFreshFormModelIsPreviewedWithTheBoxesTheFormEngineRequires() throws Exception {
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");
    Project project = loadWorkspace("basic");
    ProjectItem fresh = NewModelFactory.createModel(project.getRoot(), ModelType.FORM, "Fresh_FM", "Invoice_DM", true);
    assertFalse(Files.readString(Path.of(fresh.getPath())).contains("subHeaderBox"), "a new Form Model does not store them");

    String formModel = new FormModelPreviewSession(fresh).snapshot(null, null).formModel();

    assertTrue(formModel.contains("\"subHeaderBox\"") && formModel.contains("\"footerBox\""), formModel);
    assertFalse(Files.readString(Path.of(fresh.getPath())).contains("subHeaderBox"), "the preview leaves the model itself alone");
  }

  @Test
  void theServerServesTheSmeClientAndTheSessionData() throws Exception {
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");
    Project project = loadWorkspace("basic");
    ProjectItem form = project.getRoot().findByPath(workspace.resolve("models").resolve("Invoice_FM.json").toString());

    PreviewServer server = PreviewServer.getOrStart();
    try {
      server.registerFormEngineSession("test", new FormModelPreviewSession(form));
      HttpClient client = HttpClient.newHttpClient();

      String index = get(client, server.getFormEnginePreviewUrl("test")).body();
      assertTrue(index.contains("preview-window"), "the bootstrap makes the page SME's preview window");
      assertTrue(index.contains("main."), "the client bundle is referenced");

      HttpResponse<String> data = get(client, "http://localhost:" + server.getPort() + "/fe/test/data");
      assertEquals(200, data.statusCode(), data.body());
      assertTrue(data.body().contains("validationCode"));

      assertEquals(404, get(client, "http://localhost:" + server.getPort() + "/sme/../../settings.json").statusCode());
      assertEquals(404, get(client, "http://localhost:" + server.getPort() + "/fe/unknown/data").statusCode());
    }
    finally {
      PreviewServer.stopIfRunning();
    }
  }

  private static void collectElementNames(JsonNode node, Set<String> names) {
    if (node.isObject() && node.has("name") && node.has("id")) {
      names.add(node.get("name").asString());
    }
    node.forEach(child -> collectElementNames(child, names));
  }

  private static HttpResponse<String> get(HttpClient client, String url) throws Exception {
    return client.send(HttpRequest.newBuilder(URI.create(url)).GET().build(), HttpResponse.BodyHandlers.ofString());
  }

  private Project loadWorkspace(String name) throws Exception {
    Path source = locateWorkspace(name).resolve("models");
    Path target = workspace.resolve("models");
    try (Stream<Path> files = Files.walk(source)) {
      for (Path file : (Iterable<Path>) files::iterator) {
        Path destination = target.resolve(source.relativize(file).toString());
        if (Files.isDirectory(file)) {
          Files.createDirectories(destination);
        }
        else {
          Files.copy(file, destination);
        }
      }
    }
    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);
    return project;
  }

  private static ProjectItem findByName(Project project, String fileName) {
    return findByName(project.getRoot(), fileName);
  }

  private static ProjectItem findByName(ProjectItem item, String fileName) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        ProjectItem found = findByName(child, fileName);
        if (found != null) {
          return found;
        }
      }
      return null;
    }
    return fileName.equals(item.getName()) ? item : null;
  }

  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
  }

  private static Path locateWorkspace(String name) throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve(name);
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/" + name + "' above " + Path.of("").toAbsolutePath());
  }
}
