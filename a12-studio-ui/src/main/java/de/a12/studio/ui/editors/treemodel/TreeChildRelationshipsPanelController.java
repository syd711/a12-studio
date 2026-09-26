package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits the selected node type's {@code childRelationshipConfigurations} (SME "Child Relationship
 * Configurations"): one row per relationship that yields children of the node, showing the relationship model and
 * the role the node plays in it (Parent Role); a click on a row or its pencil opens {@link
 * Dialogs#showChildRelationshipForEdit}, which also holds the column mapping shown on the child nodes. Not bound
 * to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern. Every
 * change re-syncs the header's {@code modelReferences} ("relationship-model-for-tree", one per distinct
 * relationship over all node types) and is reported through {@link #setOnChange}, so the Root panel, whose
 * choices are these configurations, can refresh. Removing a configuration the Root points at clears the Root, as
 * in SME.
 */
public class TreeChildRelationshipsPanelController extends AbstractPropertyEditor {

  private static final DataFormat CONFIGURATION_INDEX = new DataFormat("application/x-a12-tree-child-relationship-index");

  // The move up/down + edit + delete buttons at the end of each row (3 * 34px buttons + 2 * 4px spacing).
  private static final double ACTIONS_BOX_WIDTH = 110.0;

  @FXML
  private HBox relationshipHeaders;

  @FXML
  private VBox relationshipRows;

  @FXML
  private Label emptyLabel;

  private TreeModel model;
  private ProjectItem projectItem;
  private TreeNode node;

  private Runnable onChange = () -> {
  };

  public void setModel(@NonNull TreeModel model, @NonNull ProjectItem projectItem) {
    this.model = model;
    this.projectItem = projectItem;
  }

  /** Binds the panel to {@code node}, or to nothing ({@code null}). */
  public void setNode(TreeNode node) {
    this.node = node;
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  private List<TreeChildRelationshipConfiguration> getConfigurations() {
    return node.getChildRelationshipConfigurations();
  }

  @FXML
  private void onAdd() {
    Dialogs.showChildRelationshipForAdd(Studio.stage, model, projectItem, node).ifPresent(configuration -> {
      getConfigurations().add(configuration);
      changed();
    });
  }

  private void openEditDialog(TreeChildRelationshipConfiguration configuration) {
    Dialogs.showChildRelationshipForEdit(Studio.stage, model, projectItem, node, configuration).ifPresent(edited -> {
      getConfigurations().set(getConfigurations().indexOf(configuration), edited);
      changed();
    });
  }

  private void changed() {
    rebuildRows();
    syncModelReferences();
    commitHeaderChange();
    onChange.run();
  }

  private void rebuildRows() {
    relationshipRows.getChildren().clear();
    if (node == null) {
      return;
    }

    List<TreeChildRelationshipConfiguration> configurations = getConfigurations();
    boolean empty = configurations.isEmpty();
    relationshipHeaders.setVisible(!empty);
    relationshipHeaders.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < configurations.size(); index++) {
      relationshipRows.getChildren().add(createRow(configurations.get(index), index, configurations.size()));
    }
  }

  private HBox createRow(TreeChildRelationshipConfiguration configuration, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label relationshipLabel = new Label(configuration.getRelationshipModelRef());
    relationshipLabel.setId("treeChildRelationshipModel-" + index);
    growEqually(relationshipLabel);
    makeClickableToEdit(relationshipLabel, configuration);

    Label parentRoleLabel = new Label(configuration.getParentRole());
    parentRoleLabel.setId("treeChildRelationshipParentRole-" + index);
    growEqually(parentRoleLabel);
    makeClickableToEdit(parentRoleLabel, configuration);

    HBox actionsBox = createActionsBox(configuration, index, rowCount);
    actionsBox.setMinWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setPrefWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMaxWidth(ACTIONS_BOX_WIDTH);

    HBox row = new HBox(10.0, dragHandle, relationshipLabel, parentRoleLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, CONFIGURATION_INDEX, index, this::moveViaDrag);
    return row;
  }

  // Growing columns share the free width equally only if their preferred widths are equal, in the header as in the
  // rows (see the matching header in the panel's fxml); otherwise the header labels drift away from the values.
  private static void growEqually(Label label) {
    label.setMinWidth(0.0);
    label.setPrefWidth(100.0);
    label.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(label, Priority.ALWAYS);
  }

  private void makeClickableToEdit(Label label, TreeChildRelationshipConfiguration configuration) {
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(configuration);
      }
    });
  }

  private void moveViaDrag(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getConfigurations(), fromIndex, insertBeforeIndex)) {
      changed();
    }
  }

  private HBox createActionsBox(TreeChildRelationshipConfiguration configuration, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("tree_node_action.edit"), () -> openEditDialog(configuration));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_child_relationship"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getConfigurations().remove(configuration);
        if (model.getContent().getConfiguration() != null && configuration.getId() != null
            && configuration.getId().equals(model.getContent().getConfiguration().getRootRef())) {
          model.getContent().getConfiguration().setRootRef(null);
        }
        changed();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getConfigurations(), fromIndex, toIndex);
    changed();
  }

  /** One header reference per distinct relationship model over all node types, purpose "relationship-model-for-tree". */
  private void syncModelReferences() {
    List<ModelReference> references = model.getModelReferences();
    references.removeIf(reference -> ModelReference.PURPOSE_RELATIONSHIP_MODEL_FOR_TREE.equals(reference.getPurpose()));
    List<String> seen = new ArrayList<>();
    int index = 1;
    for (TreeNode candidate : model.getContent().getNodes()) {
      for (TreeChildRelationshipConfiguration configuration : candidate.getChildRelationshipConfigurations()) {
        String relationship = configuration.getRelationshipModelRef();
        if (relationship == null || relationship.isBlank() || seen.contains(relationship)) {
          continue;
        }
        seen.add(relationship);
        ModelReference reference = new ModelReference();
        reference.setPurpose(ModelReference.PURPOSE_RELATIONSHIP_MODEL_FOR_TREE);
        reference.setModelType(ModelType.RELATIONSHIP);
        reference.setAlias("RM" + index++);
        reference.setReference(relationship);
        references.add(reference);
      }
    }
  }
}
