package de.a12.studio.ui.editors.combineddocumentmodel;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.TreeView;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Wiring test for the Combination Model editor's own FXML/controller (not the "Preview" panel's logic, which
 * {@link CombinationPreviewPanelControllerTest} already covers loading the panel's own FXML directly) - this
 * catches an {@code fx:id} mismatch or a missing {@code previewPanelController.load} call that a panel-only
 * test can't see, by driving the real editor entry point ({@link
 * de.a12.studio.ui.editors.AbstractEditorController#load}, as the real app does) end to end against the same
 * real {@code advanced_new/PersonEmployee_Cm} fixture.
 */
class CombinedDocumentModelEditorControllerTest {

  private static final String EDITOR_FXML = "/de/a12/studio/ui/editors/combineddocumentmodel/combined-document-model-editor.fxml";

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void stopValidationService() throws Exception {
    if (toolkitAvailable) {
      FxTestSupport.clearValidationService();
    }
  }

  @Test
  void openingTheEditorPopulatesThePreviewTab() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    Path source = locateWorkspace().resolve("models").resolve("10_People");
    Path models = Files.createDirectories(workspace.resolve("models"));
    for (String fileName : List.of("Person_Dc.json", "PersonEmployee_Ad.json", "PersonEmployee_Cm.json")) {
      Files.copy(source.resolve(fileName), models.resolve(fileName));
    }

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);
    FxTestSupport.setValidationServiceForProject(project);

    ProjectItem item = project.getRoot().findByPath(models.resolve("PersonEmployee_Cm.json").toString());

    FxTestSupport.Loaded<CombinedDocumentModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    FxTestSupport.onFx(() -> loaded.controller().load(item));

    CombinationPreviewPanelController previewController = FxTestSupport.field(loaded.controller(), "previewPanelController");
    TreeView<ElementViewModel> tree = FxTestSupport.field(previewController, "tree");
    assertTrue(tree.isVisible(), "the merge must have resolved from the real editor entry point");
    assertFalse(tree.getRoot().getChildren().isEmpty());
  }

  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
  }

  private static Path locateWorkspace() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("advanced_new");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IOException("Could not locate 'testing/workspaces/advanced_new' above " + Path.of("").toAbsolutePath());
  }
}
