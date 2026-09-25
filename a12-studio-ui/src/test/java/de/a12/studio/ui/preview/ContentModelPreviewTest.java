package de.a12.studio.ui.preview;

import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeBackend;
import de.a12.studio.ui.previewapp.SmeInstallation;
import javafx.scene.Scene;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Content Model preview: the session over a real project model and - the part that matters - the real Content
 * Engine of the developer's A12 installation rendering it inside a JavaFX {@code WebView}, as the Content Model editor
 * embeds it. Skipped when there is no Simple Model Editor installation (or no display for the WebView).
 */
class ContentModelPreviewTest {

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
    PreviewServer.stopIfRunning();
    SmeBackend.getInstance().stop();
    setCurrentProject(new Project());
  }

  @Test
  void aContentModelWithoutADocumentModelIsPreviewedAsIs() throws Exception {
    Project project = loadWorkspace("basic");
    ProjectItem content = project.getRoot().findByPath(workspace.resolve("models").resolve("WelcomePage_CM.json").toString());

    ContentModelPreviewSession session = new ContentModelPreviewSession(content);
    ContentModelPreviewSession.Snapshot first = session.snapshot(null, null);

    assertNotNull(first.contentModel());
    assertTrue(first.contentModel().contains("Basic Workspace"));
    assertFalse(first.hasDocumentModel());
    assertNull(first.documentModel());

    ContentModelPreviewSession.Snapshot unchanged = session.snapshot(first.contentModelRevision(), first.documentModelRevision());
    assertNull(unchanged.contentModel(), "the page has this revision already");

    ((ContentModel) content.getModel()).getContent().getRoot().setType("Grid");
    ContentModelPreviewSession.Snapshot edited = session.snapshot(first.contentModelRevision(), first.documentModelRevision());
    assertNotNull(edited.contentModel(), "an edit that was not saved is picked up");
    assertEquals(false, edited.contentModelRevision().equals(first.contentModelRevision()));
  }

  @Test
  void aContentModelBoundToADocumentModelIsPreviewedAgainstItsExpandedDocumentModel() throws Exception {
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");
    Project project = loadWorkspace("e-commerce");
    ProjectItem content = findByName(project.getRoot(), "Product_OfBundle_CM.json");

    ContentModelPreviewSession.Snapshot snapshot = new ContentModelPreviewSession(content).snapshot(null, null);

    assertTrue(snapshot.hasDocumentModel());
    assertNotNull(snapshot.documentModel());
    assertTrue(snapshot.documentModel().contains("\"Product_DM\""));
    assertTrue(snapshot.documentModel().contains("__meta"), "the input of the validation code generation keeps the metadata groups");
    assertFalse(snapshot.documentModelWithoutMetaData().contains("__meta"), "the Content Engine's model has none");
  }

  @Test
  void theRealContentEngineRendersTheModelInAWebView() throws Exception {
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");
    assumeTrue(FxTestSupport.startToolkit(), "No display for a WebView");
    Project project = loadWorkspace("basic");
    ProjectItem content = project.getRoot().findByPath(workspace.resolve("models").resolve("WelcomePage_CM.json").toString());

    assertRendered(content, "Basic Workspace");
  }

  @Test
  void aBoundContentModelIsRenderedWithItsDocumentModel() throws Exception {
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");
    assumeTrue(FxTestSupport.startToolkit(), "No display for a WebView");
    Project project = loadWorkspace("e-commerce");

    assertRendered(findByName(project.getRoot(), "Product_OfBundle_CM.json"), "Product of Bundle");
  }

  /** Loads the preview of {@code content} into a WebView and waits for {@code expectedText} to show up on the page. */
  private static void assertRendered(ProjectItem content, String expectedText) throws Exception {
    String url = PreviewLauncher.registerContentPreview(content);
    WebView[] webView = new WebView[1];
    Stage[] stage = new Stage[1];
    FxTestSupport.onFx(() -> {
      // Closing the last window would otherwise shut the toolkit down for the tests that follow.
      javafx.application.Platform.setImplicitExit(false);
      webView[0] = new WebView();
      stage[0] = new Stage();
      stage[0].setScene(new Scene(webView[0], 1000, 700));
      stage[0].show();
      webView[0].getEngine().load(url);
    });

    // The page's script runs on the JavaFX thread, so a poll can wait for as long as the page is busy.
    long started = System.currentTimeMillis();
    String text = "";
    long deadline = started + 240_000;
    while (System.currentTimeMillis() < deadline && !text.contains(expectedText)) {
      Thread.sleep(500);
      text = onFxPatiently(() -> String.valueOf(webView[0].getEngine().executeScript("document.body ? document.body.innerText : ''")));
    }
    System.out.println("RENDER TIME for " + expectedText + ": " + (System.currentTimeMillis() - started) + " ms");
    Object errors = FxTestSupport.onFx(() -> webView[0].getEngine().executeScript("String(window.__previewErrors)"));
    String diagnostics = FxTestSupport.onFx(() -> "state=" + webView[0].getEngine().getLoadWorker().getState()
        + ", exception=" + webView[0].getEngine().getLoadWorker().getException()
        + ", location=" + webView[0].getEngine().getLocation());
    FxTestSupport.onFx(() -> {
      webView[0].getEngine().load("about:blank");
      stage[0].close();
    });

    assertTrue(text.contains(expectedText), "rendered text: [" + text + "], page errors: " + errors + ", " + diagnostics);
    assertFalse(text.contains("Preview not updated"), "the page reported an error: [" + text + "]");
  }

  private static <T> T onFxPatiently(java.util.concurrent.Callable<T> action) throws Exception {
    java.util.concurrent.CompletableFuture<T> result = new java.util.concurrent.CompletableFuture<>();
    javafx.application.Platform.runLater(() -> {
      try {
        result.complete(action.call());
      }
      catch (Throwable t) {
        result.completeExceptionally(t);
      }
    });
    return result.get(240, java.util.concurrent.TimeUnit.SECONDS);
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
