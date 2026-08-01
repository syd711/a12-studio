package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.OverviewColumnOptions;
import de.a12.studio.ui.editors.overviewmodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.UUID;

/**
 * Edits an {@link OverviewModel}'s {@code content.columns}: one draggable, reorderable row per {@link
 * Column}, summarizing its Field (the referenced Document Model element, resolved via {@link
 * OverviewColumnOptions}), Sortable, Width and Pin Direction. Not bound to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by e.g.
 * {@link OverviewFeaturesPanelController}. Clicking a row opens {@link Dialogs#showColumn}, which is
 * intentionally empty for now (no fields yet) - the full column editor is a follow-up. Also edits two
 * {@link OverviewConfiguration} flags displayed alongside the column list: Enable Columns Resize and Show
 * Number Of Entries (moved here from {@link OverviewFeaturesPanelController} since both are about how the
 * resulting table of columns is presented).
 */
public class OverviewColumnsPanelController extends AbstractPropertyEditor implements Initializable {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getColumns().
  private static final DataFormat COLUMN_INDEX = new DataFormat("application/x-a12-overview-column-index");

  @FXML
  private HBox columnHeaders;

  @FXML
  private VBox columnRows;

  @FXML
  private Label columnsEmptyLabel;

  @FXML
  private CheckBox enableColumnsResizeField;

  @FXML
  private CheckBox showRowCountField;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  // Set while enableColumnsResizeField/showRowCountField are being repopulated from the model, so those
  // programmatic updates aren't mistaken for user edits and don't trigger a save.
  private boolean updatingFromModel;

