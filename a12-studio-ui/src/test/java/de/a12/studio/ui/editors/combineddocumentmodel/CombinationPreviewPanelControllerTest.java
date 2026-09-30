package de.a12.studio.ui.editors.combineddocumentmodel;

import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The "Preview" tab of the Combination Model editor (see {@link CombinationPreviewPanelController}'s own
 * javadoc): built against the real {@code advanced_new/PersonEmployee_Cm} fixture (base model {@code
 * Person_Dc}, one Addition step contributing {@code PersonEmployee_Ad}), the same fixture {@code
 * QueryModelCombinationTargetTest} already pins the underlying merge helper against.
 */
class CombinationPreviewPanelControllerTest {

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
  void loadingAResolvableCombinationShowsTheMergedTree() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    ProjectItem item = loadFixture("PersonEmployee_Cm");
    CombinedDocumentModel model = (CombinedDocumentModel) item.getModel();

    FxTestSupport.Loaded<CombinationPreviewPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/combineddocumentmodel/combination-preview-panel.fxml");
    CombinationPreviewPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.load(model, item));

    TreeView<ElementViewModel> tree = FxTestSupport.field(controller, "tree");
    assertTrue(tree.isVisible(), "the tree must be shown once the merge resolves");

    List<String> rootGroupNames = tree.getRoot().getChildren().stream()
        .map(treeItem -> treeItem.getValue().getName())
        .toList();
    // Person_Dc's own root group ("Person [1]", with FirstName/LastName) plus PersonEmployee_Ad's own root
    // group (also named "Person", non-repeatable - so also "Person [1]" - contributing MainLocation/
    // PersonalData/...) - CombinedDocumentModelElements concatenates root groups rather than merging
    // same-named ones (see its own javadoc), so both appear.
    assertEquals(2, rootGroupNames.size(), () -> "Unexpected root groups: " + rootGroupNames);
    assertTrue(rootGroupNames.stream().allMatch("Person [1]"::equals), () -> "Unexpected root groups: " + rootGroupNames);

    assertTrue(containsDescendantNamed(tree.getRoot(), "FirstName"), "a field from the base model");
    assertTrue(containsDescendantNamed(tree.getRoot(), "MainLocation"), "a field from the Addition step's model");
  }

  @Test
  void aCombinationWithNoBaseModelShowsThePlaceholderInstead() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");

    ProjectItem item = loadFixture("PersonEmployee_Cm");
    CombinedDocumentModel model = (CombinedDocumentModel) item.getModel();
    model.getContent().setBaseModelId(null);

    FxTestSupport.Loaded<CombinationPreviewPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/combineddocumentmodel/combination-preview-panel.fxml");
    CombinationPreviewPanelController controller = loaded.controller();
    FxTestSupport.onFx(() -> controller.load(model, item));

    TreeView<ElementViewModel> tree = FxTestSupport.field(controller, "tree");
    StackPane treeContainer = FxTestSupport.field(controller, "treeContainer");
    assertFalse(tree.isVisible(), "no merge to show without a Base Model");
    assertTrue(treeContainer.getChildren().stream().anyMatch(node -> node != tree), "the placeholder must be shown instead");
  }

  private static boolean containsDescendantNamed(TreeItem<ElementViewModel> item, String name) {
    if (item.getValue() != null && name.equals(item.getValue().getName())) {
      return true;
    }
    for (TreeItem<ElementViewModel> child : item.getChildren()) {
      if (containsDescendantNamed(child, name)) {
        return true;
      }
    }
    return false;
  }

  private ProjectItem loadFixture(String combinationModelId) throws IOException, Exception {
    Path source = locateWorkspace().resolve("models").resolve("10_People");
    Path models = Files.createDirectories(workspace.resolve("models"));
    for (String fileName : List.of("Person_Dc.json", "PersonEmployee_Ad.json", combinationModelId + ".json")) {
      Files.copy(source.resolve(fileName), models.resolve(fileName));
    }

    Project project = new Project();
    project.load(workspace.toFile());
    setCurrentProject(project);

    ProjectItem item = project.getRoot().findByPath(models.resolve(combinationModelId + ".json").toString());
    if (item == null) {
      item = new ProjectItem(models.resolve(combinationModelId + ".json").toFile());
    }
    return item;
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
