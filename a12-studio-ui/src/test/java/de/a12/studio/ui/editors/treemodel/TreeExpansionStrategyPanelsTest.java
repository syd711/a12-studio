package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.InitialExpansion;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.lang.reflect.Method;
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
 * The Configuration tab's expansion strategy panels: Initial Expansion and Pagination ("Level by level"), Expansion
 * Depths ("Tree"), Expand/Collapse The Whole Tree, and what switching the strategy drops.
 */
class TreeExpansionStrategyPanelsTest {

  private static final String EDITOR_FXML = "/de/a12/studio/ui/editors/treemodel/tree-model-editor.fxml";

  // Shaped like advanced_new/70_Countries/Country_Tr.json: level by level with an initial expansion, page size, whole tree.
  private static final String TREE = """
      {"header": {"id": "Team_TM", "modelType": "tree", "modelVersion": "11.0.0", "modelReferences": []},
       "content": {
         "configuration": {"wholeTreeExpansion": true,
           "expansionStrategy": {"initialExpansion": {"type": "all_levels", "affectedNodeRefs": ["node-1"]}, "type": "level_by_level", "pageSize": 5}},
         "columns": [{"id": "column-1", "name": "Name", "width": 1}],
         "nodes": [{"id": "node-1", "documentModelRef": "Team_DM", "configuration": {}},
                   {"id": "node-2", "documentModelRef": "Person_DM", "configuration": {}}]}}
      """;

  private static boolean toolkitAvailable;

  private final List<AbstractPropertyEditor> panels = new ArrayList<>();

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
    if (toolkitAvailable) {
      // TreeColumnsPanelController.refreshValidationError() calls Studio.getValidationService(); its
      // ProjectModels walk needs a real (even empty) folder - an unloaded Project's root has no File and NPEs.
      de.a12.studio.models.projects.Project validationProject = new de.a12.studio.models.projects.Project();
      validationProject.load(java.nio.file.Files.createTempDirectory("tree-validation").toFile());
      FxTestSupport.setValidationServiceForProject(validationProject);
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

  @AfterAll
  static void stopValidationService() throws Exception {
    if (toolkitAvailable) {
      FxTestSupport.clearValidationService();
    }
  }

  private record Editor(ProjectItem item, FxTestSupport.Loaded<TreeModelEditorController> loaded) {
    <T> T panel(String name) throws Exception {
      return FxTestSupport.field(loaded.controller(), name);
    }

    <T> T field(String panel, String name) throws Exception {
      return FxTestSupport.field(panel(panel), name);
    }

    ExpansionStrategy saved() {
      return ((TreeModel) new ProjectItem(item.getFile()).getModel()).getContent().getConfiguration().getExpansionStrategy();
    }

    TreeModel savedModel() {
      return (TreeModel) new ProjectItem(item.getFile()).getModel();
    }
  }

  private Editor open(Path dir, String json) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    Path file = dir.resolve("Team_TM.json");
    Files.writeString(file, json);
    ProjectItem item = new ProjectItem(file.toFile());
    assertNotNull(item.getModel(), "the fixture tree model must load");
    FxTestSupport.selectProjectItem(item);
    FxTestSupport.Loaded<TreeModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    for (java.lang.reflect.Field field : TreeModelEditorController.class.getDeclaredFields()) {
      if (AbstractPropertyEditor.class.isAssignableFrom(field.getType())) {
        panels.add(FxTestSupport.field(loaded.controller(), field.getName()));
      }
    }
    FxTestSupport.onFx(() -> loaded.controller().loadModel(item.getModel()));
    return new Editor(item, loaded);
  }

  private static boolean shown(Object panel) throws Exception {
    TitledPane root = FxTestSupport.field(panel, "root");
    return root.isVisible() && root.isManaged();
  }

  private static boolean shownNode(javafx.scene.Node node) {
    return node.isVisible() && node.isManaged();
  }

  // ---- reading a file ----

  @Test
  void aLevelByLevelTreeShowsItsInitialExpansionPageSizeAndWholeTreeFlag(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE);

    assertTrue(shown(editor.panel("initialExpansionPanelController")));
    assertTrue(shown(editor.panel("paginationPanelController")));
    assertFalse(shown(editor.panel("expansionDepthsPanelController")));

