package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.GridPane;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The node type dialog builds a draft carrying only what it edits - Document Model, drag &amp; drop and the
 * column mapping - and never touches the node it was opened for; the mapping's per-node display-mode override
 * survives the round trip. Runs against a real project holding the "Person_DM" fixture, so the Document Model
 * and field choices are the ones a user is offered.
 */
class TreeNodeDialogControllerTest {

  // %FIELD% is replaced by the id of the first field of Person_DM.
  private static final String TREE = """
      {"header": {"id": "Team_TM", "modelType": "tree", "modelVersion": "11.0.0"},
       "content": {
         "columns": [{"id": "column-1", "name": "Name", "width": 1}, {"id": "column-2", "name": "Size", "width": 1}],
         "nodes": [
           {"id": "node-1", "documentModelRef": "Person_DM", "configuration": {"dnd": true, "showInherit": true},
            "actions": [{"type": "event", "event": "event_delete_node"}],
            "columns": [{"columnRef": "column-1", "elementRef": "%FIELD%",
                         "configuration": {"attachmentDisplayMode": "preview"}}]},
           {"id": "node-2", "documentModelRef": "Person_DM", "configuration": {}}]}}
      """;

  private static boolean toolkitAvailable;

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterAll
  static void resetProject() throws Exception {
    if (toolkitAvailable) {
      setCurrentProject(new Project());
    }
  }

  /** A project with the Person_DM fixture and the tree above; {@code fieldIds} are Person_DM's field ids. */
  private record Workspace(ProjectItem tree, List<String> fieldIds) {
    TreeModel model() {
      return (TreeModel) tree.getModel();
    }
  }

  private static Workspace workspace(Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path models = Files.createDirectories(dir.resolve("models"));
    Files.copy(locateBasicModels().resolve("Person_DM.json"), models.resolve("Person_DM.json"), StandardCopyOption.REPLACE_EXISTING);

    List<String> fieldIds = new ArrayList<>();
    DocumentModel invoice = (DocumentModel) new ProjectItem(models.resolve("Person_DM.json").toFile()).getModel();
    invoice.getContent().getModelRoot().getRootGroups().forEach(group -> collectFieldIds(group, fieldIds));
    assertTrue(fieldIds.size() >= 2, "the fixture needs at least two fields");

    Path treeFile = models.resolve("Team_TM.json");
    Files.writeString(treeFile, TREE.replace("%FIELD%", fieldIds.get(0)));

    Project project = new Project();
    project.load(dir.toFile());
    setCurrentProject(project);
    ProjectItem tree = project.getRoot().findByPath(treeFile.toString());
    return new Workspace(tree != null ? tree : new ProjectItem(treeFile.toFile()), fieldIds);
  }

  private static void collectFieldIds(GroupElement group, List<String> ids) {
    for (Element child : group.getGroup().getElements()) {
      if (child instanceof FieldElement field) {
        ids.add(field.getId());
      }
      else if (child instanceof GroupElement childGroup) {
        collectFieldIds(childGroup, ids);
      }
    }
  }

  private static FxTestSupport.Loaded<TreeNodeDialogController> open(Workspace workspace, TreeNode node) throws Exception {
    FxTestSupport.Loaded<TreeNodeDialogController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/dialogs/tree-node-dialog.fxml");
    FxTestSupport.onFx(() -> loaded.controller().init(new Stage(), workspace.model(), workspace.tree(), node));
    return loaded;
  }

  private static void submit(TreeNodeDialogController controller) throws Exception {
    Button ok = FxTestSupport.field(controller, "okButton");
    FxTestSupport.onFx(ok::fire);
  }

  @SuppressWarnings("unchecked")
  private static ComboBox<String> mappingCombo(TreeNodeDialogController controller, int row) throws Exception {
    GridPane grid = FxTestSupport.field(controller, "columnMappingGrid");
    return (ComboBox<String>) grid.getChildren().stream()
        .filter(node -> node instanceof ComboBox && GridPane.getRowIndex(node) == row)
        .findFirst().orElseThrow();
  }

