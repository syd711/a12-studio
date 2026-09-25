package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.FxTestSupport;
import de.a12.studio.ui.util.StudioBundle;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * The Tree Model editor's tabs and property editors: Root, Columns and Node Types on the first tab, the
 * Accessibility and Styles editors on the new Layout tab, and the Node Types list's rows.
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

    assertEquals(List.of(StudioBundle.get("columns"), StudioBundle.get("configuration"), StudioBundle.get("layout")),
        tabs.getTabs().stream().map(Tab::getText).toList(),
        "the former Nodes tab is gone; Layout is added");
    assertEquals(List.of(StudioBundle.get("tree_root"), StudioBundle.get("columns"), StudioBundle.get("node_types")),
        paneTitles(tabs.getTabs().get(0)));
    assertEquals(List.of(StudioBundle.get("accessibility"), StudioBundle.get("styles")), paneTitles(tabs.getTabs().get(2)));

    for (String panel : List.of("rootPanelController", "columnsPanelController", "nodeTypesPanelController",
        "configurationPanelController", "accessibilityPanelController", "stylesPanelController")) {
      AbstractPropertyEditor controller = FxTestSupport.field(loaded.controller(), panel);
      assertNotNull(controller, panel + " must be injected");
      panels.add(controller);
    }
  }

  private static List<String> paneTitles(Tab tab) {
    VBox box = (VBox) ((ScrollPane) tab.getContent()).getContent();
    return box.getChildren().stream().map(node -> ((TitledPane) node).getText()).toList();
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