    CheckBox enable = editor.field("initialExpansionPanelController", "enableField");
    ComboBox<String> type = editor.field("initialExpansionPanelController", "typeField");
    VBox levelsBox = editor.field("initialExpansionPanelController", "levelsBox");
    VBox rows = editor.field("initialExpansionPanelController", "nodeRefRows");
    assertTrue(enable.isSelected());
    assertEquals("all_levels", type.getValue());
    assertEquals(StudioBundle.get("tree_initial_expansion_panel.type_all_levels"), type.getConverter().toString("all_levels"));
    assertFalse(shownNode(levelsBox), "the number of levels only applies to level_limit");
    assertEquals(1, rows.getChildren().size());
    @SuppressWarnings("unchecked")
    ComboBox<String> nodeType = (ComboBox<String>) rows.lookup("#initialExpansionNodeType-0");
    assertEquals("node-1", nodeType.getValue());
    assertEquals("Team_DM", nodeType.getConverter().toString("node-1"), "a node type is shown as its Document Model");
    assertEquals(List.of("node-1", "node-2"), nodeType.getItems(), "its own node type plus the unused ones");

    CheckBox pagination = editor.field("paginationPanelController", "paginationField");
    Spinner<Integer> pageSize = editor.field("paginationPanelController", "pageSizeField");
    assertTrue(pagination.isSelected());
    assertEquals(5, pageSize.getValue());

