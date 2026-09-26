package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Like SME, the node type dialog only asks for the Document Model (drag &amp; drop and the column mapping are edited in
 * the configuration panel below the node types list). It builds a draft carrying just that choice and never touches
 * the node it was opened for. Runs against a real project holding the "Person_DM" fixture, so the Document Model
 * choices are the ones a user is offered.
 */
class TreeNodeDialogControllerTest {

  private static final String TREE = """
      {"header": {"id": "Team_TM", "modelType": "tree", "modelVersion": "11.0.0"},
       "content": {
         "columns": [{"id": "column-1", "name": "Name", "width": 1}],
         "nodes": [
           {"id": "node-1", "documentModelRef": "Person_DM", "configuration": {"dnd": true, "showInherit": true},
            "actions": [{"type": "event", "event": "event_delete_node"}],
            "columns": [{"columnRef": "column-1", "elementRef": "field_name"}]}]}}
      """;

  private static boolean toolkitAvailable;

  // Studio's current project is static state shared with every other test; put back what was there.
  private static Project previousProject;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
    if (toolkitAvailable) {
      previousProject = Studio.getCurrentProject();
    }
  }

  @AfterAll
  static void resetProject() throws Exception {
    if (toolkitAvailable) {
      setCurrentProject(previousProject);
    }
  }

  private record Workspace(ProjectItem tree) {
    TreeModel model() {
      return (TreeModel) tree.getModel();
    }

    TreeNode node() {
      return model().getContent().getNodes().get(0);
    }
  }

  private static Workspace workspace(Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path models = Files.createDirectories(dir.resolve("models"));
    Files.copy(locateBasicModels().resolve("Person_DM.json"), models.resolve("Person_DM.json"), StandardCopyOption.REPLACE_EXISTING);
    Path treeFile = models.resolve("Team_TM.json");
    Files.writeString(treeFile, TREE);

    Project project = new Project();
    project.load(dir.toFile());
    setCurrentProject(project);
    ProjectItem tree = project.getRoot().findByPath(treeFile.toString());
    return new Workspace(tree != null ? tree : new ProjectItem(treeFile.toFile()));
  }

  private static FxTestSupport.Loaded<TreeNodeDialogController> open(Workspace workspace, TreeNode node) throws Exception {
    FxTestSupport.Loaded<TreeNodeDialogController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/dialogs/tree-node-dialog.fxml");
    FxTestSupport.onFx(() -> loaded.controller().init(new Stage(), workspace.tree(), node));
    return loaded;
  }

  private static void submit(TreeNodeDialogController controller) throws Exception {
    Button ok = FxTestSupport.field(controller, "okButton");
    FxTestSupport.onFx(ok::fire);
  }

  @Test
  void theDocumentModelChoicesAreTheProjectsOwn(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeDialogController> loaded = open(workspace, workspace.node());

    ComboBox<String> documentModel = FxTestSupport.field(loaded.controller(), "documentModelField");
    assertTrue(documentModel.getItems().contains("Person_DM"));
    assertFalse(documentModel.getItems().contains("Team_TM"), "only Document Models are offered");
    assertEquals("Person_DM", documentModel.getValue());
  }

  @Test
  void editingBuildsADraftWithJustTheDocumentModelAndLeavesTheNodeAlone(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNode node = workspace.node();
    FxTestSupport.Loaded<TreeNodeDialogController> loaded = open(workspace, node);

    submit(loaded.controller());

    Optional<TreeNode> result = loaded.controller().getResult();
    assertTrue(result.isPresent());
    TreeNode draft = result.get();
    assertNotSame(node, draft);
    assertEquals("Person_DM", draft.getDocumentModelRef());
    assertTrue(draft.getColumns().isEmpty(), "the caller copies only the Document Model, the mapping stays on the node");

    assertEquals(true, node.getConfiguration().get("dnd"));
    assertEquals(true, node.getConfiguration().get("showInherit"));
    assertEquals(1, node.getColumns().size());
    assertEquals(1, node.getActions().size());
  }

  @Test
  void addNeedsADocumentModelAndStartsWithAnEmptyConfiguration(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeDialogController> loaded = open(workspace, null);
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");
    ComboBox<String> documentModel = FxTestSupport.field(loaded.controller(), "documentModelField");
    assertTrue(ok.isDisable(), "a node type without a Document Model cannot be added");

    FxTestSupport.onFx(() -> documentModel.setValue("Person_DM"));
    assertFalse(ok.isDisable());
    submit(loaded.controller());

    TreeNode draft = loaded.controller().getResult().orElseThrow();
    assertEquals("Person_DM", draft.getDocumentModelRef());
    assertNotNull(draft.getConfiguration(), "SME requires a configuration on every node");
    assertTrue(draft.getConfiguration().isEmpty());
    assertTrue(draft.getColumns().isEmpty());
  }

  @Test
  void cancelProducesNoResult(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeDialogController> loaded = open(workspace, workspace.node());

    FxTestSupport.onFx(() -> loaded.controller().onDialogCancel());

    assertTrue(loaded.controller().getResult().isEmpty());
  }

  private static void setCurrentProject(Project project) throws Exception {
    Field currentProject = Studio.class.getDeclaredField("currentProject");
    currentProject.setAccessible(true);
    currentProject.set(null, project);
  }

  private static Path locateBasicModels() throws IOException {
    for (Path dir = Path.of("").toAbsolutePath(); dir != null; dir = dir.getParent()) {
      Path candidate = dir.resolve("testing").resolve("workspaces").resolve("basic").resolve("models");
      if (Files.isDirectory(candidate)) {
        return candidate;
      }
    }
    throw new IllegalStateException("Could not locate 'testing/workspaces/basic/models' above " + Path.of("").toAbsolutePath());
  }
}