  @Test
  void theDocumentModelAndFieldChoicesAreTheProjectsOwn(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeDialogController> loaded = open(workspace, workspace.model().getContent().getNodes().get(0));

    ComboBox<String> documentModel = FxTestSupport.field(loaded.controller(), "documentModelField");
    assertTrue(documentModel.getItems().contains("Person_DM"));
    assertFalse(documentModel.getItems().contains("Team_TM"), "only Document Models are offered");
    assertEquals("Person_DM", documentModel.getValue());
    assertEquals(workspace.fieldIds(), mappingCombo(loaded.controller(), 0).getItems());
    assertEquals(workspace.fieldIds().get(0), mappingCombo(loaded.controller(), 0).getValue());
    assertNull(mappingCombo(loaded.controller(), 1).getValue());
  }

  @Test
  void editingBuildsADraftAndLeavesTheNodeAndItsOtherSettingsAlone(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNode node = workspace.model().getContent().getNodes().get(0);
    FxTestSupport.Loaded<TreeNodeDialogController> loaded = open(workspace, node);
    CheckBox dragDrop = FxTestSupport.field(loaded.controller(), "dragDropField");
    assertTrue(dragDrop.isSelected());

    ComboBox<String> nameMapping = mappingCombo(loaded.controller(), 0);
    ComboBox<String> sizeMapping = mappingCombo(loaded.controller(), 1);
    String otherField = workspace.fieldIds().get(1);
    FxTestSupport.onFx(() -> {
      dragDrop.setSelected(false);
      nameMapping.setValue(otherField);
      sizeMapping.setValue(workspace.fieldIds().get(0));
    });
    submit(loaded.controller());

    Optional<TreeNode> result = loaded.controller().getResult();
    assertTrue(result.isPresent());
    TreeNode draft = result.get();
    assertEquals("Person_DM", draft.getDocumentModelRef());
    assertEquals(false, draft.getConfiguration().get("dnd"));
    assertEquals(true, draft.getConfiguration().get("showInherit"), "other configuration keys are carried over");
    assertEquals(2, draft.getColumns().size());
    assertEquals(otherField, draft.getColumns().get(0).getElementRef());
    assertEquals("column-2", draft.getColumns().get(1).getColumnRef());
    assertEquals(workspace.fieldIds().get(0), draft.getColumns().get(1).getElementRef());
    assertEquals("preview", draft.getColumns().get(0).getConfiguration().getAttachmentDisplayMode(),
        "the per-node display-mode override stays with its mapping");

    // The node the dialog was opened for is untouched until the caller applies the draft.
    assertEquals(true, node.getConfiguration().get("dnd"));
    assertEquals(1, node.getColumns().size());
    TreeNodeColumn original = node.getColumns().get(0);
    assertEquals(workspace.fieldIds().get(0), original.getElementRef());
    assertNotSame(original, draft.getColumns().get(0));
    assertEquals(1, node.getActions().size());
  }

  @Test
  void anUntouchedDragDropFlagIsNotWrittenBack(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeDialogController> loaded = open(workspace, workspace.model().getContent().getNodes().get(1));

    submit(loaded.controller());

    TreeNode draft = loaded.controller().getResult().orElseThrow();
    assertFalse(draft.getConfiguration().containsKey("dnd"), "an absent key stays absent");
    assertTrue(draft.getColumns().isEmpty(), "nothing mapped, nothing invented");
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
    assertEquals(workspace.fieldIds(), mappingCombo(loaded.controller(), 0).getItems(), "picking a model lists its fields");
    submit(loaded.controller());

    TreeNode draft = loaded.controller().getResult().orElseThrow();
    assertEquals("Person_DM", draft.getDocumentModelRef());
    assertNotNull(draft.getConfiguration());
    assertTrue(draft.getConfiguration().isEmpty());
    assertTrue(draft.getColumns().isEmpty());
  }

  @Test
  void cancelProducesNoResult(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeDialogController> loaded = open(workspace, workspace.model().getContent().getNodes().get(0));

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
