package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.InitialExpansion;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link ExpansionStrategy#getInitialExpansion()} - SME's "Initial Expansion", only shown while the Expansion
 * Strategy is "Level by level" (the owning editor toggles that through {@link #setStrategyVisible}). Like SME, "Enable
 * Initial Expansion" isn't stored: it is on exactly when an initial expansion is present, and unchecking it drops the
 * Type, Number Of Levels and node types. The Type is "Expand all possible nodes" ({@link InitialExpansion#ALL_LEVELS},
 * the default) or "Expand by a pre-defined level" ({@link InitialExpansion#LEVEL_LIMIT}, which also asks for the Number
 * Of Levels). "Node Types To Apply" is one row per node type - unique, chosen from the tree's node types - and an empty
 * list means all of them; it is stored as absent then. Call {@link #refresh()} after the node types changed, which the
 * rows show. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header
 * pattern.
 */
public class TreeInitialExpansionPanelController extends AbstractPropertyEditor implements Initializable {

  private static final int DEFAULT_LEVELS = 1;

  @FXML
  private CheckBox enableField;

  @FXML
  private VBox detailsBox;

  @FXML
  private ComboBox<String> typeField;

  @FXML
  private VBox levelsBox;

  @FXML
  private Spinner<Integer> levelsField;

  @FXML
  private Label nodeTypesInfoIcon;

  @FXML
  private VBox nodeRefRows;

  @FXML
  private Label noNodeTypesLabel;

  @FXML
  private Button addNodeTypeButton;

  private TreeModel model;

  // Set while the fields are being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    WidgetFactory.createHelpIcon(nodeTypesInfoIcon, StudioBundle.get("tree_initial_expansion_panel.node_types_info"));
    typeField.getItems().setAll(InitialExpansion.ALL_LEVELS, InitialExpansion.LEVEL_LIMIT);
    typeField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String type) {
        if (type == null) {
          return null;
        }
        return switch (type) {
          case InitialExpansion.ALL_LEVELS -> StudioBundle.get("tree_initial_expansion_panel.type_all_levels");
          case InitialExpansion.LEVEL_LIMIT -> StudioBundle.get("tree_initial_expansion_panel.type_level_limit");
          default -> type;
        };
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    levelsField.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, Integer.MAX_VALUE, DEFAULT_LEVELS));
    WidgetFactory.restrictToNumericInput(levelsField.getEditor());

    enableField.selectedProperty().addListener((observable, oldValue, enabled) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      if (enabled) {
        InitialExpansion initialExpansion = new InitialExpansion();
        initialExpansion.setType(InitialExpansion.ALL_LEVELS);
        ensureStrategy().setInitialExpansion(initialExpansion);
      }
      else {
        ensureStrategy().setInitialExpansion(null);
      }
      refresh();
      commitHeaderChange();
    });
    typeField.valueProperty().addListener((observable, oldValue, type) -> {
      InitialExpansion initialExpansion = current();
      if (updatingFromModel || initialExpansion == null || type == null) {
        return;
      }
      initialExpansion.setType(type);
      initialExpansion.setLevel(InitialExpansion.LEVEL_LIMIT.equals(type) ? Integer.valueOf(DEFAULT_LEVELS) : null);
      refresh();
      commitHeaderChange();
    });
    levelsField.valueProperty().addListener((observable, oldValue, levels) -> {
      InitialExpansion initialExpansion = current();
      if (updatingFromModel || initialExpansion == null || levels == null
          || !InitialExpansion.LEVEL_LIMIT.equals(initialExpansion.getType())) {
        return;
      }
      initialExpansion.setLevel(levels);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull TreeModel model) {
    this.model = model;
    refresh();
  }

  /** Shows or hides the whole panel; it only applies to the "Level by level" expansion strategy. */
  public void setStrategyVisible(boolean visible) {
    setEditorVisible(visible);
  }

  /** Re-reads everything from the model, e.g. after the strategy or the node types changed. */
  public void refresh() {
    if (model == null) {
      return;
    }
    updatingFromModel = true;
    try {
      InitialExpansion initialExpansion = current();
      boolean enabled = initialExpansion != null;
      enableField.setSelected(enabled);
      detailsBox.setVisible(enabled);
      detailsBox.setManaged(enabled);

      boolean levelLimit = enabled && InitialExpansion.LEVEL_LIMIT.equals(initialExpansion.getType());
      typeField.setValue(enabled ? initialExpansion.getType() : null);
      levelsBox.setVisible(levelLimit);
      levelsBox.setManaged(levelLimit);
      levelsField.getValueFactory().setValue(levelLimit && initialExpansion.getLevel() != null ? initialExpansion.getLevel() : DEFAULT_LEVELS);

      rebuildNodeRefRows(enabled ? refs(initialExpansion) : List.of());
    }
    finally {
      updatingFromModel = false;
    }
  }

  private InitialExpansion current() {
    TreeConfiguration configuration = model != null ? model.getContent().getConfiguration() : null;
    return configuration != null && configuration.getExpansionStrategy() != null
        ? configuration.getExpansionStrategy().getInitialExpansion() : null;
  }

  private static List<String> refs(InitialExpansion initialExpansion) {
    return initialExpansion.getAffectedNodeRefs() != null ? initialExpansion.getAffectedNodeRefs() : List.of();
  }

  private ExpansionStrategy ensureStrategy() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    ExpansionStrategy strategy = model.getContent().getConfiguration().getOrCreateExpansionStrategy();
    if (strategy.getType() == null) {
      // The strategy shown for a tree without one; this panel only exists for it.
      strategy.setType(ExpansionStrategy.LEVEL_BY_LEVEL);
    }
    return strategy;
  }

  private void rebuildNodeRefRows(List<String> refs) {
    nodeRefRows.getChildren().clear();
    for (int index = 0; index < refs.size(); index++) {
      nodeRefRows.getChildren().add(createNodeRefRow(refs, index));
    }
    noNodeTypesLabel.setVisible(refs.isEmpty());
    noNodeTypesLabel.setManaged(refs.isEmpty());
    addNodeTypeButton.setDisable(firstUnusedNodeId(refs) == null);
  }

  private HBox createNodeRefRow(List<String> refs, int index) {
    String ref = refs.get(index);
    ComboBox<String> nodeTypeField = new ComboBox<>();
    nodeTypeField.setId("initialExpansionNodeType-" + index);
    nodeTypeField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nodeTypeField, Priority.ALWAYS);
    // A node type is listed once: the ones the other rows use are not on offer. A reference to a node type that no
    // longer exists stays selectable, so it is shown (and can be replaced) instead of being dropped.
    List<String> choices = new ArrayList<>();
    for (TreeNode node : model.getContent().getNodes()) {
      if (node.getId() != null && (node.getId().equals(ref) || !refs.contains(node.getId()))) {
        choices.add(node.getId());
      }
    }
    if (ref != null && !choices.contains(ref)) {
      choices.add(ref);
    }
    nodeTypeField.getItems().setAll(choices);
    nodeTypeField.setConverter(new StringConverter<>() {
      @Override
      public String toString(String nodeId) {
        return nodeId == null ? null : nodeTypeLabel(nodeId);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    nodeTypeField.setValue(ref);
    nodeTypeField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || newValue == null) {
        return;
      }
      List<String> stored = current().getAffectedNodeRefs();
      stored.set(index, newValue);
      refresh();
      commitHeaderChange();
    });

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> removeNodeRef(index));

    HBox row = new HBox(10.0, nodeTypeField, deleteButton);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  /** The node type as the node types list shows it: its Document Model; the id when it has none (or is gone). */
  private String nodeTypeLabel(String nodeId) {
    return model.getContent().getNodes().stream()
        .filter(node -> nodeId.equals(node.getId()) && node.getDocumentModelRef() != null && !node.getDocumentModelRef().isBlank())
        .map(TreeNode::getDocumentModelRef)
        .findFirst()
        .orElse(nodeId);
  }

  private String firstUnusedNodeId(List<String> refs) {
    return model.getContent().getNodes().stream()
        .map(TreeNode::getId)
        .filter(nodeId -> nodeId != null && !refs.contains(nodeId))
        .findFirst()
        .orElse(null);
  }

  @FXML
  private void onAddNodeType() {
    InitialExpansion initialExpansion = current();
    if (initialExpansion == null) {
      return;
    }
    String nodeId = firstUnusedNodeId(refs(initialExpansion));
    if (nodeId == null) {
      return;
    }
    if (initialExpansion.getAffectedNodeRefs() == null) {
      initialExpansion.setAffectedNodeRefs(new ArrayList<>());
    }
    initialExpansion.getAffectedNodeRefs().add(nodeId);
    refresh();
    commitHeaderChange();
  }

  private void removeNodeRef(int index) {
    InitialExpansion initialExpansion = current();
    initialExpansion.getAffectedNodeRefs().remove(index);
    // No node types means all of them, which SME writes as an absent key.
    if (initialExpansion.getAffectedNodeRefs().isEmpty()) {
      initialExpansion.setAffectedNodeRefs(null);
    }
    refresh();
    commitHeaderChange();
  }
}
