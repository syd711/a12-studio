package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The node type selection in the Node Types list (a radio button per row, a click anywhere on the row selects) and
 * the configuration panels shown for the selected node type: inheritance, child relationship configurations,
 * actions, context menu, default row action. Runs against a real project holding a Document Model and one of its
 * sub types, so "has a super type node to inherit from" is decided the way it is for a user.
 */
class TreeNodeConfigurationPanelTest {

  private static final String EDITOR_FXML = "/de/a12/studio/ui/editors/treemodel/tree-model-editor.fxml";

  private static final String TREE = """
      {"header": {"id": "Team_TM", "modelType": "tree", "modelVersion": "11.0.0", "modelReferences": []},
       "content": {
         "configuration": {"rootRef": "crc-1"},
         "columns": [{"id": "column-1", "name": "Name", "width": 1}],
         "nodes": [
           {"id": "node-1", "documentModelRef": "Base_DM", "configuration": {"dnd": true},
            "childRelationshipConfigurations": [
              {"id": "crc-1", "relationshipModelRef": "BaseBase_Re", "parentRole": "Parent"},
              {"id": "crc-2", "relationshipModelRef": "BasePerson_Re", "parentRole": "Base"}],
            "actions": [{"type": "event", "event": "event_delete_node", "icon": {"name": "delete"}},
                        {"type": "insert", "position": "as_child", "documentModelRef": "Sub_DM"}],
            "contextMenu": {"groups": [
              {"name": "Change", "actions": [{"type": "event", "event": "event_copy_node"}, {"type": "event", "event": "event_cut_node"}]},
              {"name": "Add", "type": "add", "actions": [{"type": "insert", "position": "below"}]}]},
            "styles": ["s1"], "rowTitle": [{"locale": "en", "text": "Row"}]},
           {"id": "node-2", "documentModelRef": "Sub_DM", "configuration": {"inherit": {}}}]}}
      """;

  private static final String RELATIONSHIP = """
      {"header": {"id": "%ID%", "modelType": "relationship", "modelVersion": "11.0.0"},
       "content": {"linkDocumentModel": null, "entityCharacteristics": [
         {"role": "%ROLE1%", "documentModel": "Base_DM"}, {"role": "%ROLE2%", "documentModel": "%DM2%"}]}}
      """;

  private static boolean toolkitAvailable;

  // Studio's current project is static state shared with every other test; put back what was there.
  private static Project previousProject;

  private final List<AbstractPropertyEditor> panels = new ArrayList<>();

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

  @AfterEach
  void tearDown() throws Exception {
    for (AbstractPropertyEditor panel : panels) {
      panel.destroy();
    }
    FxTestSupport.onFx(() -> {
    });
    FxTestSupport.selectProjectItem(null);
  }

  private record Workspace(ProjectItem tree) {
    TreeModel model() {
      return (TreeModel) tree.getModel();
    }
  }

  /** A project with Base_DM, its sub type Sub_DM, Person_DM, two relationship models and the tree above. */
  private static Workspace workspace(Path dir) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path models = Files.createDirectories(dir.resolve("models"));
    String person = Files.readString(locateBasicModels().resolve("Person_DM.json"));
    Files.writeString(models.resolve("Person_DM.json"), person);
    Files.writeString(models.resolve("Base_DM.json"), person.replace("\"id\": \"Person_DM\"", "\"id\": \"Base_DM\""));
    Files.writeString(models.resolve("Sub_DM.json"), person.replace("\"id\": \"Person_DM\"", "\"id\": \"Sub_DM\"")
        .replaceFirst("\"annotations\": \\[", "\"annotations\": [{\"name\": \"superTypes\", \"value\": \"Base_DM\"},"));
    Files.writeString(models.resolve("BaseBase_Re.json"), RELATIONSHIP.replace("%ID%", "BaseBase_Re")
        .replace("%ROLE1%", "Parent").replace("%ROLE2%", "Child").replace("%DM2%", "Base_DM"));
    Files.writeString(models.resolve("BasePerson_Re.json"), RELATIONSHIP.replace("%ID%", "BasePerson_Re")
        .replace("%ROLE1%", "Base").replace("%ROLE2%", "Person").replace("%DM2%", "Person_DM")
        .replace("\"linkDocumentModel\": null", "\"linkDocumentModel\": \"Person_DM\""));
    Path treeFile = models.resolve("Team_TM.json");
    Files.writeString(treeFile, TREE);

