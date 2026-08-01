package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnRef;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.overview.OverviewInitialSortingReferenceValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.overviewmodel.OverviewColumnOptions;
import de.a12.studio.ui.util.Icons;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;

/**
 * Edits an {@link OverviewModel}'s {@code content.configuration.initialSorting}: one draggable, reorderable
 * row per {@link ColumnRef}, each picking one of the columns defined in the Columns panel via an inline combo
 * box (order determines sort priority - only the first entry's sort direction icon is shown at runtime, per
 * SME). Not bound to a single {@link de.a12.studio.models.documentmodel.Element}, so it follows the
 * model-header pattern used by e.g. {@link OverviewColumnsPanelController}. {@link
 * OverviewInitialSortingReferenceValidator} flags entries left pointing at a column that was since deleted;
 * that error is queried directly (like {@link ModulesPanelController#refreshNameUniquenessError}) since there
 * is no single bound Element for the base class's own validation plumbing to key off of.
 */
public class OverviewSortingPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSorting().
  private static final DataFormat SORTING_INDEX = new DataFormat("application/x-a12-overview-sorting-index");

  @FXML
  private VBox sortingRows;

  @FXML
  private Label sortingEmptyLabel;

  private OverviewModel model;

  private ElementIndex documentModelIndex;

  // Set while a row's combo box is being repopulated from the model, so that isn't mistaken for a user edit.
  private boolean updatingFromModel;

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();
  }

  /** Re-points the column picker's "Field" summary at the currently referenced Document Model. */
  public void setDocumentModelIndex(ElementIndex documentModelIndex) {
    this.documentModelIndex = documentModelIndex;
    rebuildRows();
  }

  /** Called by the owning editor whenever the Columns panel changes, since this panel's picker choices and
   * its own dangling-reference validation both derive from the current column list. */
  public void refresh() {
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    ensureConfiguration().getInitialSorting().add(new ColumnRef());
    rebuildRows();
    commitHeaderChange();
  }

  private List<ColumnRef> getSorting() {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    return configuration != null ? configuration.getInitialSorting() : List.of();
  }

  private OverviewConfiguration ensureConfiguration() {
    if (model.getContent().getConfiguration() == null) {
      model.getContent().setConfiguration(new OverviewConfiguration());
    }
    return model.getContent().getConfiguration();
  }

  private List<Column> getColumns() {
    return model.getContent().getColumns();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    refreshValidationError();
    sortingRows.getChildren().clear();

    List<ColumnRef> sorting = getSorting();
    boolean empty = sorting.isEmpty();
    sortingEmptyLabel.setVisible(empty);
    sortingEmptyLabel.setManaged(empty);

    for (int index = 0; index < sorting.size(); index++) {
      sortingRows.getChildren().add(createRow(sorting.get(index), index, sorting.size()));
    }
  }

  private void refreshValidationError() {
    if (getSorting().isEmpty()) {
      hideError();
      return;
    }
    List<ModelValidationError> errors =
        Studio.getValidationService().validateElement(model, OverviewInitialSortingReferenceValidator.ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    } else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private HBox createRow(ColumnRef columnRef, int index, int rowCount) {
    List<Column> columns = getColumns();

    FontIcon dragHandle = new FontIcon(Icons.DRAG_HANDLE);
    dragHandle.setIconSize(18);
    dragHandle.getStyleClass().add("module-drag-handle");
    dragHandle.setCursor(Cursor.MOVE);

    ComboBox<String> columnField = new ComboBox<>();
    columnField.setId("overviewSortingColumn-" + index);
    columnField.setPromptText("Select a column");
    columnField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(columnField, Priority.ALWAYS);
    columnField.getItems().setAll(OverviewColumnOptions.columnIds(columns));
    OverviewColumnOptions.applyColumnConverter(columnField, columns, documentModelIndex);

    updatingFromModel = true;
    try {
      columnField.setValue(columnRef.getIdref());
    }
    finally {
      updatingFromModel = false;
    }
    columnField.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      columnRef.setIdref(newValue);
      commitHeaderChange();
      refreshValidationError();
    });

    HBox row = new HBox(10.0, dragHandle, columnField, createActionsBox(columnRef, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    setupDragAndDrop(row, dragHandle, index);
    return row;
  }

  // Only the drag handle initiates a drag (so using the combo box or action buttons doesn't start one); the
  // whole row is the drop target, so hovering anywhere over another row while dragging offers reordering there.
  private void setupDragAndDrop(HBox row, Node dragHandle, int index) {
    dragHandle.setOnDragDetected(event -> {
      Dragboard dragboard = dragHandle.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(SORTING_INDEX, String.valueOf(index));
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
      if (event.getDragboard().hasContent(SORTING_INDEX)) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, isAboveMidpoint(row, event.getY()));
      }
      event.consume();
    });
    row.setOnDragExited(event -> clearDropIndicator(row));
    row.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = dragboard.hasContent(SORTING_INDEX);
      if (success) {
        int insertBeforeIndex = isAboveMidpoint(row, event.getY()) ? index : index + 1;
        moveSorting(Integer.parseInt((String) dragboard.getContent(SORTING_INDEX)), insertBeforeIndex);
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

  // targetIndex is the position the moved entry should end up at, indexed into the list as it stood before
  // the drag started (e.g. "landed above the row currently at index 2" is targetIndex 2).
  private void moveSorting(int fromIndex, int targetIndex) {
    int insertIndex = fromIndex < targetIndex ? targetIndex - 1 : targetIndex;
    if (insertIndex == fromIndex) {
      return;
    }
    List<ColumnRef> sorting = getSorting();
    ColumnRef moved = sorting.remove(fromIndex);
    sorting.add(insertIndex, moved);
    rebuildRows();
    commitHeaderChange();
  }

  private HBox createActionsBox(ColumnRef columnRef, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      getSorting().remove(columnRef);
      rebuildRows();
      commitHeaderChange();
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, deleteButton);
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
    Collections.swap(getSorting(), fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
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
}