  // Notified after every structural change (add/reorder/delete), so the owning editor can keep sibling
  // panels whose choices derive from this list (e.g. the Sorting panel's column picker) in sync.
  private Runnable onChange = () -> {
  };

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    enableColumnsResizeField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setEnableColumnsResize(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
    showRowCountField.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel || model == null) {
        return;
      }
      ensureConfiguration().setShowRowCount(newValue ? Boolean.TRUE : null);
      commitHeaderChange();
    });
  }

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();

    updatingFromModel = true;
    try {
      OverviewConfiguration configuration = model.getContent().getConfiguration();
      enableColumnsResizeField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getEnableColumnsResize()));
      showRowCountField.setSelected(configuration != null && Boolean.TRUE.equals(configuration.getShowRowCount()));
    }
    finally {
      updatingFromModel = false;
    }
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  /** Re-points the "Field" summary of every row at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    rebuildRows();
  }

  public void setOnChange(@NonNull Runnable onChange) {
    this.onChange = onChange;
  }

  @FXML
  private void onAdd() {
    Column column = new Column();
    column.setId("column-" + shortId());
    column.setWidth(1.0);
    getColumns().add(column);
    rebuildRows();
    notifyChanged();
  }

  private List<Column> getColumns() {
    return model.getContent().getColumns();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    columnRows.getChildren().clear();

    List<Column> columns = getColumns();
    boolean empty = columns.isEmpty();
    columnHeaders.setVisible(!empty);
    columnHeaders.setManaged(!empty);
    columnsEmptyLabel.setVisible(empty);
    columnsEmptyLabel.setManaged(empty);

    for (int index = 0; index < columns.size(); index++) {
      columnRows.getChildren().add(createRow(columns.get(index), index, columns.size()));
    }
  }

  private HBox createRow(Column column, int index, int rowCount) {
    FontIcon dragHandle = new FontIcon(Icons.DRAG_HANDLE);
    dragHandle.setIconSize(18);
    dragHandle.getStyleClass().add("module-drag-handle");
    dragHandle.setCursor(Cursor.MOVE);

    Label fieldLabel = createRowLabel(fieldSummary(column), "overviewColumnField-" + index, 200.0, column);
    Label sortableLabel = createRowLabel(Boolean.TRUE.equals(column.getSortable()) ? "Yes" : "No", "overviewColumnSortable-" + index, 70.0, column);
    Label widthLabel = createRowLabel(column.getWidth() != null ? String.valueOf(column.getWidth()) : "", "overviewColumnWidth-" + index, 70.0, column);
    Label pinDirectionLabel = createRowLabel(column.getPinDirection() != null ? column.getPinDirection() : "", "overviewColumnPinDirection-" + index, 100.0, column);

    HBox row = new HBox(10.0, dragHandle, fieldLabel, sortableLabel, widthLabel, pinDirectionLabel, createActionsBox(column, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    setupDragAndDrop(row, dragHandle, index);
    return row;
  }

  private Label createRowLabel(String text, String id, double width, Column column) {
    Label label = new Label(text);
    label.setId(id);
    label.setPrefWidth(width);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(column);
      }
    });
    return label;
  }

  private String fieldSummary(Column column) {
    return OverviewColumnOptions.describe(column, documentModelIndex);
  }

  private void openEditDialog(Column column) {
    Dialogs.showColumn(Studio.stage, documentModelIndex, column);
    rebuildRows();
  }

  // Only the drag handle initiates a drag (so clicking a label or the action buttons doesn't start one); the
  // whole row is the drop target, so hovering anywhere over another row while dragging offers reordering there.
  private void setupDragAndDrop(HBox row, Node dragHandle, int index) {
    dragHandle.setOnDragDetected(event -> {
      Dragboard dragboard = dragHandle.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(COLUMN_INDEX, String.valueOf(index));
      dragboard.setContent(content);

      SnapshotParameters snapshotParams = new SnapshotParameters();
      snapshotParams.setFill(Color.TRANSPARENT);
      Point2D cursorInRow = dragHandle.localToParent(event.getX(), event.getY());
      dragboard.setDragView(row.snapshot(snapshotParams, null), cursorInRow.getX(), cursorInRow.getY());

      row.getStyleClass().add("module-row-dragging");
      event.consume();
    });
    dragHandle.setOnDragDone(event -> row.getStyleClass().remove("module-row-dragging"));

    row.setOnDragOver(event -> {
      if (event.getDragboard().hasContent(COLUMN_INDEX)) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, isAboveMidpoint(row, event.getY()));
      }
      event.consume();
    });
    row.setOnDragExited(event -> clearDropIndicator(row));
    row.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = dragboard.hasContent(COLUMN_INDEX);
      if (success) {
        int insertBeforeIndex = isAboveMidpoint(row, event.getY()) ? index : index + 1;
        moveColumn(Integer.parseInt((String) dragboard.getContent(COLUMN_INDEX)), insertBeforeIndex);
      }
      clearDropIndicator(row);
      event.setDropCompleted(success);
      event.consume();
    });
  }

  private static boolean isAboveMidpoint(HBox row, double dragY) {
    return dragY < row.getHeight() / 2;
  }

  private static void showDropIndicator(HBox row, boolean above) {
    String showClass = above ? "module-row-drop-above" : "module-row-drop-below";
    String hideClass = above ? "module-row-drop-below" : "module-row-drop-above";
    row.getStyleClass().remove(hideClass);
    if (!row.getStyleClass().contains(showClass)) {
      row.getStyleClass().add(showClass);
    }
  }

  private static void clearDropIndicator(HBox row) {
    row.getStyleClass().removeAll("module-row-drop-above", "module-row-drop-below");
  }

  // targetIndex is the position the moved column should end up at, indexed into the list as it stood before
  // the drag started (e.g. "landed above the row currently at index 2" is targetIndex 2).
  private void moveColumn(int fromIndex, int targetIndex) {
    int insertIndex = fromIndex < targetIndex ? targetIndex - 1 : targetIndex;
    if (insertIndex == fromIndex) {
      return;
    }
    List<Column> columns = getColumns();
    Column moved = columns.remove(fromIndex);
    columns.add(insertIndex, moved);
    rebuildRows();
    notifyChanged();
  }

  private HBox createActionsBox(Column column, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(column));

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this column?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getColumns().remove(column);
        rebuildRows();
        notifyChanged();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  // Move up/down stacked in a VBox instead of side by side in the HBox: each button is half-height (see the
  // "move-button" style class), so the pair together takes up the same width/height as a single normal button.
  private VBox createMoveButtonsBox(int index, int rowCount) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> moveRow(index, index - 1));
    moveUpButton.setDisable(index == 0);
    moveUpButton.getStyleClass().addAll("move-button", "move-button-top");

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> moveRow(index, index + 1));
    moveDownButton.setDisable(index == rowCount - 1);
    moveDownButton.getStyleClass().addAll("move-button", "move-button-bottom");

    return new VBox(1, moveUpButton, moveDownButton);
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(getColumns(), fromIndex, toIndex);
    rebuildRows();
    notifyChanged();
  }

  private void notifyChanged() {
    commitHeaderChange();
    onChange.run();
  }

  private static Button createActionButton(String iconLiteral, String tooltip, Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(new Tooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }

  private static String shortId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 5);
  }
}
