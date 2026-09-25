package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.treemodel.ExpansionDepth;
import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.editors.treemodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Edits {@link ExpansionStrategy#getExpansionDepths()} - SME's "Expansion Depths", only shown while the
 * Expansion Strategy is "Tree" (the owning editor toggles that through {@link #setStrategyVisible}). One row
 * per {@link ExpansionDepth} summarizing its Relationship and Max Depth; clicking a row opens {@link
 * Dialogs#showExpansionDepthForEdit}, the Add button opens {@link Dialogs#showExpansionDepthForAdd}. The
 * relationships on offer are the ones used by the nodes' child relationship configurations, as in SME. Not
 * bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern
 * used by e.g. {@link TreeColumnsPanelController}. Order carries no meaning here, so unlike the other row
 * lists the rows aren't reorderable.
 */
public class TreeExpansionDepthsPanelController extends AbstractPropertyEditor {

  @FXML
  private HBox depthHeaders;

  @FXML
  private VBox depthRows;

  @FXML
  private Label depthsEmptyLabel;

  private TreeModel model;

  public void setModel(@NonNull TreeModel model) {
    this.model = model;
    rebuildRows();
  }

  /** Shows or hides the whole panel; it only applies to the "Tree" expansion strategy. */
  public void setStrategyVisible(boolean visible) {
    setEditorVisible(visible);
  }

  private List<ExpansionDepth> getDepths() {
    TreeConfiguration configuration = model.getContent().getConfiguration();
    if (configuration == null || configuration.getExpansionStrategy() == null || configuration.getExpansionStrategy().getExpansionDepths() == null) {
      return List.of();
    }
    return configuration.getExpansionStrategy().getExpansionDepths();
  }

  /** The mutable list, created (and the configuration/strategy with it) on first write. */
  private List<ExpansionDepth> ensureDepths() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new TreeConfiguration());
    }
    TreeConfiguration configuration = model.getContent().getConfiguration();
    if (configuration.getExpansionStrategy() == null) {
      configuration.setExpansionStrategy(new ExpansionStrategy());
    }
    ExpansionStrategy strategy = configuration.getExpansionStrategy();
    if (strategy.getExpansionDepths() == null) {
      strategy.setExpansionDepths(new ArrayList<>());
    }
    return strategy.getExpansionDepths();
  }

  /** The distinct relationship models used by the nodes' child relationship configurations, in model order. */
  private List<String> relationshipChoices() {
    List<String> relationships = new ArrayList<>();
    for (TreeNode node : model.getContent().getNodes()) {
      for (Object configuration : node.getChildRelationshipConfigurations()) {
        if (configuration instanceof Map<?, ?> map && map.get("relationshipModelRef") instanceof String ref
            && !ref.isBlank() && !relationships.contains(ref)) {
          relationships.add(ref);
        }
      }
    }
    return relationships;
  }

  @FXML
  private void onAdd() {
    Dialogs.showExpansionDepthForAdd(Studio.stage, relationshipChoices()).ifPresent(depth -> {
      ensureDepths().add(depth);
      rebuildRows();
      commitHeaderChange();
    });
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    depthRows.getChildren().clear();

    List<ExpansionDepth> depths = getDepths();
    boolean empty = depths.isEmpty();
    depthHeaders.setVisible(!empty);
    depthHeaders.setManaged(!empty);
    depthsEmptyLabel.setVisible(empty);
    depthsEmptyLabel.setManaged(empty);

    for (int index = 0; index < depths.size(); index++) {
      depthRows.getChildren().add(createRow(depths.get(index), index));
    }
  }

  private HBox createRow(ExpansionDepth depth, int index) {
    Label relationshipLabel = createRowLabel(depth.getRelationshipModel() != null ? depth.getRelationshipModel() : "", "treeExpansionDepthRelationship-" + index, depth);
    relationshipLabel.getStyleClass().add("path-text");
    relationshipLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(relationshipLabel, Priority.ALWAYS);

    Label maxDepthLabel = createRowLabel(depth.getMaxDepth() != null ? String.valueOf(depth.getMaxDepth()) : "", "treeExpansionDepthMaxDepth-" + index, depth);
    lockWidth(maxDepthLabel, 120.0);

    // Fixed widths here and in tree-expansion-depths-panel.fxml's header must stay in sync so labels sit above their values.
    HBox actionsBox = createActionsBox(depth);
    lockWidth(actionsBox, 80.0);
    HBox row = new HBox(10.0, relationshipLabel, maxDepthLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    return row;
  }

  private static void lockWidth(Region region, double width) {
    region.setMinWidth(width);
    region.setPrefWidth(width);
    region.setMaxWidth(width);
  }

  private Label createRowLabel(String text, String id, ExpansionDepth depth) {
    Label label = new Label(text);
    label.setId(id);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(depth);
      }
    });
    return label;
  }

  private void openEditDialog(ExpansionDepth depth) {
    Dialogs.showExpansionDepthForEdit(Studio.stage, relationshipChoices(), depth).ifPresent(edited -> {
      depth.setRelationshipModel(edited.getRelationshipModel());
      depth.setMaxDepth(edited.getMaxDepth());
      rebuildRows();
      commitHeaderChange();
    });
  }

  private HBox createActionsBox(ExpansionDepth depth) {
    Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("tree_expansion_depths_panel.edit_title"), () -> openEditDialog(depth));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("tree_expansion_depths_panel.delete_confirmation"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        ensureDepths().remove(depth);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }
}
