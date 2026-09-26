package de.a12.studio.ui.editors.treemodel;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeActionGroup;
import de.a12.studio.models.treemodel.TreeNodeContextMenu;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Edits the selected node type's {@code contextMenu} (SME "Context Menu"): one draggable, reorderable row per named
 * group, summarizing its Group Name and Actions; a click on a row or its pencil opens {@link
 * Dialogs#showContextMenuGroupForEdit}, the Add button {@link Dialogs#showContextMenuGroupForAdd}. Not bound to a
 * single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern. The node's
 * {@code contextMenu} is only created with its first group and dropped again with its last, since SME treats a
 * node without groups as having no context menu.
 */
public class TreeNodeContextMenuPanelController extends AbstractPropertyEditor {

  private static final DataFormat GROUP_INDEX = new DataFormat("application/x-a12-tree-context-menu-group-index");

  // The move up/down + edit + delete buttons at the end of each row (3 * 34px buttons + 2 * 4px spacing).
  private static final double ACTIONS_BOX_WIDTH = 110.0;

  @FXML
  private HBox groupHeaders;

  @FXML
  private VBox groupRows;

  @FXML
  private Label emptyLabel;

  private ProjectItem projectItem;
  private TreeNode node;

  public void setProjectItem(@NonNull ProjectItem projectItem) {
    this.projectItem = projectItem;
  }

  /** Binds the panel to {@code node}, or to nothing ({@code null}). */
  public void setNode(TreeNode node) {
    this.node = node;
    rebuildRows();
  }

  private List<TreeNodeActionGroup> getGroups() {
    TreeNodeContextMenu contextMenu = node.getContextMenu();
    return contextMenu != null ? contextMenu.getGroups() : List.of();
  }

  @FXML
  private void onAdd() {
    Dialogs.showContextMenuGroupForAdd(Studio.stage, projectItem).ifPresent(group -> {
      if (node.getContextMenu() == null) {
        node.setContextMenu(new TreeNodeContextMenu());
      }
      node.getContextMenu().getGroups().add(group);
      changed();
    });
  }

  private void openEditDialog(TreeNodeActionGroup group) {
    Dialogs.showContextMenuGroupForEdit(Studio.stage, projectItem, group).ifPresent(edited -> {
      getGroups().set(getGroups().indexOf(group), edited);
      changed();
    });
  }

  private void changed() {
    if (node.getContextMenu() != null && node.getContextMenu().getGroups().isEmpty()) {
      node.setContextMenu(null);
    }
    rebuildRows();
    commitHeaderChange();
  }

  private void rebuildRows() {
    groupRows.getChildren().clear();
    if (node == null) {
      return;
    }

    List<TreeNodeActionGroup> groups = getGroups();
    boolean empty = groups.isEmpty();
    groupHeaders.setVisible(!empty);
    groupHeaders.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < groups.size(); index++) {
      groupRows.getChildren().add(createRow(groups.get(index), index, groups.size()));
    }
  }

  private HBox createRow(TreeNodeActionGroup group, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label nameLabel = new Label(group.getName() != null ? group.getName() : "");
    nameLabel.setId("treeContextMenuGroupName-" + index);
    growEqually(nameLabel);
    makeClickableToEdit(nameLabel, group);

    Label actionsLabel = new Label(group.getActions().stream()
        .map(TreeNodeActionsPanelController::describe)
        .filter(text -> !text.isBlank())
        .collect(Collectors.joining(", ")));
    actionsLabel.setId("treeContextMenuGroupActions-" + index);
    growEqually(actionsLabel);
    actionsLabel.setWrapText(true);
    makeClickableToEdit(actionsLabel, group);

    HBox actionsBox = createActionsBox(group, index, rowCount);
    actionsBox.setMinWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setPrefWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMaxWidth(ACTIONS_BOX_WIDTH);

    HBox row = new HBox(10.0, dragHandle, nameLabel, actionsLabel, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, GROUP_INDEX, index, this::moveViaDrag);
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

  private void makeClickableToEdit(Label label, TreeNodeActionGroup group) {
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(group);
      }
    });
  }

  private void moveViaDrag(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getGroups(), fromIndex, insertBeforeIndex)) {
      changed();
    }
  }

  private HBox createActionsBox(TreeNodeActionGroup group, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("tree_node_action.edit"), () -> openEditDialog(group));

    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_context_menu_group"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getGroups().remove(group);
        changed();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getGroups(), fromIndex, toIndex);
    changed();
  }
}
