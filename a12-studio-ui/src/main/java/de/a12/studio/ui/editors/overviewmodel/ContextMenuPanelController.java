package de.a12.studio.ui.editors.overviewmodel;

import de.a12.studio.models.overviewmodel.ActionGroup;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ContextMenu;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
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
 * Edits an {@link OverviewModel}'s {@code content.contextMenu}: one draggable, reorderable row per named {@link
 * ActionGroup}, summarizing its Group Name and Actions - matching the SME reference's Context Menu section
 * ("similar to adding Row Actions, but without Priority, Destructive, and Hide Label"). Not bound to a single
 * {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern ({@link
 * #commitHeaderChange()}) used by e.g. {@link OverviewColumnsPanelController}. Clicking a row (or its Edit
 * button) opens {@link Dialogs#showContextMenuGroupForEdit}, the full group editor (name, multilingual title,
 * actions); the Add button opens the same editor via {@link Dialogs#showContextMenuGroupForAdd}, only adding the
 * new group to {@code content.contextMenu.groups} once it's confirmed.
 */
public class ContextMenuPanelController extends AbstractPropertyEditor {

  private static final double ACTIONS_BOX_WIDTH = 100.0;

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getGroups().
  private static final DataFormat GROUP_INDEX = new DataFormat("application/x-a12-context-menu-group-index");

  @FXML
  private HBox groupColumnHeaders;

  @FXML
  private VBox groupRows;

  @FXML
  private Label groupsEmptyLabel;

  private OverviewModel model;

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAddGroup() {
    Dialogs.showContextMenuGroupForAdd(Studio.stage, model).ifPresent(group -> {
      ensureContextMenu().getGroups().add(group);
      rebuildRows();
      commitHeaderChange();
    });
  }

  private List<ActionGroup> getGroups() {
    ContextMenu contextMenu = model.getContent().getContextMenu();
    return contextMenu != null ? contextMenu.getGroups() : List.of();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    groupRows.getChildren().clear();

    List<ActionGroup> groups = getGroups();
    boolean empty = groups.isEmpty();
    groupColumnHeaders.setVisible(!empty);
    groupColumnHeaders.setManaged(!empty);
    groupsEmptyLabel.setVisible(empty);
    groupsEmptyLabel.setManaged(empty);

    for (int index = 0; index < groups.size(); index++) {
      groupRows.getChildren().add(createRow(groups.get(index), index, groups.size()));
    }
  }

  private HBox createRow(ActionGroup group, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label nameCell = new Label(groupName(group));
    nameCell.setId("contextMenuGroupName-" + index);
    nameCell.setMaxWidth(Double.MAX_VALUE);
    makeClickableToEdit(nameCell, group);

    Label actionsCell = new Label(actionsSummary(group));
    actionsCell.setId("contextMenuGroupActions-" + index);
    actionsCell.setMaxWidth(Double.MAX_VALUE);
    actionsCell.setWrapText(true);
    makeClickableToEdit(actionsCell, group);

    GridPane contentGrid = new GridPane();
    contentGrid.setHgap(10.0);
    contentGrid.setMaxWidth(Double.MAX_VALUE);
    ColumnConstraints nameColumn = new ColumnConstraints();
    nameColumn.setPercentWidth(40.0);
    ColumnConstraints actionsColumn = new ColumnConstraints();
    actionsColumn.setPercentWidth(60.0);
    contentGrid.getColumnConstraints().addAll(nameColumn, actionsColumn);
    contentGrid.add(nameCell, 0, 0);
    contentGrid.add(actionsCell, 1, 0);
    HBox.setHgrow(contentGrid, Priority.ALWAYS);

    HBox actionsBox = createActionsBox(group, index, rowCount);
    actionsBox.setPrefWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMinWidth(ACTIONS_BOX_WIDTH);
    actionsBox.setMaxWidth(ACTIONS_BOX_WIDTH);
    HBox.setHgrow(actionsBox, Priority.NEVER);

    HBox row = new HBox(10.0, dragHandle, contentGrid, actionsBox);
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    RowFactory.setupRowDragAndDrop(row, dragHandle, GROUP_INDEX, index, this::moveGroup);
    return row;
  }

  private void makeClickableToEdit(Node node, ActionGroup group) {
    node.setCursor(Cursor.HAND);
    node.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(group);
      }
    });
  }

  private void openEditDialog(ActionGroup group) {
    if (Dialogs.showContextMenuGroupForEdit(Studio.stage, model, group)) {
      rebuildRows();
      commitHeaderChange();
    }
  }

  private void moveGroup(int fromIndex, int insertBeforeIndex) {
    if (RowFactory.reorder(getGroups(), fromIndex, insertBeforeIndex)) {
      rebuildRows();
      commitHeaderChange();
    }
  }

  private HBox createActionsBox(ActionGroup group, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    javafx.scene.control.Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(group));

    javafx.scene.control.Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("delete"), () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_context_menu_group"), null, null, StudioBundle.get("delete"));
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getGroups().remove(group);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getGroups(), fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
  }

  private ContextMenu ensureContextMenu() {
    if (model.getContent().getContextMenu() == null) {
      model.getContent().setContextMenu(new ContextMenu());
    }
    return model.getContent().getContextMenu();
  }

  private static String groupName(ActionGroup group) {
    return group.getName() != null ? group.getName() : "";
  }

  private static String actionsSummary(ActionGroup group) {
    return group.getActions().stream()
        .map(Button::getEvent)
        .filter(event -> event != null && !event.isBlank())
        .collect(Collectors.joining(", "));
  }
}
