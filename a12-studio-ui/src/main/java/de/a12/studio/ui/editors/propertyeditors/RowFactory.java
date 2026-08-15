package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Point2D;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * Shared row-building blocks for the reorderable-list property editors in this package (e.g.
 * {@link de.a12.studio.ui.editors.applicationmodel.ModulesPanelController}, {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewColumnsPanelController}, {@link
 * de.a12.studio.ui.editors.overviewmodel.StylesPanelController}):
 * the drag handle icon, drag-and-drop reordering, the up/down move-buttons column, and the generic action
 * button used for edit/copy/delete. Every method is stateless; callers own their row list and re-render
 * (typically a {@code rebuildRows()}) plus persist (typically {@code commitChange()}) after a mutation.
 */
public final class RowFactory {

  private RowFactory() {
  }

  public static FontIcon createDragHandle() {
    FontIcon dragHandle = new FontIcon(Icons.DRAG_HANDLE);
    dragHandle.setIconSize(18);
    dragHandle.getStyleClass().add("module-drag-handle");
    dragHandle.setCursor(Cursor.MOVE);
    return dragHandle;
  }

  /**
   * Wires {@code dragHandle} as the sole drag source and {@code row} as the drop target for a row-reorder
   * drag identified by {@code indexFormat} (a {@link DataFormat} unique to the owning list, so drags from
   * unrelated row lists in the same window are rejected). The drop position is shown as an accent-colored
   * line on the row's top or bottom edge, depending on which half of the row the cursor is over. On drop,
   * invokes {@code onDrop} with the dragged row's original index and the target insertion index (before
   * which the row should land, as it stood prior to the drag) - the same convention {@link #reorder} expects.
   */
  public static void setupRowDragAndDrop(@NonNull HBox row, @NonNull Node dragHandle, @NonNull DataFormat indexFormat,
      int index, @NonNull BiConsumer<Integer, Integer> onDrop) {
    dragHandle.setOnDragDetected(event -> {
      Dragboard dragboard = dragHandle.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(indexFormat, String.valueOf(index));
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
      if (event.getDragboard().hasContent(indexFormat)) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, isAboveMidpoint(row, event.getY()));
      }
      event.consume();
    });
    row.setOnDragExited(event -> clearDropIndicator(row));
    row.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = dragboard.hasContent(indexFormat);
      if (success) {
        int insertBeforeIndex = isAboveMidpoint(row, event.getY()) ? index : index + 1;
        onDrop.accept(Integer.parseInt((String) dragboard.getContent(indexFormat)), insertBeforeIndex);
      }
      clearDropIndicator(row);
      event.setDropCompleted(success);
      event.consume();
    });
  }

  /**
   * Same as {@link #setupRowDragAndDrop}, but for a drag that may be accepted by a different container than
   * the one it started in - e.g. {@link ToolbarButtonsPanelController} moving a button row between its own
   * Major/Minor and Subheader/Footer panel instances. {@code format} is shared by every such container
   * instance (instead of being unique per instance, as {@link #setupRowDragAndDrop}'s {@code indexFormat} is),
   * so a drop anywhere among them can accept the drag; {@code payload} - opaque to this method - is whatever
   * the caller needs to resolve the drag back to its source container and row (e.g. {@code sourceId + ":" +
   * index}). {@code onDrop} receives that payload back together with the insertion index into {@code row}'s
   * own list, exactly as {@link #reorder} expects; the caller is responsible for parsing the payload to decide
   * whether the drop is a same-container reorder or a cross-container move.
   */
  public static void setupCrossContainerRowDragAndDrop(@NonNull HBox row, @NonNull Node dragHandle, @NonNull DataFormat format,
      @NonNull String payload, int index, @NonNull BiConsumer<String, Integer> onDrop) {
    dragHandle.setOnDragDetected(event -> {
      Dragboard dragboard = dragHandle.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(format, payload);
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
      if (event.getDragboard().hasContent(format)) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, isAboveMidpoint(row, event.getY()));
      }
      event.consume();
    });
    row.setOnDragExited(event -> clearDropIndicator(row));
    row.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = dragboard.hasContent(format);
      if (success) {
        int insertBeforeIndex = isAboveMidpoint(row, event.getY()) ? index : index + 1;
        onDrop.accept((String) dragboard.getContent(format), insertBeforeIndex);
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

  /**
   * Moves the element at {@code fromIndex} so it lands just before {@code insertBeforeIndex}, both indexed
   * into {@code list} as it stood before the move - the convention {@link #setupRowDragAndDrop} passes to its
   * {@code onDrop} callback. Returns whether the list actually changed (false for a no-op drop onto itself),
   * so callers know whether to re-render and persist.
   */
  public static <T> boolean reorder(@NonNull List<T> list, int fromIndex, int insertBeforeIndex) {
    int insertIndex = fromIndex < insertBeforeIndex ? insertBeforeIndex - 1 : insertBeforeIndex;
    if (insertIndex == fromIndex) {
      return false;
    }
    T moved = list.remove(fromIndex);
    list.add(insertIndex, moved);
    return true;
  }

  public static Button createActionButton(@NonNull String iconLiteral, @NonNull String tooltip, @NonNull Runnable action) {
    FontIcon icon = new FontIcon(iconLiteral);
    icon.setIconSize(16);
    icon.getStyleClass().add("toolbar-icon");

    Button button = new Button();
    button.getStyleClass().add("default-button");
    button.setGraphic(icon);
    button.setTooltip(WidgetFactory.createTooltip(tooltip));
    button.setOnAction(event -> action.run());
    return button;
  }

  /**
   * Move up/down stacked in a VBox instead of side by side in an HBox: each button is half-height (see the
   * "move-button" style class), so the pair together takes up the same width/height as a single normal
   * button. {@code onMove} is invoked with (fromIndex, toIndex) - an adjacent swap, per {@link
   * java.util.Collections#swap} - when either arrow is pressed; disabled at the ends of the list.
   */
  public static VBox createMoveButtonsBox(int index, int rowCount, @NonNull BiConsumer<Integer, Integer> onMove) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> onMove.accept(index, index - 1));
    moveUpButton.setDisable(index == 0);
    moveUpButton.getStyleClass().addAll("move-button", "move-button-top");

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> onMove.accept(index, index + 1));
    moveDownButton.setDisable(index == rowCount - 1);
    moveDownButton.getStyleClass().addAll("move-button", "move-button-bottom");

    return new VBox(1, moveUpButton, moveDownButton);
  }
}
