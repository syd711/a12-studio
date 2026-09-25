package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.ButtonElement;
import de.a12.studio.models.overviewmodel.ConfigurableBoxElement;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.ExpandAllPopupElement;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.ExpansionDepth;
import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.editors.overviewmodel.SubheaderSlotPanelController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Tree Model editor's tabs and property editors: Root, Columns and Node Types on the first tab, the
 * Subheader and Footer slots on the Custom Actions tab, the Accessibility and Styles editors on the Layout tab,
 * and the Node Types list's rows.
 */
class TreeModelEditorPanelsTest {

  private static final String EDITOR_FXML = "/de/a12/studio/ui/editors/treemodel/tree-model-editor.fxml";

  private static final String TREE = """
      {"header": {"id": "Team_TM", "modelType": "tree", "modelVersion": "11.0.0", "modelReferences": [
         {"purpose": "relationship-model-for-tree", "modelType": "relationship", "alias": "RM1", "reference": "TeamTeam_Re"}]},
       "content": {
         "configuration": {"rootRef": "crc-2", "hierarchicalColumnRef": "column-1"},
         "columns": [{"id": "column-1", "name": "Name", "width": 1}],
         "nodes": [
           {"id": "node-1", "documentModelRef": "Team_DM", "configuration": {"dnd": true},
            "childRelationshipConfigurations": [{"id": "crc-1", "relationshipModelRef": "TeamPerson_Re", "parentRole": "Team"},
                                                {"id": "crc-2", "relationshipModelRef": "TeamTeam_Re", "parentRole": "Parent"}],
            "columns": [{"columnRef": "column-1", "elementRef": "field_name"}]},
           {"id": "node-2", "documentModelRef": "Person_DM", "configuration": {}}]}}
      """;

  private static boolean toolkitAvailable;

  private final List<AbstractPropertyEditor> panels = new ArrayList<>();

  @BeforeAll
  static void startToolkit() throws Exception {
    toolkitAvailable = FxTestSupport.startToolkit();
  }

  @AfterEach
  void tearDown() throws Exception {
    // Unregisters the panels from the application-wide event manager (and cancels pending debounced saves).
    for (AbstractPropertyEditor panel : panels) {
      panel.destroy();
    }
    FxTestSupport.onFx(() -> {
    });
    FxTestSupport.selectProjectItem(null);
  }

  private ProjectItem selectTree(Path dir) throws Exception {
    Path file = dir.resolve("Team_TM.json");
    Files.writeString(file, TREE);
    ProjectItem item = new ProjectItem(file.toFile());
    assertNotNull(item.getModel(), "the fixture tree model must load");
    FxTestSupport.selectProjectItem(item);
    return item;
  }

  private static TreeModel reloaded(ProjectItem item) {
    return (TreeModel) new ProjectItem(item.getFile()).getModel();
  }

