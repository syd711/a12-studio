package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
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
 * Whether "Ad Hoc Testing" is offered for an {@code AdditiveDocumentModel} (see {@link
 * DocumentModelActions#startAdditiveAdHocTest} and {@link AdHocTestPreviewSessionAdditiveIdMappingTest} in
 * {@code a12-studio-ui.preview}, which pins the id-mapping logic that action feeds into) now hinges on whether
 * at least one Combination Model actually references it - not unconditionally disabled any more - built
 * against the real {@code advanced_new/PersonEmployee_Ad}/{@code PersonEmployee_Cm} fixture pair.
 */
class AdditiveAdHocTestAvailabilityTest {

  private static boolean toolkitAvailable;

  @TempDir
  Path workspace;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void restoreEmptyProject() throws Exception {
    if (toolkitAvailable) {
      setCurrentProject(new Project());
    }
  }

  @Test
  void isEnabledWhenACombinationModelReferencesTheAdditiveModel() throws Exception {
    Fixture fixture = open(List.of("Person_Dc.json", "PersonEmployee_Ad.json", "PersonEmployee_Cm.json"));

    Button button = FxTestSupport.field(fixture.controller, "adHocTestButton");
    assertFalse(button.isDisable(), "PersonEmployee_Cm references this Additive Document Model");

    DocumentModelActions actions = FxTestSupport.field(fixture.controller, "documentModelActions");
    List<MenuItem> rootMenu = FxTestSupport.onFx(() -> actions.createRootContextMenu().getItems());
    String label = StudioBundle.get("document_model_tree.ad_hoc_testing");
    assertTrue(rootMenu.stream().anyMatch(item -> !item.isDisable() && label.equals(item.getText())),
        "the context menu's Ad Hoc Testing item must be enabled too: " + rootMenu.stream().map(MenuItem::getText).toList());
  }

  @Test
  void isDisabledWhenNoCombinationModelReferencesTheAdditiveModel() throws Exception {
    Fixture fixture = open(List.of("Person_Dc.json", "PersonEmployee_Ad.json"));

    Button button = FxTestSupport.field(fixture.controller, "adHocTestButton");
    assertTrue(button.isDisable(), "no Combination Model in this project references PersonEmployee_Ad");
  }

  private record Fixture(DocumentModelElementsTreeController controller, DocumentModel model, ProjectItem item) {
  }

  private Fixture open(List<String> fileNames) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path source = locateWorkspace().resolve("models").resolve("10_People");
    Path models = Files.createDirectories(workspace.resolve("models"));
    for (String fileName : fileNames) {
      Files.copy(source.resolve(fileName), models.resolve(fileName));
    }

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);
    ProjectItem item = project.getRoot().findByPath(models.resolve("PersonEmployee_Ad.json").toString());
    if (item == null) {
      item = new ProjectItem(models.resolve("PersonEmployee_Ad.json").toFile());
    }
    DocumentModel model = (DocumentModel) item.getModel();

    FxTestSupport.Loaded<DocumentModelElementsTreeController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/documentmodel/document-model-elements-tree.fxml");
    ProjectItem openedItem = item;
    FxTestSupport.onFx(() -> loaded.controller().load(openedItem, model.getContent().getModelRoot()));
    return new Fixture(loaded.controller(), model, item);
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