    CheckBox wholeTree = editor.field("wholeTreeExpansionPanelController", "wholeTreeExpansionField");
    assertTrue(wholeTree.isSelected());
  }

  // ---- Initial Expansion ----

  @Test
  void enablingTheInitialExpansionWritesAllLevelsAndDisablingItDropsIt(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE.replace("\"initialExpansion\": {\"type\": \"all_levels\", \"affectedNodeRefs\": [\"node-1\"]}, ", ""));
    CheckBox enable = editor.field("initialExpansionPanelController", "enableField");
    VBox details = editor.field("initialExpansionPanelController", "detailsBox");
    assertFalse(enable.isSelected());
    assertFalse(shownNode(details));

    FxTestSupport.onFx(() -> enable.setSelected(true));
    assertTrue(shownNode(details));
    assertEquals("all_levels", editor.saved().getInitialExpansion().getType());
    assertNull(editor.saved().getInitialExpansion().getAffectedNodeRefs(), "no node types means all, an absent key");

    FxTestSupport.onFx(() -> enable.setSelected(false));
    assertNull(editor.saved().getInitialExpansion());
    assertFalse(shownNode(details));
  }

  @Test
  void anInitialExpansionByLevelAsksForTheNumberOfLevels(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE);
    ComboBox<String> type = editor.field("initialExpansionPanelController", "typeField");
    VBox levelsBox = editor.field("initialExpansionPanelController", "levelsBox");
    Spinner<Integer> levels = editor.field("initialExpansionPanelController", "levelsField");

    FxTestSupport.onFx(() -> type.setValue("level_limit"));
    assertTrue(shownNode(levelsBox));
    assertEquals(1, levels.getValue());
    assertEquals("level_limit", editor.saved().getInitialExpansion().getType());
    assertEquals(1, editor.saved().getInitialExpansion().getLevel());
    assertEquals(List.of("node-1"), editor.saved().getInitialExpansion().getAffectedNodeRefs(), "the node types are kept");

    FxTestSupport.onFx(() -> levels.getValueFactory().setValue(3));
    assertEquals(3, editor.saved().getInitialExpansion().getLevel());

    FxTestSupport.onFx(() -> type.setValue("all_levels"));
    assertFalse(shownNode(levelsBox));
    assertNull(editor.saved().getInitialExpansion().getLevel(), "the level only belongs to level_limit");
  }

  @Test
  void nodeTypesToApplyAreUniqueAndAnEmptyListIsWrittenAsAbsent(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE);
    VBox rows = editor.field("initialExpansionPanelController", "nodeRefRows");
    Button add = editor.field("initialExpansionPanelController", "addNodeTypeButton");
    Label none = editor.field("initialExpansionPanelController", "noNodeTypesLabel");
    assertFalse(add.isDisabled());
    assertFalse(none.isVisible());

    FxTestSupport.onFx(add::fire);
    assertEquals(List.of("node-1", "node-2"), editor.saved().getInitialExpansion().getAffectedNodeRefs());
    assertTrue(add.isDisabled(), "every node type is used");
    @SuppressWarnings("unchecked")
    ComboBox<String> first = (ComboBox<String>) rows.lookup("#initialExpansionNodeType-0");
    assertEquals(List.of("node-1"), first.getItems(), "node-2 is taken by the other row");

    FxTestSupport.onFx(() -> deleteButton(rows, 0).fire());
    FxTestSupport.onFx(() -> deleteButton(rows, 0).fire());
    assertNull(editor.saved().getInitialExpansion().getAffectedNodeRefs());
    assertTrue(none.isVisible());
    assertEquals("all_levels", editor.saved().getInitialExpansion().getType(), "the initial expansion itself stays");
  }

  @Test
  void aNodeTypeThatIsGoneStaysVisibleInsteadOfBeingDropped(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE.replace("[\"node-1\"]", "[\"node-gone\"]"));
    VBox rows = editor.field("initialExpansionPanelController", "nodeRefRows");

    @SuppressWarnings("unchecked")
    ComboBox<String> row = (ComboBox<String>) rows.lookup("#initialExpansionNodeType-0");
    assertEquals("node-gone", row.getValue());
    assertEquals("node-gone", row.getConverter().toString("node-gone"));
    assertEquals(List.of("node-1", "node-2", "node-gone"), row.getItems());
  }

  @Test
  void deletingANodeTypeRemovesItFromTheInitialExpansion(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE);
    TreeNodeTypesPanelController nodeTypes = editor.panel("nodeTypesPanelController");
    TreeNode node = ((TreeModel) editor.item().getModel()).getContent().getNodes().get(0);

    Method removeNode = TreeNodeTypesPanelController.class.getDeclaredMethod("removeNode", TreeNode.class);
    removeNode.setAccessible(true);
    FxTestSupport.onFx(() -> {
      try {
        removeNode.invoke(nodeTypes, node);
      }
      catch (ReflectiveOperationException e) {
        throw new IllegalStateException(e);
      }
    });

    InitialExpansion initialExpansion = ((TreeModel) editor.item().getModel()).getContent().getConfiguration().getExpansionStrategy().getInitialExpansion();
    assertNull(initialExpansion.getAffectedNodeRefs());
    assertEquals("all_levels", initialExpansion.getType());
  }

  // ---- Pagination and whole tree ----

  @Test
  void paginationIsOnWhileThereIsAPageSizeAndPrefilledWithTen(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE.replace(", \"pageSize\": 5", ""));
    CheckBox pagination = editor.field("paginationPanelController", "paginationField");
    VBox pageSizeBox = editor.field("paginationPanelController", "pageSizeBox");
    Spinner<Integer> pageSize = editor.field("paginationPanelController", "pageSizeField");
    assertFalse(pagination.isSelected());
    assertFalse(shownNode(pageSizeBox));

    FxTestSupport.onFx(() -> pagination.setSelected(true));
    assertTrue(shownNode(pageSizeBox));
    assertEquals(10, pageSize.getValue());
    assertEquals(10, editor.saved().getPageSize());

    FxTestSupport.onFx(() -> pageSize.getValueFactory().setValue(25));
    assertEquals(25, editor.saved().getPageSize());

    FxTestSupport.onFx(() -> pagination.setSelected(false));
    assertNull(editor.saved().getPageSize());
  }

  @Test
  void theWholeTreeFlagIsWrittenAsTrueOrOmitted(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE.replace("\"wholeTreeExpansion\": true,", ""));
    CheckBox wholeTree = editor.field("wholeTreeExpansionPanelController", "wholeTreeExpansionField");
    assertFalse(wholeTree.isSelected());

    FxTestSupport.onFx(() -> wholeTree.setSelected(true));
    assertEquals(Boolean.TRUE, editor.savedModel().getContent().getConfiguration().getWholeTreeExpansion());

    FxTestSupport.onFx(() -> wholeTree.setSelected(false));
    assertNull(editor.savedModel().getContent().getConfiguration().getWholeTreeExpansion());
    assertFalse(editor.savedModel().getContent().getConfiguration().getExtras().containsKey("wholeTreeExpansion"));
  }

  // ---- switching the strategy ----

  @Test
  void switchingToTreeDropsTheLevelByLevelKeysAndSwitchingBackDropsTheDepths(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE);
    ComboBox<String> strategy = editor.field("configurationPanelController", "expansionStrategyField");

    FxTestSupport.onFx(() -> strategy.setValue("tree"));
    assertTrue(shown(editor.panel("expansionDepthsPanelController")));
    assertFalse(shown(editor.panel("initialExpansionPanelController")));
    assertFalse(shown(editor.panel("paginationPanelController")));
    ExpansionStrategy tree = editor.saved();
    assertEquals("tree", tree.getType());
    assertNull(tree.getInitialExpansion());
    assertNull(tree.getPageSize());
    assertEquals(List.of(), tree.getExpansionDepths(), "SME writes an empty list for the tree strategy");

    FxTestSupport.onFx(() -> strategy.setValue("level_by_level"));
    assertTrue(shown(editor.panel("initialExpansionPanelController")));
    assertTrue(shown(editor.panel("paginationPanelController")));
    assertFalse(shown(editor.panel("expansionDepthsPanelController")));
    ExpansionStrategy levelByLevel = editor.saved();
    assertNull(levelByLevel.getExpansionDepths());
    CheckBox enable = editor.field("initialExpansionPanelController", "enableField");
    CheckBox pagination = editor.field("paginationPanelController", "paginationField");
    assertFalse(enable.isSelected(), "the initial expansion was dropped by the switch");
    assertFalse(pagination.isSelected());
  }

  @Test
  void aTreeWithoutAStrategyShowsLevelByLevelWithoutWritingItUntilSomethingIsEdited(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE.replace("\"expansionStrategy\": {\"initialExpansion\": {\"type\": \"all_levels\", \"affectedNodeRefs\": [\"node-1\"]}, \"type\": \"level_by_level\", \"pageSize\": 5}", "\"rootRef\": null"));
    ComboBox<String> strategy = editor.field("configurationPanelController", "expansionStrategyField");
    CheckBox pagination = editor.field("paginationPanelController", "paginationField");

    assertEquals("level_by_level", strategy.getValue());
    assertTrue(shown(editor.panel("initialExpansionPanelController")));
    assertFalse(shown(editor.panel("expansionDepthsPanelController")));
    assertNull(editor.savedModel().getContent().getConfiguration().getExpansionStrategy(), "showing the default is not an edit");

    FxTestSupport.onFx(() -> pagination.setSelected(true));
    assertEquals("level_by_level", editor.saved().getType());
    assertEquals(10, editor.saved().getPageSize());
  }

  @Test
  void eachRelationshipOnlyGetsOneExpansionDepth(@TempDir Path dir) throws Exception {
    String json = TREE.replace("\"nodes\": [{\"id\": \"node-1\", \"documentModelRef\": \"Team_DM\", \"configuration\": {}}",
            "\"nodes\": [{\"id\": \"node-1\", \"documentModelRef\": \"Team_DM\", \"configuration\": {}, \"childRelationshipConfigurations\": ["
                + "{\"id\": \"crc-1\", \"relationshipModelRef\": \"TeamTeam_Re\", \"parentRole\": \"Parent\"},"
                + "{\"id\": \"crc-2\", \"relationshipModelRef\": \"TeamPerson_Re\", \"parentRole\": \"Team\"}]}")
        .replace("\"expansionStrategy\": {\"initialExpansion\": {\"type\": \"all_levels\", \"affectedNodeRefs\": [\"node-1\"]}, \"type\": \"level_by_level\", \"pageSize\": 5}",
            "\"expansionStrategy\": {\"type\": \"tree\", \"expansionDepths\": [{\"relationshipModel\": \"TeamTeam_Re\", \"maxDepth\": 5}]}");
    Editor editor = open(dir, json);
    TreeExpansionDepthsPanelController depths = editor.panel("expansionDepthsPanelController");
    Button add = editor.field("expansionDepthsPanelController", "addButton");
    de.a12.studio.models.treemodel.ExpansionDepth existing =
        ((TreeModel) editor.item().getModel()).getContent().getConfiguration().getExpansionStrategy().getExpansionDepths().get(0);

    assertEquals(List.of("TeamPerson_Re"), depths.unusedRelationshipChoices(null), "a relationship that has a depth is not offered again");
    assertEquals(List.of("TeamTeam_Re", "TeamPerson_Re"), depths.unusedRelationshipChoices(existing), "the edited depth keeps its own");
    assertFalse(add.isDisabled());

    de.a12.studio.models.treemodel.ExpansionDepth second = new de.a12.studio.models.treemodel.ExpansionDepth();
    second.setRelationshipModel("TeamPerson_Re");
    second.setMaxDepth(1);
    FxTestSupport.onFx(() -> {
      ((TreeModel) editor.item().getModel()).getContent().getConfiguration().getExpansionStrategy().getExpansionDepths().add(second);
      depths.refresh();
    });
    assertTrue(add.isDisabled(), "every relationship of the tree has its depth");
  }

  @Test
  void reSelectingTheSameStrategyChangesNothing(@TempDir Path dir) throws Exception {
    Editor editor = open(dir, TREE);
    ExpansionStrategy strategy = ((TreeModel) editor.item().getModel()).getContent().getConfiguration().getExpansionStrategy();

    strategy.switchTypeTo("level_by_level");

    assertEquals(5, strategy.getPageSize());
    assertNotNull(strategy.getInitialExpansion());
  }

  private static Button deleteButton(VBox rows, int row) {
    HBox rowBox = (HBox) rows.getChildren().get(row);
    return (Button) rowBox.getChildren().get(rowBox.getChildren().size() - 1);
  }
}
