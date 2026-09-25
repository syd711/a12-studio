package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.preview.PreviewServer;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.previewapp.PreviewAppException;
import de.a12.studio.ui.previewapp.SmeInstallation;
import javafx.scene.control.Label;
import javafx.scene.web.WebView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** The Content Model editor's center: a WebView on the preview session of the edited model. */
class ContentModelEditorPreviewTest {

  @TempDir
  Path workspace;

  @AfterAll
  static void tearDown() throws Exception {
    PreviewServer.stopIfRunning();
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, new Project());
    Field validationService = Studio.class.getDeclaredField("validationService");
    validationService.setAccessible(true);
    validationService.set(null, null);
  }

  @Test
  void theEditorShowsThePreviewOfItsModelAndStopsItWhenClosed() throws Exception {
    assumeTrue(FxTestSupport.startToolkit(), "No JavaFX toolkit available");
    boolean smeAvailable;
    try {
      SmeInstallation.resolve();
      smeAvailable = true;
    }
    catch (PreviewAppException e) {
      smeAvailable = false;
    }
    assumeTrue(smeAvailable, "No Simple Model Editor in the A12 installation");

    Files.createDirectories(workspace.resolve("models"));
    Files.copy(locateFixture(), workspace.resolve("models").resolve("WelcomePage_CM.json"));
    Project project = new Project();
    project.load(workspace.toFile());
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
    Field validationService = Studio.class.getDeclaredField("validationService");
    validationService.setAccessible(true);
    validationService.set(null, new ValidationService(project));
    ProjectItem item = project.getRoot().findByPath(workspace.resolve("models").resolve("WelcomePage_CM.json").toString());

    FxTestSupport.Loaded<ContentModelEditorController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/contentmodel/content-model-editor.fxml");
    try {
      FxTestSupport.onFx(() -> loaded.controller().load(item));

      WebView webView = FxTestSupport.field(loaded.controller(), "previewWebView");
      Label unavailable = FxTestSupport.field(loaded.controller(), "previewUnavailableLabel");
      String location = FxTestSupport.onFx(() -> webView.getEngine().getLocation());

      assertNotNull(location);
      assertTrue(location.startsWith("http://localhost:"), location);
      assertTrue(location.endsWith("/sme/index.html?content=content-WelcomePage_CM"), location);
      assertTrue(webView.isVisible());
      assertFalse(unavailable.isVisible());

      FxTestSupport.onFx(() -> loaded.controller().modelClosed(new ModelClosedEvent(item)));
      String closedLocation = location;
      for (int i = 0; i < 50 && !"about:blank".equals(closedLocation); i++) {
        Thread.sleep(100);
        closedLocation = FxTestSupport.onFx(() -> webView.getEngine().getLocation());
      }
      assertEquals("about:blank", closedLocation);
    }
    finally {
      StudioEventManager.getInstance().removeListener(loaded.controller());
    }
  }

  private static Path locateFixture() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("basic").resolve("models").resolve("WelcomePage_CM.json");
      if (Files.isRegularFile(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate the WelcomePage_CM.json fixture");
  }
}
