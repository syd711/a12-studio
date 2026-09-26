package de.a12.studio.ui.editors.treemodel.dialogs;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeAction;
import de.a12.studio.models.treemodel.TreeNodeActionGroup;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The dialogs behind the node type configuration panels - one action ({@link TreeNodeActionDialogController}), one
 * child relationship configuration ({@link TreeChildRelationshipDialogController}) and one context-menu group
 * ({@link TreeNodeContextMenuGroupDialogController}) - against real FXML and a real project, so the Document Model,
 * relationship and field choices are the ones a user is offered.
 */
class TreeConfigurationDialogsTest {

  private static final String TREE = """
      {"header": {"id": "Team_TM", "modelType": "tree", "modelVersion": "11.0.0"},
       "content": {
         "columns": [{"id": "column-1", "name": "Name", "width": 1}, {"id": "column-2", "name": "Size", "width": 1}],
         "nodes": [{"id": "node-1", "documentModelRef": "Base_DM", "configuration": {}}]}}
      """;

  private static final String RELATIONSHIP = """
      {"header": {"id": "%ID%", "modelType": "relationship", "modelVersion": "11.0.0"},
       "content": {"linkDocumentModel": %LINK%, "entityCharacteristics": [
         {"role": "%ROLE1%", "documentModel": "%DM1%"}, {"role": "%ROLE2%", "documentModel": "%DM2%"}]}}
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

  private record Workspace(ProjectItem tree, List<String> personFieldIds) {
    TreeModel model() {
      return (TreeModel) tree.getModel();
    }

    TreeNode node() {
      return model().getContent().getNodes().get(0);
    }
  }

  /**
   * A project with Base_DM, Person_DM, Other_DM, a relationship Base-Person whose link Document Model is Person_DM,
   * a relationship Base-Base without one, a relationship Person-Other that does not involve Base_DM, and the tree.
   */
  private static Workspace workspace(Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path models = Files.createDirectories(dir.resolve("models"));
    String person = Files.readString(locateBasicModels().resolve("Person_DM.json"));
    Files.writeString(models.resolve("Person_DM.json"), person);
    Files.writeString(models.resolve("Base_DM.json"), person.replace("\"id\": \"Person_DM\"", "\"id\": \"Base_DM\""));
    Files.writeString(models.resolve("Other_DM.json"), person.replace("\"id\": \"Person_DM\"", "\"id\": \"Other_DM\""));
    writeRelationship(models, "BasePerson_Re", "\"Person_DM\"", "Base", "Base_DM", "Person", "Person_DM");
    writeRelationship(models, "BaseBase_Re", "null", "Parent", "Base_DM", "Child", "Base_DM");
    writeRelationship(models, "PersonOther_Re", "null", "Person", "Person_DM", "Other", "Other_DM");
    Path treeFile = models.resolve("Team_TM.json");
    Files.writeString(treeFile, TREE);

    Project project = new Project();
    project.load(dir.toFile());
    setCurrentProject(project);
    ProjectItem tree = project.getRoot().findByPath(treeFile.toString());
    assertNotNull(tree, "the tree model must be part of the project");

    List<String> fieldIds = new ArrayList<>();
    ((de.a12.studio.models.documentmodel.DocumentModel) new ProjectItem(models.resolve("Person_DM.json").toFile()).getModel())
        .getContent().getModelRoot().getRootGroups().forEach(group -> collectFieldIds(group, fieldIds));
    assertTrue(fieldIds.size() >= 2, "the fixture needs at least two fields");
    return new Workspace(tree, fieldIds);
  }

  private static void writeRelationship(Path models, String id, String link, String role1, String dm1, String role2, String dm2) throws IOException {
    Files.writeString(models.resolve(id + ".json"), RELATIONSHIP.replace("%ID%", id).replace("%LINK%", link)
        .replace("%ROLE1%", role1).replace("%DM1%", dm1).replace("%ROLE2%", role2).replace("%DM2%", dm2));
  }

  private static void collectFieldIds(de.a12.studio.models.documentmodel.GroupElement group, List<String> ids) {
    for (de.a12.studio.models.documentmodel.Element child : group.getGroup().getElements()) {
      if (child instanceof de.a12.studio.models.documentmodel.FieldElement field) {
        ids.add(field.getId());
      }
      else if (child instanceof de.a12.studio.models.documentmodel.GroupElement childGroup) {
        collectFieldIds(childGroup, ids);
      }
    }
  }

  private static void submit(Object controller) throws Exception {
    Button ok = FxTestSupport.field(controller, "okButton");
    FxTestSupport.onFx(ok::fire);
  }

  // ---- action ----

  private static FxTestSupport.Loaded<TreeNodeActionDialogController> openAction(Workspace workspace, TreeNodeAction action, boolean insertOnly) throws Exception {
    FxTestSupport.Loaded<TreeNodeActionDialogController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/dialogs/tree-node-action-dialog.fxml");
    FxTestSupport.onFx(() -> loaded.controller().init(new Stage(), workspace.tree(), action, insertOnly));
    return loaded;
  }

  @Test
  void aNewActionIsAnEventActionThatNeedsItsEvent(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNodeAction action = new TreeNodeAction();
    FxTestSupport.Loaded<TreeNodeActionDialogController> loaded = openAction(workspace, action, false);
    ComboBox<String> type = FxTestSupport.field(loaded.controller(), "typeField");
    ComboBox<String> event = FxTestSupport.field(loaded.controller(), "eventField");
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");
    VBox eventBox = FxTestSupport.field(loaded.controller(), "eventBox");
    VBox insertBox = FxTestSupport.field(loaded.controller(), "insertBox");

    assertEquals("event", type.getValue());
    assertEquals("event", action.getType());
    assertTrue(eventBox.isVisible());
    assertFalse(insertBox.isVisible());
    assertFalse(insertBox.isManaged());
    assertTrue(ok.isDisable(), "an event action without an event cannot be confirmed");

    FxTestSupport.onFx(() -> event.getEditor().setText("event_delete_node"));
    assertFalse(ok.isDisable());
    submit(loaded.controller());

    assertTrue(loaded.controller().isConfirmed());
    assertEquals("event_delete_node", loaded.controller().getAction().getEvent());
    assertNull(action.getPosition(), "insert-only settings are not invented for an event action");
  }

  @Test
  void switchingToInsertDropsTheEventAndOffersPositionAndDocumentModel(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNodeAction action = new TreeNodeAction();
    action.setType("event");
    action.setEvent("event_delete_node");
    FxTestSupport.Loaded<TreeNodeActionDialogController> loaded = openAction(workspace, action, false);
    ComboBox<String> type = FxTestSupport.field(loaded.controller(), "typeField");
    ComboBox<String> position = FxTestSupport.field(loaded.controller(), "positionField");
    ComboBox<String> documentModel = FxTestSupport.field(loaded.controller(), "documentModelField");
    CheckBox useLabel = FxTestSupport.field(loaded.controller(), "useLabelFromDocumentModelField");
    CheckBox useGlobalIcon = FxTestSupport.field(loaded.controller(), "useGlobalIconField");
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");
    VBox insertBox = FxTestSupport.field(loaded.controller(), "insertBox");

    FxTestSupport.onFx(() -> type.setValue("insert"));

    assertEquals("insert", action.getType());
    assertNull(action.getEvent(), "an insert action has no event");
    assertEquals("as_child", action.getPosition());
    assertEquals(List.of("as_child", "above", "below"), position.getItems());
    assertTrue(insertBox.isVisible());
    assertFalse(ok.isDisable(), "an insert action needs no event");
    assertTrue(documentModel.getItems().contains(null), "the default Document Model is choosable");
    assertTrue(documentModel.getItems().containsAll(List.of("Base_DM", "Person_DM", "Other_DM")));
    assertFalse(useLabel.isVisible(), "nothing to take the label from before a Document Model is picked");

    FxTestSupport.onFx(() -> {
      position.setValue("below");
      documentModel.setValue("Person_DM");
    });
    assertTrue(useLabel.isVisible());
    FxTestSupport.onFx(() -> useGlobalIcon.setSelected(true));

    assertEquals("below", action.getPosition());
    assertEquals("Person_DM", action.getDocumentModelRef());
    assertEquals(Boolean.TRUE, action.getUseGlobalIcon());
    FxTestSupport.onFx(() -> useGlobalIcon.setSelected(false));
    assertNull(action.getUseGlobalIcon(), "written as true or omitted");

    // Back to an event action: everything that only an insert action uses is dropped again.
    FxTestSupport.onFx(() -> useLabel.setSelected(true));
    FxTestSupport.onFx(() -> type.setValue("event"));
    assertNull(action.getPosition());
    assertNull(action.getDocumentModelRef());
    assertNull(action.getUseLabelFromDocumentModel());
    assertTrue(ok.isDisable(), "the event is required again");
  }

  @Test
  void anAddGroupsActionsCanOnlyBeInsertActions(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNodeAction action = new TreeNodeAction();
    FxTestSupport.Loaded<TreeNodeActionDialogController> loaded = openAction(workspace, action, true);
    ComboBox<String> type = FxTestSupport.field(loaded.controller(), "typeField");

    assertEquals("insert", type.getValue());
    assertTrue(type.isDisable(), "the type is fixed");
    assertEquals("insert", action.getType());
    assertEquals("as_child", action.getPosition());
  }

  @Test
  void anExistingInsertActionShowsItsValuesAndCancelProducesNoResult(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNodeAction action = new TreeNodeAction();
    action.setType("insert");
    action.setPosition("above");
    action.setDocumentModelRef("Other_DM");
    action.setUseTitleFromDocumentModel(true);
    FxTestSupport.Loaded<TreeNodeActionDialogController> loaded = openAction(workspace, action, false);

    ComboBox<String> position = FxTestSupport.field(loaded.controller(), "positionField");
    ComboBox<String> documentModel = FxTestSupport.field(loaded.controller(), "documentModelField");
    CheckBox useTitle = FxTestSupport.field(loaded.controller(), "useTitleFromDocumentModelField");
    assertEquals("above", position.getValue());
    assertEquals("Other_DM", documentModel.getValue());
    assertTrue(useTitle.isSelected());
    assertTrue(useTitle.isVisible());

    FxTestSupport.onFx(() -> loaded.controller().onDialogCancel());
    assertFalse(loaded.controller().isConfirmed());
  }

  // ---- child relationship ----

  private static FxTestSupport.Loaded<TreeChildRelationshipDialogController> openRelationship(Workspace workspace,
      TreeChildRelationshipConfiguration configuration) throws Exception {
    FxTestSupport.Loaded<TreeChildRelationshipDialogController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/dialogs/tree-child-relationship-dialog.fxml");
    FxTestSupport.onFx(() -> loaded.controller().init(new Stage(), workspace.model(), workspace.tree(), workspace.node(), configuration));
    return loaded;
  }

  @Test
  void onlyRelationshipsConnectedToTheNodesDocumentModelAreOffered(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeChildRelationshipDialogController> loaded = openRelationship(workspace, new TreeChildRelationshipConfiguration());
    ComboBox<String> relationship = FxTestSupport.field(loaded.controller(), "relationshipModelField");
    ComboBox<String> parentRole = FxTestSupport.field(loaded.controller(), "parentRoleField");
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");

    assertEquals(List.of("BaseBase_Re", "BasePerson_Re"), relationship.getItems(), "PersonOther_Re does not involve Base_DM");
    assertTrue(ok.isDisable(), "a relationship and a parent role are required");

    FxTestSupport.onFx(() -> relationship.setValue("BasePerson_Re"));
    assertEquals(List.of("Base", "Person"), parentRole.getItems());
    assertEquals("Base", parentRole.getValue(), "the role the node's own Document Model plays is preselected");
    assertFalse(ok.isDisable());
  }

  @Test
  void aRelationshipWithALinkDocumentModelOffersTheColumnMappingOfItsFields(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeChildRelationshipConfiguration configuration = new TreeChildRelationshipConfiguration();
    configuration.setId("crc-1");
    FxTestSupport.Loaded<TreeChildRelationshipDialogController> loaded = openRelationship(workspace, configuration);
    ComboBox<String> relationship = FxTestSupport.field(loaded.controller(), "relationshipModelField");
    VBox mappingBox = FxTestSupport.field(loaded.controller(), "columnMappingBox");
    GridPane grid = FxTestSupport.field(loaded.controller(), "columnMappingGrid");

    FxTestSupport.onFx(() -> relationship.setValue("BaseBase_Re"));
    assertFalse(mappingBox.isVisible(), "no link Document Model, nothing to map");

    FxTestSupport.onFx(() -> relationship.setValue("BasePerson_Re"));
    assertTrue(mappingBox.isVisible());
    @SuppressWarnings("unchecked")
    ComboBox<String> firstColumn = (ComboBox<String>) grid.getChildren().stream()
        .filter(node -> node instanceof ComboBox && GridPane.getRowIndex(node) == 0).findFirst().orElseThrow();
    assertEquals(workspace.personFieldIds(), firstColumn.getItems(), "the link Document Model's fields");
    FxTestSupport.onFx(() -> firstColumn.setValue(workspace.personFieldIds().get(1)));
    submit(loaded.controller());

    TreeChildRelationshipConfiguration result = loaded.controller().getResult().orElseThrow();
    assertEquals("crc-1", result.getId());
    assertEquals("BasePerson_Re", result.getRelationshipModelRef());
    assertEquals("Base", result.getParentRole());
    assertEquals(1, result.getColumns().size(), "unmapped columns are not written");
    assertEquals("column-1", result.getColumns().get(0).getColumnRef());
    assertEquals(workspace.personFieldIds().get(1), result.getColumns().get(0).getElementRef());
  }

  @Test
  void aConfigurationWithoutColumnsKeepsHavingNone(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeChildRelationshipConfiguration configuration = new TreeChildRelationshipConfiguration();
    configuration.setId("crc-1");
    configuration.setRelationshipModelRef("BasePerson_Re");
    configuration.setParentRole("Base");
    FxTestSupport.Loaded<TreeChildRelationshipDialogController> loaded = openRelationship(workspace, configuration);

    submit(loaded.controller());

    assertNull(loaded.controller().getResult().orElseThrow().getColumns(), "an absent key stays absent");
  }

  @Test
  void anUnresolvedRelationshipStaysSelectableInsteadOfBeingDropped(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeChildRelationshipConfiguration configuration = new TreeChildRelationshipConfiguration();
    configuration.setRelationshipModelRef("Gone_Re");
    configuration.setParentRole("Base");
    FxTestSupport.Loaded<TreeChildRelationshipDialogController> loaded = openRelationship(workspace, configuration);
    ComboBox<String> relationship = FxTestSupport.field(loaded.controller(), "relationshipModelField");
    ComboBox<String> parentRole = FxTestSupport.field(loaded.controller(), "parentRoleField");

    assertEquals("Gone_Re", relationship.getValue());
    assertEquals("Base", parentRole.getValue());
    assertTrue(relationship.getItems().contains("Gone_Re"));
  }

  // ---- context-menu group ----

  private static FxTestSupport.Loaded<TreeNodeContextMenuGroupDialogController> openGroup(Workspace workspace, TreeNodeActionGroup group) throws Exception {
    FxTestSupport.Loaded<TreeNodeContextMenuGroupDialogController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/dialogs/tree-node-context-menu-group-dialog.fxml");
    FxTestSupport.onFx(() -> loaded.controller().init(new Stage(), workspace.tree(), group));
    return loaded;
  }

  @Test
  void aGroupNeedsANameAndCanBeMadeAnAddGroup(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNodeActionGroup group = new TreeNodeActionGroup();
    FxTestSupport.Loaded<TreeNodeContextMenuGroupDialogController> loaded = openGroup(workspace, group);
    TextField name = FxTestSupport.field(loaded.controller(), "nameField");
    ComboBox<String> type = FxTestSupport.field(loaded.controller(), "typeField");
    Button ok = FxTestSupport.field(loaded.controller(), "okButton");

    assertTrue(ok.isDisable(), "a group without a name cannot be confirmed");
    assertEquals("", type.getValue(), "a plain group has no type");

    FxTestSupport.onFx(() -> {
      name.setText("Add_Node");
      type.setValue("add");
    });
    assertFalse(ok.isDisable());
    submit(loaded.controller());

    assertTrue(loaded.controller().isConfirmed());
    assertEquals("Add_Node", loaded.controller().getGroup().getName());
    assertEquals("add", loaded.controller().getGroup().getType());
    FxTestSupport.onFx(() -> type.setValue(""));
    assertNull(group.getType(), "a plain group is written without a type");
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