    Project project = new Project();
    project.load(dir.toFile());
    setCurrentProject(project);
    ProjectItem tree = project.getRoot().findByPath(treeFile.toString());
    assertNotNull(tree, "the tree model must be part of the project");
    assertNotNull(tree.getModel(), "the fixture tree model must load");
    FxTestSupport.selectProjectItem(tree);
    return new Workspace(tree);
  }

  private static TreeModel reloaded(ProjectItem item) {
    return (TreeModel) new ProjectItem(item.getFile()).getModel();
  }

  private <T extends AbstractPropertyEditor> FxTestSupport.Loaded<T> load(String panelFxml) throws Exception {
    FxTestSupport.Loaded<T> loaded = FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/" + panelFxml);
    panels.add(loaded.controller());
    return loaded;
  }

  private FxTestSupport.Loaded<TreeNodeConfigurationPanelController> loadConfigurationPanel(Workspace workspace) throws Exception {
    FxTestSupport.Loaded<TreeNodeConfigurationPanelController> loaded =
        FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/tree-node-configuration-panel.fxml");
    for (String panel : List.of("inheritPanelController", "iconPanelController", "childRelationshipsPanelController",
        "actionsPanelController", "contextMenuPanelController", "rowActivationPanelController", "rowTitlePanelController",
        "stylesPanelController")) {
      panels.add(FxTestSupport.field(loaded.controller(), panel));
    }
    FxTestSupport.onFx(() -> loaded.controller().setModel(workspace.model(), workspace.tree()));
    return loaded;
  }

  private static <T> T field(Object owner, String name) throws Exception {
    return FxTestSupport.field(owner, name);
  }

  // ---- selection in the node types list ----

  @Test
  void everyRowStartsWithASelectionRadioAndTheFirstNodeTypeIsSelected(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeTypesPanelController> loaded = load("tree-node-types-panel.fxml");
    AtomicReference<TreeNode> reported = new AtomicReference<>();
    FxTestSupport.onFx(() -> {
      loaded.controller().setOnSelectionChange(reported::set);
      loaded.controller().setModel(workspace.model(), workspace.tree());
    });

    VBox rows = field(loaded.controller(), "nodeRows");
    assertEquals(2, rows.getChildren().size());
    RadioButton first = radio(rows, 0);
    RadioButton second = radio(rows, 1);
    assertEquals(first, ((HBox) rows.getChildren().get(0)).getChildren().get(0), "the radio is the first column");
    assertTrue(first.isSelected());
    assertFalse(second.isSelected());
    assertSame(workspace.model().getContent().getNodes().get(0), reported.get());
    assertSame(reported.get(), loaded.controller().getSelectedNode());
    assertTrue(rows.getChildren().get(0).getStyleClass().contains("module-row-selected"));
    assertFalse(rows.getChildren().get(1).getStyleClass().contains("module-row-selected"));
  }

  @Test
  void clickingARadioOrAnywhereOnARowSelectsThatNodeType(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeTypesPanelController> loaded = load("tree-node-types-panel.fxml");
    List<TreeNode> reported = new ArrayList<>();
    FxTestSupport.onFx(() -> {
      loaded.controller().setOnSelectionChange(reported::add);
      loaded.controller().setModel(workspace.model(), workspace.tree());
    });
    VBox rows = field(loaded.controller(), "nodeRows");
    List<TreeNode> nodes = workspace.model().getContent().getNodes();

    FxTestSupport.onFx(radio(rows, 1)::fire);
    assertSame(nodes.get(1), loaded.controller().getSelectedNode());
    assertFalse(radio(rows, 0).isSelected(), "a single radio group");
    assertTrue(rows.getChildren().get(1).getStyleClass().contains("module-row-selected"));
    assertFalse(rows.getChildren().get(0).getStyleClass().contains("module-row-selected"));

    // A click on the row's document model label selects the row - and only opens nothing, the dialog is the pencil.
    Label documentModel = (Label) rows.getChildren().get(0).lookup("#treeNodeDocumentModel-0");
    FxTestSupport.onFx(() -> rows.getChildren().get(0).fireEvent(click(documentModel)));
    assertSame(nodes.get(0), loaded.controller().getSelectedNode());
    assertTrue(radio(rows, 0).isSelected());
    assertEquals(List.of(nodes.get(0), nodes.get(1), nodes.get(0)), reported, "the initial pick, then one report per change");
  }

  @Test
  void theSelectionSurvivesAReorderAndFallsBackToTheFirstNodeWhenItsNodeIsGone(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeTypesPanelController> loaded = load("tree-node-types-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(workspace.model(), workspace.tree()));
    VBox rows = field(loaded.controller(), "nodeRows");
    List<TreeNode> nodes = workspace.model().getContent().getNodes();
    TreeNode second = nodes.get(1);
    FxTestSupport.onFx(radio(rows, 1)::fire);

    // Move the first row down: the selected node is now the first row, and still the selected one.
    HBox firstRow = (HBox) rows.getChildren().get(0);
    HBox actions = (HBox) firstRow.getChildren().get(firstRow.getChildren().size() - 1);
    Button moveDown = ((Button) ((VBox) actions.getChildren().get(0)).getChildren().get(1));
    FxTestSupport.onFx(moveDown::fire);

    assertSame(second, workspace.model().getContent().getNodes().get(0));
    assertSame(second, loaded.controller().getSelectedNode());
    assertTrue(radio(rows, 0).isSelected());

    // A different model (or a removed node) resets to the first node type.
    FxTestSupport.onFx(() -> {
      nodes.remove(second);
      loaded.controller().setModel(workspace.model(), workspace.tree());
    });
    assertSame(nodes.get(0), loaded.controller().getSelectedNode());
  }

  @Test
  void noNodeTypesMeansNothingIsSelected(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    workspace.model().getContent().getNodes().clear();
    FxTestSupport.Loaded<TreeNodeTypesPanelController> loaded = load("tree-node-types-panel.fxml");
    AtomicReference<TreeNode> reported = new AtomicReference<>(new TreeNode());
    FxTestSupport.onFx(() -> {
      loaded.controller().setOnSelectionChange(reported::set);
      loaded.controller().setModel(workspace.model(), workspace.tree());
    });

    assertNull(loaded.controller().getSelectedNode());
    assertNull(reported.get(), "the owner is told there is nothing to configure");
  }

  // ---- the whole editor ----

  @Test
  void selectingANodeTypeShowsItsConfigurationBelowTheList(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    for (java.lang.reflect.Field editorField : TreeModelEditorController.class.getDeclaredFields()) {
      if (AbstractPropertyEditor.class.isAssignableFrom(editorField.getType())) {
        panels.add(FxTestSupport.field(loaded.controller(), editorField.getName()));
      }
    }
    // The editor is normally handed its project item by load(ProjectItem), which also wires toolbars; only the item matters here.
    Field projectItem = de.a12.studio.ui.editors.AbstractEditorController.class.getDeclaredField("projectItem");
    projectItem.setAccessible(true);
    projectItem.set(loaded.controller(), workspace.tree());
    FxTestSupport.onFx(() -> loaded.controller().loadModel(workspace.model()));

    TreeNodeConfigurationPanelController configuration = field(loaded.controller(), "nodeConfigurationPanelController");
    TreeNodeTypesPanelController nodeTypes = field(loaded.controller(), "nodeTypesPanelController");
    Label title = field(configuration, "titleLabel");
    VBox content = field(configuration, "content");
    Label noSelection = field(configuration, "noSelectionLabel");
    assertTrue(title.getText().contains("Base_DM"), "the first node type is shown initially: " + title.getText());
    assertTrue(content.isVisible());
    assertFalse(noSelection.isVisible());

    VBox rows = field(nodeTypes, "nodeRows");
    FxTestSupport.onFx(radio(rows, 1)::fire);
    assertTrue(title.getText().contains("Sub_DM"), title.getText());
  }

  @Test
  void withoutASelectedNodeTypeThePanelAsksForOne(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeConfigurationPanelController> loaded = loadConfigurationPanel(workspace);
    VBox content = field(loaded.controller(), "content");
    Label noSelection = field(loaded.controller(), "noSelectionLabel");
    assertFalse(content.isVisible(), "nothing is selected yet");
    assertTrue(noSelection.isVisible());

    FxTestSupport.onFx(() -> loaded.controller().setNode(workspace.model().getContent().getNodes().get(0)));
    assertTrue(content.isVisible());
    assertFalse(noSelection.isVisible());

    FxTestSupport.onFx(() -> loaded.controller().setNode(null));
    assertFalse(content.isVisible());
    assertTrue(noSelection.isVisible());
  }

  // ---- what the panels show for the selected node type ----

  @Test
  void theSelectedNodeTypesActionsContextMenuAndChildRelationshipsAreListed(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeConfigurationPanelController> loaded = loadConfigurationPanel(workspace);
    FxTestSupport.onFx(() -> loaded.controller().setNode(workspace.model().getContent().getNodes().get(0)));

    TreeNodeActionsPanelController actions = field(loaded.controller(), "actionsPanelController");
    VBox actionRows = field(actions, "actionsList");
    assertEquals(2, actionRows.getChildren().size());
    assertEquals(StudioBundle.get("tree_node_action.type_event"), label(actionRows, 0, "#treeNodeActionType-0"));
    assertEquals("event_delete_node", label(actionRows, 0, "#treeNodeActionWhat-0"));
    assertEquals("delete", label(actionRows, 0, "#treeNodeActionIcon-0"));
    assertEquals(StudioBundle.get("tree_node_action.type_insert"), label(actionRows, 1, "#treeNodeActionType-1"));
    assertEquals(StudioBundle.get("tree_node_action.position_as_child") + " · Sub_DM", label(actionRows, 1, "#treeNodeActionWhat-1"));

    TreeNodeContextMenuPanelController contextMenu = field(loaded.controller(), "contextMenuPanelController");
    VBox groupRows = field(contextMenu, "groupRows");
    assertEquals(2, groupRows.getChildren().size());
    assertEquals("Change", label(groupRows, 0, "#treeContextMenuGroupName-0"));
    assertEquals("event_copy_node, event_cut_node", label(groupRows, 0, "#treeContextMenuGroupActions-0"));
    assertEquals("Add", label(groupRows, 1, "#treeContextMenuGroupName-1"));

    TreeChildRelationshipsPanelController relationships = field(loaded.controller(), "childRelationshipsPanelController");
    VBox relationshipRows = field(relationships, "relationshipRows");
    assertEquals(2, relationshipRows.getChildren().size());
    assertEquals("BaseBase_Re", label(relationshipRows, 0, "#treeChildRelationshipModel-0"));
    assertEquals("Parent", label(relationshipRows, 0, "#treeChildRelationshipParentRole-0"));
    assertEquals("BasePerson_Re", label(relationshipRows, 1, "#treeChildRelationshipModel-1"));
  }

  @Test
  void movingAnActionOrAContextMenuGroupPersistsTheNewOrder(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeConfigurationPanelController> loaded = loadConfigurationPanel(workspace);
    FxTestSupport.onFx(() -> loaded.controller().setNode(workspace.model().getContent().getNodes().get(0)));

    TreeNodeActionsPanelController actions = field(loaded.controller(), "actionsPanelController");
    VBox actionRows = field(actions, "actionsList");
    FxTestSupport.onFx(moveButtons(actionRows, 0).get(1)::fire);
    assertEquals(List.of("insert", "event"), reloaded(workspace.tree()).getContent().getNodes().get(0).getActions().stream()
        .map(action -> action.getType()).toList());
    assertEquals(StudioBundle.get("tree_node_action.type_insert"), label(actionRows, 0, "#treeNodeActionType-0"), "the rows are rebuilt in the new order");

    TreeNodeContextMenuPanelController contextMenu = field(loaded.controller(), "contextMenuPanelController");
    VBox groupRows = field(contextMenu, "groupRows");
    FxTestSupport.onFx(moveButtons(groupRows, 0).get(1)::fire);
    assertEquals(List.of("Add", "Change"), reloaded(workspace.tree()).getContent().getNodes().get(0).getContextMenu().getGroups().stream()
        .map(group -> group.getName()).toList());
  }

  @Test
  void movingAChildRelationshipSyncsTheHeaderReferencesAndTellsTheOwner(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeConfigurationPanelController> loaded = loadConfigurationPanel(workspace);
    int[] changes = {0};
    FxTestSupport.onFx(() -> {
      loaded.controller().setOnRelationshipsChange(() -> changes[0]++);
      loaded.controller().setNode(workspace.model().getContent().getNodes().get(0));
    });
    TreeChildRelationshipsPanelController relationships = field(loaded.controller(), "childRelationshipsPanelController");
    VBox relationshipRows = field(relationships, "relationshipRows");

    FxTestSupport.onFx(moveButtons(relationshipRows, 0).get(1)::fire);

    assertEquals(1, changes[0]);
    assertEquals(List.of("crc-2", "crc-1"), reloaded(workspace.tree()).getContent().getNodes().get(0).getChildRelationshipConfigurations().stream()
        .map(configuration -> configuration.getId()).toList());
    List<ModelReference> references = reloaded(workspace.tree()).getModelReferences().stream()
        .filter(reference -> ModelReference.PURPOSE_RELATIONSHIP_MODEL_FOR_TREE.equals(reference.getPurpose())).toList();
    assertEquals(List.of("BasePerson_Re", "BaseBase_Re"), references.stream().map(ModelReference::getReference).toList());
    assertEquals(List.of("RM1", "RM2"), references.stream().map(ModelReference::getAlias).toList());
  }

  // ---- inheritance ----

  @Test
  void onlyANodeTypeWithASuperTypeNodeOffersInheritance(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeConfigurationPanelController> loaded = loadConfigurationPanel(workspace);
    TreeNodeInheritPanelController inherit = field(loaded.controller(), "inheritPanelController");
    Node inheritPane = field(inherit, "root");

    FxTestSupport.onFx(() -> loaded.controller().setNode(workspace.model().getContent().getNodes().get(0)));
    assertFalse(inheritPane.isVisible(), "Base_DM is the super type, it has nothing to inherit");
    assertFalse(inheritPane.isManaged());

    FxTestSupport.onFx(() -> loaded.controller().setNode(workspace.model().getContent().getNodes().get(1)));
    assertTrue(inheritPane.isVisible(), "Sub_DM is a sub type of the Document Model of another node type");
    assertTrue(inheritPane.isManaged());
  }

  @Test
  void inheritingAPartHidesItsPanelAndIsSavedUnderTheInheritKey(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    FxTestSupport.Loaded<TreeNodeConfigurationPanelController> loaded = loadConfigurationPanel(workspace);
    FxTestSupport.onFx(() -> loaded.controller().setNode(workspace.model().getContent().getNodes().get(1)));
    TreeNodeInheritPanelController inherit = field(loaded.controller(), "inheritPanelController");
    CheckBox rowTitle = field(inherit, "rowTitleField");
    Node rowTitlePanel = field(loaded.controller(), "rowTitlePanel");
    Node rowActivationPanel = field(loaded.controller(), "rowActivationPanel");
    assertTrue(rowTitlePanel.isVisible());

    // (The node defines no row title, so there is nothing to lose and no confirmation to answer.)
    FxTestSupport.onFx(() -> rowTitle.setSelected(true));

    assertFalse(rowTitlePanel.isVisible());
    assertFalse(rowTitlePanel.isManaged());
    assertTrue(rowActivationPanel.isVisible(), "the other parts are still configured here");
    assertEquals(java.util.Map.of("rowTitle", true),
        reloaded(workspace.tree()).getContent().getNodes().get(1).getConfiguration().get("inherit"));

    FxTestSupport.onFx(() -> rowTitle.setSelected(false));

    assertTrue(rowTitlePanel.isVisible());
    assertEquals(java.util.Map.of(), reloaded(workspace.tree()).getContent().getNodes().get(1).getConfiguration().get("inherit"),
        "SME's own files keep an empty inherit object");
  }

  // ---- default row action ----

  @Test
  void aCustomRowActionIsAnObjectWithTheEventAndDefaultsToAbsent(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNode node = workspace.model().getContent().getNodes().get(0);
    FxTestSupport.Loaded<TreeNodeRowActivationPanelController> loaded = load("tree-node-row-activation-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setNode(node));
    CheckBox custom = field(loaded.controller(), "customField");
    TextField event = field(loaded.controller(), "eventField");
    assertFalse(custom.isSelected());
    assertTrue(event.isDisabled(), "no event without a custom row action");
    assertNull(node.getDefaultRowAction());

    FxTestSupport.onFx(() -> custom.setSelected(true));
    assertFalse(event.isDisabled());
    FxTestSupport.onFx(() -> event.setText("event_open"));

    assertEquals(Boolean.TRUE, reloaded(workspace.tree()).getContent().getNodes().get(0).getDefaultRowAction().getCustom());
    assertEquals("event_open", reloaded(workspace.tree()).getContent().getNodes().get(0).getDefaultRowAction().getEvent());

    FxTestSupport.onFx(() -> custom.setSelected(false));
    assertNull(reloaded(workspace.tree()).getContent().getNodes().get(0).getDefaultRowAction());
    assertFalse(Files.readString(workspace.tree().getFile().toPath()).contains("defaultRowAction"), "back to absent");
  }

  @Test
  void aLoadedCustomRowActionIsShown(@TempDir Path dir) throws Exception {
    Workspace workspace = workspace(dir);
    TreeNode node = workspace.model().getContent().getNodes().get(0);
    de.a12.studio.models.overviewmodel.RowAction rowAction = new de.a12.studio.models.overviewmodel.RowAction();
    rowAction.setCustom(true);
    rowAction.setEvent("event_open");
    node.setDefaultRowAction(rowAction);
    FxTestSupport.Loaded<TreeNodeRowActivationPanelController> loaded = load("tree-node-row-activation-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setNode(node));

    CheckBox custom = field(loaded.controller(), "customField");
    TextField event = field(loaded.controller(), "eventField");
    assertTrue(custom.isSelected());
    assertEquals("event_open", event.getText());
    assertFalse(event.isDisabled());
  }

  // ---- helpers ----

  private static RadioButton radio(VBox rows, int row) {
    return (RadioButton) ((HBox) rows.getChildren().get(row)).getChildren().get(0);
  }

  private static String label(VBox rows, int row, String selector) {
    return ((Label) rows.getChildren().get(row).lookup(selector)).getText();
  }

  /** The move-up and move-down buttons of a row: the two buttons inside the VBox of its actions box. */
  private static List<Button> moveButtons(VBox rows, int row) {
    HBox rowBox = (HBox) rows.getChildren().get(row);
    HBox actions = (HBox) rowBox.getChildren().get(rowBox.getChildren().size() - 1);
    VBox moveBox = (VBox) actions.getChildren().get(0);
    return moveBox.getChildren().stream().map(Button.class::cast).toList();
  }

  private static MouseEvent click(Node target) {
    return new MouseEvent(MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, MouseButton.PRIMARY, 1, false, false, false, false,
        true, false, false, true, false, true, null);
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