  private <T extends AbstractPropertyEditor> FxTestSupport.Loaded<T> load(String panelFxml) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FxTestSupport.Loaded<T> loaded = FxTestSupport.load("/de/a12/studio/ui/editors/treemodel/" + panelFxml);
    panels.add(loaded.controller());
    return loaded;
  }

  // ---- tab layout ----

  @Test
  void firstTabHoldsRootColumnsAndNodeTypesAndLayoutIsANewTab() throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FxTestSupport.Loaded<TreeModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    TabPane tabs = (TabPane) ((BorderPane) loaded.root()).getCenter();

    assertEquals(List.of(StudioBundle.get("tree_model_editor.tab_tree"), StudioBundle.get("configuration"),
            StudioBundle.get("custom_actions"), StudioBundle.get("tree_model_editor.tab_layout")),
        tabs.getTabs().stream().map(Tab::getText).toList(),
        "the former Nodes tab is gone; Custom Actions is the third tab, followed by Layout");
    assertEquals(List.of(StudioBundle.get("tree_root"), StudioBundle.get("columns"), StudioBundle.get("node_types")),
        paneTitles(tabs.getTabs().get(0)));
    assertEquals(List.of(StudioBundle.get("tree_virtual_scrolling_panel.title"),
            StudioBundle.get("row_height_and_action_column_width"), StudioBundle.get("tree_columns_resize_panel.title"),
            StudioBundle.get("accessibility"), StudioBundle.get("styles")),
        paneTitles(tabs.getTabs().get(3)));

    for (String panel : List.of("rootPanelController", "columnsPanelController", "nodeTypesPanelController",
        "configurationPanelController", "multiSelectionPanelController", "dragAndDropPanelController",
        "subheaderMajorController", "subheaderMinorController", "footerMinorButtonsController",
        "footerMajorButtonsController", "virtualScrollingPanelController", "rowHeightActionColumnWidthPanelController",
        "columnsResizePanelController", "accessibilityPanelController", "stylesPanelController")) {
      AbstractPropertyEditor controller = FxTestSupport.field(loaded.controller(), panel);
      assertNotNull(controller, panel + " must be injected");
      panels.add(controller);
    }
  }

  private static List<String> paneTitles(Tab tab) {
    VBox box = (VBox) ((ScrollPane) tab.getContent()).getContent();
    return box.getChildren().stream().map(node -> ((TitledPane) node).getText()).toList();
  }

  // ---- Custom Actions ----

  private FxTestSupport.Loaded<TreeModelEditorController> loadEditor(ProjectItem item) throws Exception {
    assumeTrue(toolkitAvailable, "No JavaFX toolkit available");
    FxTestSupport.Loaded<TreeModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    for (java.lang.reflect.Field field : TreeModelEditorController.class.getDeclaredFields()) {
      if (AbstractPropertyEditor.class.isAssignableFrom(field.getType())) {
        panels.add(FxTestSupport.field(loaded.controller(), field.getName()));
      }
    }
    FxTestSupport.onFx(() -> loaded.controller().loadModel(item.getModel()));
    return loaded;
  }

  @Test
  void customActionsIsTheThirdTabWithSubheaderAndFooterSlots(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    FxTestSupport.Loaded<TreeModelEditorController> loaded = loadEditor(item);
    TabPane tabs = (TabPane) ((BorderPane) loaded.root()).getCenter();

    Tab tab = tabs.getTabs().get(2);
    assertEquals(StudioBundle.get("custom_actions"), tab.getText());
    VBox box = (VBox) ((ScrollPane) tab.getContent()).getContent();
    assertEquals(List.of(StudioBundle.get("subheader"), StudioBundle.get("major_buttons"), StudioBundle.get("minor_buttons"),
            StudioBundle.get("footer"), StudioBundle.get("minor_buttons"), StudioBundle.get("major_buttons")),
        box.getChildren().stream().map(node -> node instanceof TitledPane pane ? pane.getText() : ((Label) node).getText()).toList(),
        "no row actions, context menu, row activation or titles, unlike the Overview Model");

    TreeModel model = (TreeModel) item.getModel();
    assertNotNull(model.getContent().getSubHeaderBox(), "a model without boxes gets empty ones, written as empty slots");
    assertNotNull(model.getContent().getFooterBox());
  }

  @Test
  void loadedSubheaderElementsAreListedByTypeAndEventInTheirSlot(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    ElementBox box = ElementBox.createEmpty();
    box.getLeftSlot().add(new ExpandAllPopupElement());
    ButtonElement button = new ButtonElement();
    button.setId("button-026dc");
    button.setEvent("event_add_root_node");
    button.setIconName("add");
    box.getLeftSlot().add(button);
    model.getContent().setSubHeaderBox(box);

    FxTestSupport.Loaded<TreeModelEditorController> loaded = loadEditor(item);

    SubheaderSlotPanelController minor = FxTestSupport.field(loaded.controller(), "subheaderMinorController");
    VBox grid = FxTestSupport.field(minor, "rowsList");
    assertEquals(StudioBundle.get("subheader_slot.type_expand_all_popup"), ((Label) grid.lookup("#subheaderSlotType-0")).getText());
    assertEquals("", ((Label) grid.lookup("#subheaderSlotEvent-0")).getText());
    assertEquals(StudioBundle.get("subheader_slot.type_button"), ((Label) grid.lookup("#subheaderSlotType-1")).getText());
    assertEquals("event_add_root_node", ((Label) grid.lookup("#subheaderSlotEvent-1")).getText());
    assertEquals("add", ((Label) grid.lookup("#subheaderSlotIcon-1")).getText());

    SubheaderSlotPanelController major = FxTestSupport.field(loaded.controller(), "subheaderMajorController");
    Label emptyLabel = FxTestSupport.field(major, "emptyLabel");
    assertTrue(emptyLabel.isVisible(), "the right slot is empty");
  }

  @Test
  void theSubheaderOffersButtonMultiSelectionAndExpandAllPopUpAndNumbersNewButtons(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    FxTestSupport.Loaded<TreeModelEditorController> loaded = loadEditor(item);
    SubheaderSlotPanelController major = FxTestSupport.field(loaded.controller(), "subheaderMajorController");
    MenuButton add = FxTestSupport.field(major, "addButton");

    assertEquals(List.of(StudioBundle.get("subheader_slot.type_button"), StudioBundle.get("subheader_slot.type_multi_selection"),
            StudioBundle.get("subheader_slot.type_expand_all_popup")),
        add.getItems().stream().map(MenuItem::getText).toList(), "no Search or Filter in a Tree Model");

    FxTestSupport.onFx(() -> {
      add.getItems().get(2).fire();
      add.getItems().get(0).fire();
    });

    List<BoxElement> rightSlot = reloaded(item).getContent().getSubHeaderBox().getRightSlot();
    assertEquals(2, rightSlot.size(), "Major is the right slot; saved to disk");
    ConfigurableBoxElement expandAll = assertInstanceOf(ExpandAllPopupElement.class, rightSlot.get(0));
    assertNull(expandAll.getId(), "only buttons get an id");
    ButtonElement button = assertInstanceOf(ButtonElement.class, rightSlot.get(1));
    assertTrue(button.getId().matches("button-[0-9a-f]{5}"), "new Tree buttons get an id like SME's, got " + button.getId());
  }

  // ---- Root ----

  @Test
  void rootOffersTheChildRelationshipConfigurationsAndPersistsThePick(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    FxTestSupport.Loaded<TreeRootPanelController> loaded = load("tree-root-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));

    ComboBox<String> combo = FxTestSupport.field(loaded.controller(), "rootField");
    assertEquals(List.of("crc-1", "crc-2"), combo.getItems());
    assertEquals("crc-2", combo.getValue());
    assertEquals("Team_DM → TeamTeam_Re", combo.getConverter().toString("crc-2"));
    assertFalse(errorShown(loaded.controller()));

    FxTestSupport.onFx(() -> combo.setValue("crc-1"));

    assertEquals("crc-1", model.getContent().getConfiguration().getRootRef());
    assertEquals("crc-1", reloaded(item).getContent().getConfiguration().getRootRef(), "saved to disk");
  }

  @Test
  void aRootThatNoLongerResolvesIsKeptAndReportedByName(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    model.getContent().getConfiguration().setRootRef("crc-gone");
    FxTestSupport.Loaded<TreeRootPanelController> loaded = load("tree-root-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));

    assertTrue(errorShown(loaded.controller()));
    assertEquals(StudioBundle.get("tree_root_invalid_reference", "crc-gone"), errorText(loaded.controller()));
    assertEquals("crc-gone", model.getContent().getConfiguration().getRootRef(), "not silently dropped");
  }

  // ---- Accessibility ----

  @Test
  void hideLabelIsWrittenAsTrueOrOmitted(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    FxTestSupport.Loaded<TreeAccessibilityPanelController> loaded = load("tree-accessibility-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));
    CheckBox hideLabel = FxTestSupport.field(loaded.controller(), "hideLabelField");
    assertFalse(hideLabel.isSelected());

    FxTestSupport.onFx(() -> hideLabel.setSelected(true));
    assertEquals(Boolean.TRUE, reloaded(item).getContent().getConfiguration().getLabelHidden());

    FxTestSupport.onFx(() -> hideLabel.setSelected(false));
    assertNull(reloaded(item).getContent().getConfiguration().getLabelHidden(), "never written as false");
    assertFalse(Files.readString(item.getFile().toPath()).contains("labelHidden"));
  }

  // ---- Multi-Selection ----

  @Test
  void enablingMultiSelectionSeedsSmesDefaultsAndDisablingRemovesIt(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    FxTestSupport.Loaded<TreeMultiSelectionPanelController> loaded = load("tree-multi-selection-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));
    CheckBox enabled = FxTestSupport.field(loaded.controller(), "multiSelectionEnabledField");
    CheckBox clearConfirmation = FxTestSupport.field(loaded.controller(), "clearConfirmationField");
    assertFalse(enabled.isSelected());
    assertNull(model.getContent().getConfiguration().getMultiSelection());

    FxTestSupport.onFx(() -> enabled.setSelected(true));

    MultiSelectionConfig saved = reloaded(item).getContent().getConfiguration().getMultiSelection();
    assertNotNull(saved, "saved to disk");
    assertEquals("collapsible_collapsed", saved.getCollapseOption());
    assertEquals("simple", saved.getCounterOption());
    assertEquals("checkbox_and_row", saved.getSelectionArea());
    assertEquals(Boolean.TRUE, saved.getClearConfirmation().getEnabled());
    assertTrue(clearConfirmation.isSelected());
    assertNull(saved.getSelectParent(), "the flag is omitted unless it is set");

    FxTestSupport.onFx(() -> enabled.setSelected(false));

    assertNull(reloaded(item).getContent().getConfiguration().getMultiSelection());
    assertFalse(Files.readString(item.getFile().toPath()).contains("multiSelection"));
  }

  @Test
  void multiSelectionOptionsAndSelectParentArePersisted(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    FxTestSupport.Loaded<TreeMultiSelectionPanelController> loaded = load("tree-multi-selection-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));
    CheckBox enabled = FxTestSupport.field(loaded.controller(), "multiSelectionEnabledField");
    ComboBox<String> collapse = FxTestSupport.field(loaded.controller(), "collapseOptionField");
    ComboBox<String> selectionArea = FxTestSupport.field(loaded.controller(), "selectionAreaField");
    CheckBox selectParent = FxTestSupport.field(loaded.controller(), "selectParentField");
    FxTestSupport.onFx(() -> enabled.setSelected(true));

    FxTestSupport.onFx(() -> {
      collapse.setValue("non_collapsible");
      selectionArea.setValue("checkbox");
      selectParent.setSelected(true);
    });

    assertEquals(StudioBundle.get("multi_selection_panel.collapse_option.non_collapsible"),
        collapse.getConverter().toString("non_collapsible"), "options read as words, not ids");
    MultiSelectionConfig saved = reloaded(item).getContent().getConfiguration().getMultiSelection();
    assertEquals("non_collapsible", saved.getCollapseOption());
    assertEquals("checkbox", saved.getSelectionArea());
    assertEquals(Boolean.TRUE, saved.getSelectParent());

    FxTestSupport.onFx(() -> selectParent.setSelected(false));
    assertNull(reloaded(item).getContent().getConfiguration().getMultiSelection().getSelectParent(), "written as true or omitted");
  }

  @Test
  void aLoadedMultiSelectionKeepsItsActionsWithTheirIds(@TempDir Path dir) throws Exception {
    Path file = dir.resolve("Team_TM.json");
    Files.writeString(file, TREE.replace("\"configuration\": {\"rootRef\": \"crc-2\", \"hierarchicalColumnRef\": \"column-1\"}",
        "\"configuration\": {\"multiSelection\": {\"collapseOption\": \"collapsible_expanded\", \"counterOption\": \"none\","
            + " \"selectionArea\": \"checkbox\", \"selectParent\": true, \"clearConfirmation\": {\"enabled\": true},"
            + " \"buttons\": [{\"id\": \"button-15174\", \"event\": \"event_copy_nodes\", \"destructive\": false, \"primary\": false},"
            + " {\"id\": \"button-54493\", \"event\": \"event_delete_nodes\", \"destructive\": true, \"primary\": false}]}}"));
    ProjectItem item = new ProjectItem(file.toFile());
    FxTestSupport.selectProjectItem(item);
    TreeModel model = (TreeModel) item.getModel();
    FxTestSupport.Loaded<TreeMultiSelectionPanelController> loaded = load("tree-multi-selection-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));

    CheckBox selectParent = FxTestSupport.field(loaded.controller(), "selectParentField");
    ComboBox<String> counter = FxTestSupport.field(loaded.controller(), "counterOptionField");
    VBox rows = FxTestSupport.field(loaded.controller(), "actionRows");
    assertTrue(selectParent.isSelected());
    assertEquals("none", counter.getValue());
    assertEquals(2, rows.getChildren().size());
    assertEquals("event_delete_nodes", rowLabel(rows, 1, "#multiSelectionActionEvent-1"));
    assertEquals(StudioBundle.get("yes"), rowLabel(rows, 1, "#multiSelectionActionDestructive-1"));

    FxTestSupport.onFx(moveButtons(rows, 0).get(1)::fire);

    List<de.a12.studio.models.overviewmodel.Button> buttons = reloaded(item).getContent().getConfiguration().getMultiSelection().getButtons();
    assertEquals(List.of("button-54493", "button-15174"), buttons.stream().map(de.a12.studio.models.overviewmodel.Button::getId).toList());
  }

  // ---- Drag and Drop ----

  @Test
  void dragAndDropIsAbsentUntilEnabledAndThenExpandsOnHoverByDefault(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    FxTestSupport.Loaded<TreeDragAndDropPanelController> loaded = load("tree-drag-and-drop-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));
    CheckBox enabled = FxTestSupport.field(loaded.controller(), "dragAndDropEnabledField");
    CheckBox expandOnHover = FxTestSupport.field(loaded.controller(), "expandOnHoverField");
    assertFalse(enabled.isSelected());
    assertTrue(expandOnHover.isDisabled(), "Expand On Hover can't be configured while drag and drop is off");

    FxTestSupport.onFx(() -> enabled.setSelected(true));

    assertFalse(expandOnHover.isDisabled());
    assertTrue(expandOnHover.isSelected());
    assertEquals(Map.of("onDrag", Map.of("expandHoveredNode", true)), reloaded(item).getContent().getConfiguration().getDnd());

    FxTestSupport.onFx(() -> expandOnHover.setSelected(false));
    assertEquals(Map.of("onDrag", Map.of("expandHoveredNode", false)), reloaded(item).getContent().getConfiguration().getDnd());

    FxTestSupport.onFx(() -> enabled.setSelected(false));
    // (the file still contains "dnd": true on the node type - that is the per-node flag, not this key)
    assertNull(reloaded(item).getContent().getConfiguration().getDnd());
  }

  @Test
  void aLoadedDragAndDropShowsItsExpandOnHoverFlag(@TempDir Path dir) throws Exception {
    Path file = dir.resolve("Team_TM.json");
    Files.writeString(file, TREE.replace("\"configuration\": {\"rootRef\": \"crc-2\", \"hierarchicalColumnRef\": \"column-1\"}",
        "\"configuration\": {\"dnd\": {\"onDrag\": {\"expandHoveredNode\": false}}}"));
    ProjectItem item = new ProjectItem(file.toFile());
    FxTestSupport.selectProjectItem(item);
    FxTestSupport.Loaded<TreeDragAndDropPanelController> loaded = load("tree-drag-and-drop-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel((TreeModel) item.getModel()));

    CheckBox enabled = FxTestSupport.field(loaded.controller(), "dragAndDropEnabledField");
    CheckBox expandOnHover = FxTestSupport.field(loaded.controller(), "expandOnHoverField");
    assertTrue(enabled.isSelected());
    assertFalse(expandOnHover.isSelected());
  }

  // ---- Virtual Scrolling / Row Height And Action Column Width / Columns Resize ----

  @Test
  void virtualScrollingIsWrittenAsTrueOrOmitted(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    FxTestSupport.Loaded<TreeVirtualScrollingPanelController> loaded = load("tree-virtual-scrolling-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel((TreeModel) item.getModel()));
    CheckBox enabled = FxTestSupport.field(loaded.controller(), "virtualScrollingField");
    assertFalse(enabled.isSelected());

    FxTestSupport.onFx(() -> enabled.setSelected(true));
    assertEquals(Boolean.TRUE, reloaded(item).getContent().getConfiguration().getEnableVirtualScroll());

    FxTestSupport.onFx(() -> enabled.setSelected(false));
    assertNull(reloaded(item).getContent().getConfiguration().getEnableVirtualScroll());
    assertFalse(Files.readString(item.getFile().toPath()).contains("enableVirtualScroll"));
  }

  @Test
  void columnsResizeIsWrittenAsTrueOrOmitted(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    FxTestSupport.Loaded<TreeColumnsResizePanelController> loaded = load("tree-columns-resize-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel((TreeModel) item.getModel()));
    CheckBox enabled = FxTestSupport.field(loaded.controller(), "columnsResizeField");
    assertFalse(enabled.isSelected());

    FxTestSupport.onFx(() -> enabled.setSelected(true));
    assertEquals(Boolean.TRUE, reloaded(item).getContent().getConfiguration().getEnableColumnsResize());

    FxTestSupport.onFx(() -> enabled.setSelected(false));
    assertNull(reloaded(item).getContent().getConfiguration().getEnableColumnsResize());
    assertFalse(Files.readString(item.getFile().toPath()).contains("enableColumnsResize"));
  }

  @Test
  void rowHeightAndActionColumnWidthArePersistedAndBlankMeansAbsent(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    FxTestSupport.Loaded<TreeRowHeightActionColumnWidthPanelController> loaded = load("tree-row-height-action-column-width-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel((TreeModel) item.getModel()));
    TextField rowHeight = FxTestSupport.field(loaded.controller(), "rowHeightField");
    TextField actionColumnWidth = FxTestSupport.field(loaded.controller(), "actionColumnWidthField");
    assertEquals("", rowHeight.getText());
    assertEquals("", actionColumnWidth.getText());

    FxTestSupport.onFx(() -> {
      rowHeight.setText("49");
      actionColumnWidth.setText("1.5");
    });
    assertEquals(49, reloaded(item).getContent().getConfiguration().getRowHeight());
    assertEquals(1.5, reloaded(item).getContent().getConfiguration().getActionColumnWidth());

    FxTestSupport.onFx(() -> {
      rowHeight.setText("");
      actionColumnWidth.setText("");
    });
    String json = Files.readString(item.getFile().toPath());
    assertFalse(json.contains("rowHeight"));
    assertFalse(json.contains("actionColumnWidth"));
  }

  @Test
  void aLoadedRowHeightAndIntegerActionColumnWidthAreShownAndSurviveUntouched(@TempDir Path dir) throws Exception {
    Path file = dir.resolve("Team_TM.json");
    Files.writeString(file, TREE.replace("\"configuration\": {\"rootRef\": \"crc-2\", \"hierarchicalColumnRef\": \"column-1\"}",
        "\"configuration\": {\"actionColumnWidth\": 1, \"enableColumnsResize\": true, \"enableVirtualScroll\": true, \"rowHeight\": 49}"));
    ProjectItem item = new ProjectItem(file.toFile());
    FxTestSupport.selectProjectItem(item);

    FxTestSupport.Loaded<TreeRowHeightActionColumnWidthPanelController> rows = load("tree-row-height-action-column-width-panel.fxml");
    FxTestSupport.onFx(() -> rows.controller().setModel((TreeModel) item.getModel()));
    TextField rowHeight = FxTestSupport.field(rows.controller(), "rowHeightField");
    TextField actionColumnWidth = FxTestSupport.field(rows.controller(), "actionColumnWidthField");
    assertEquals("49", rowHeight.getText());
    assertEquals("1.0", actionColumnWidth.getText());

    FxTestSupport.Loaded<TreeVirtualScrollingPanelController> virtual = load("tree-virtual-scrolling-panel.fxml");
    FxTestSupport.onFx(() -> virtual.controller().setModel((TreeModel) item.getModel()));
    assertTrue(((CheckBox) FxTestSupport.field(virtual.controller(), "virtualScrollingField")).isSelected());

    FxTestSupport.Loaded<TreeColumnsResizePanelController> resize = load("tree-columns-resize-panel.fxml");
    FxTestSupport.onFx(() -> resize.controller().setModel((TreeModel) item.getModel()));
    assertTrue(((CheckBox) FxTestSupport.field(resize.controller(), "columnsResizeField")).isSelected());

    // Merely loading must not rewrite the width token (1 stays 1, not 1.0).
    assertEquals(1, reloaded(item).getContent().getConfiguration().getActionColumnWidthNode().intValue());
    assertTrue(reloaded(item).getContent().getConfiguration().getActionColumnWidthNode().isIntegralNumber());
  }

  // ---- Node types ----

  @Test
  void nodeTypesAreOneRowEachWithTheDocumentModelAndDragDropFlag(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    FxTestSupport.Loaded<TreeNodeTypesPanelController> loaded = load("tree-node-types-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel((TreeModel) item.getModel(), item));

    VBox rows = FxTestSupport.field(loaded.controller(), "nodeRows");
    assertEquals(2, rows.getChildren().size());
    assertEquals("Team_DM", rowLabel(rows, 0, "#treeNodeDocumentModel-0"));
    assertEquals(StudioBundle.get("yes"), rowLabel(rows, 0, "#treeNodeDragDrop-0"));
    assertEquals("Person_DM", rowLabel(rows, 1, "#treeNodeDocumentModel-1"));
    assertEquals(StudioBundle.get("no"), rowLabel(rows, 1, "#treeNodeDragDrop-1"));
    Label empty = FxTestSupport.field(loaded.controller(), "nodesEmptyLabel");
    assertFalse(empty.isVisible());
  }

  @Test
  void movingARowReordersTheNodesSavesAndNotifiesTheOwner(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    FxTestSupport.Loaded<TreeNodeTypesPanelController> loaded = load("tree-node-types-panel.fxml");
    int[] changes = {0};
    FxTestSupport.onFx(() -> {
      loaded.controller().setModel(model, item);
      loaded.controller().setOnChange(() -> changes[0]++);
    });

    VBox rows = FxTestSupport.field(loaded.controller(), "nodeRows");
    Button moveDown = moveButtons(rows, 0).get(1);
    FxTestSupport.onFx(moveDown::fire);

    assertEquals(List.of("node-2", "node-1"), model.getContent().getNodes().stream().map(TreeNode::getId).toList());
    assertEquals(List.of("node-2", "node-1"), reloaded(item).getContent().getNodes().stream().map(TreeNode::getId).toList());
    assertEquals(1, changes[0]);
    assertEquals("Person_DM", rowLabel(rows, 0, "#treeNodeDocumentModel-0"), "rows are rebuilt in the new order");
  }

  @Test
  void anEmptyListShowsThePlaceholderInsteadOfTheHeader(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    model.getContent().getNodes().clear();
    FxTestSupport.Loaded<TreeNodeTypesPanelController> loaded = load("tree-node-types-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model, item));

    HBox headers = FxTestSupport.field(loaded.controller(), "nodeHeaders");
    Label empty = FxTestSupport.field(loaded.controller(), "nodesEmptyLabel");
    assertFalse(headers.isVisible());
    assertTrue(empty.isVisible());
    assertEquals(StudioBundle.get("no_node_types_defined"), empty.getText());
  }

  // ---- Expansion strategy / Expansion Depths ----

  @Test
  void expansionDepthsAreOneRowEachWithTheRelationshipAndMaxDepth(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    TreeModel model = (TreeModel) item.getModel();
    ExpansionStrategy strategy = new ExpansionStrategy();
    strategy.setType(ExpansionStrategy.TREE);
    ExpansionDepth teamTeam = new ExpansionDepth();
    teamTeam.setRelationshipModel("TeamTeam_Re");
    teamTeam.setMaxDepth(5);
    ExpansionDepth teamPerson = new ExpansionDepth();
    teamPerson.setRelationshipModel("TeamPerson_Re");
    teamPerson.setMaxDepth(1);
    strategy.setExpansionDepths(new ArrayList<>(List.of(teamTeam, teamPerson)));
    model.getContent().getConfiguration().setExpansionStrategy(strategy);
    FxTestSupport.Loaded<TreeExpansionDepthsPanelController> loaded = load("tree-expansion-depths-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel(model));

    VBox rows = FxTestSupport.field(loaded.controller(), "depthRows");
    assertEquals(2, rows.getChildren().size());
    assertEquals("TeamTeam_Re", rowLabel(rows, 0, "#treeExpansionDepthRelationship-0"));
    assertEquals("5", rowLabel(rows, 0, "#treeExpansionDepthMaxDepth-0"));
    assertEquals("TeamPerson_Re", rowLabel(rows, 1, "#treeExpansionDepthRelationship-1"));
    assertEquals("1", rowLabel(rows, 1, "#treeExpansionDepthMaxDepth-1"));
    Label empty = FxTestSupport.field(loaded.controller(), "depthsEmptyLabel");
    assertFalse(empty.isVisible());
  }

  @Test
  void noExpansionDepthsShowsThePlaceholderInsteadOfTheHeader(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    FxTestSupport.Loaded<TreeExpansionDepthsPanelController> loaded = load("tree-expansion-depths-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel((TreeModel) item.getModel()));

    HBox headers = FxTestSupport.field(loaded.controller(), "depthHeaders");
    Label empty = FxTestSupport.field(loaded.controller(), "depthsEmptyLabel");
    assertFalse(headers.isVisible());
    assertTrue(empty.isVisible());
    assertEquals(StudioBundle.get("tree_expansion_depths_panel.none_defined"), empty.getText());
  }

  @Test
  void expansionStrategyOffersLevelByLevelAndTreeWithReadableLabels(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    FxTestSupport.Loaded<TreeConfigurationPanelController> loaded = load("tree-configuration-panel.fxml");
    FxTestSupport.onFx(() -> loaded.controller().setModel((TreeModel) item.getModel()));

    ComboBox<String> combo = FxTestSupport.field(loaded.controller(), "expansionStrategyField");
    assertEquals(List.of("level_by_level", "tree"), combo.getItems());
    assertEquals(StudioBundle.get("tree_configuration_panel.strategy_tree"), combo.getConverter().toString("tree"));
    assertEquals(StudioBundle.get("tree_configuration_panel.strategy_level_by_level"), combo.getConverter().toString("level_by_level"));
  }

  @Test
  void expansionDepthsAreOnlyShownWhileTheStrategyIsTree(@TempDir Path dir) throws Exception {
    ProjectItem item = selectTree(dir);
    ExpansionStrategy levelByLevel = new ExpansionStrategy();
    levelByLevel.setType(ExpansionStrategy.LEVEL_BY_LEVEL);
    ((TreeModel) item.getModel()).getContent().getConfiguration().setExpansionStrategy(levelByLevel);
    FxTestSupport.Loaded<TreeModelEditorController> loaded = FxTestSupport.load(EDITOR_FXML);
    for (String panel : List.of("rootPanelController", "columnsPanelController", "nodeTypesPanelController",
        "configurationPanelController", "expansionDepthsPanelController", "accessibilityPanelController", "stylesPanelController")) {
      panels.add(FxTestSupport.field(loaded.controller(), panel));
    }
    TreeConfigurationPanelController configuration = FxTestSupport.field(loaded.controller(), "configurationPanelController");
    TreeExpansionDepthsPanelController depths = FxTestSupport.field(loaded.controller(), "expansionDepthsPanelController");
    TitledPane depthsPane = FxTestSupport.field(depths, "root");
    FxTestSupport.onFx(() -> loaded.controller().loadModel(item.getModel()));
    ComboBox<String> combo = FxTestSupport.field(configuration, "expansionStrategyField");

    assertEquals("level_by_level", combo.getValue());
    assertFalse(depthsPane.isVisible());
    assertFalse(depthsPane.isManaged());

    FxTestSupport.onFx(() -> combo.setValue("tree"));
    assertTrue(depthsPane.isVisible());
    assertTrue(depthsPane.isManaged());
    assertEquals("tree", reloaded(item).getContent().getConfiguration().getExpansionStrategy().getType());

    FxTestSupport.onFx(() -> combo.setValue("level_by_level"));
    assertFalse(depthsPane.isVisible());
  }

  private static String rowLabel(VBox rows, int row, String selector) {
    return ((Label) rows.getChildren().get(row).lookup(selector)).getText();
  }

  /** The move-up and move-down buttons of a row: the two buttons inside the VBox of its actions box. */
  private static List<Button> moveButtons(VBox rows, int row) {
    HBox rowBox = (HBox) rows.getChildren().get(row);
    HBox actions = (HBox) rowBox.getChildren().get(rowBox.getChildren().size() - 1);
    VBox moveBox = (VBox) actions.getChildren().get(0);
    return moveBox.getChildren().stream().map(Button.class::cast).toList();
  }

  private static boolean errorShown(AbstractPropertyEditor panel) throws Exception {
    ErrorContainerController container = FxTestSupport.field(panel, "errorContainerController");
    return container.errorProperty().get();
  }

  private static String errorText(AbstractPropertyEditor panel) throws Exception {
    ErrorContainerController container = FxTestSupport.field(panel, "errorContainerController");
    Label label = FxTestSupport.field(container, "errorMessage");
    return label.getText();
  }
}
