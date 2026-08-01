package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.mappingmodel.dialogs.Dialogs;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Edits a {@link MappingModel}'s {@code content.Source}: one draggable, reorderable row per {@link
 * MappingSource}, summarizing its Name, Model, Repetitions and Skip Document Validation. Not bound to a
 * single {@link de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern used by
 * e.g. {@link OverviewColumnsPanelController}. Clicking a row opens {@link Dialogs#showSourceModel}, which is
 * intentionally empty for now (no fields yet) - the full source model editor is a follow-up.
 */
public class SourceModelsPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSource().
  private static final DataFormat SOURCE_MODEL_INDEX = new DataFormat("application/x-a12-source-model-index");

  @FXML
  private HBox sourceModelHeaders;

  @FXML
  private VBox sourceModelRows;

  @FXML
  private Label sourceModelsEmptyLabel;

  private MappingModel model;

  public void setModel(@NonNull MappingModel model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    MappingSource sourceModel = new MappingSource();
    getSource().add(sourceModel);
    rebuildRows();
    commitHeaderChange();
    openEditDialog(sourceModel);
  }

  private List<MappingSource> getSource() {
    return model.getContent().getSource();
  }

  private void rebuildRows() {
    if (model == null) {
      return;
    }
    sourceModelRows.getChildren().clear();

    List<MappingSource> source = getSource();
    boolean empty = source.isEmpty();
    sourceModelHeaders.setVisible(!empty);
    sourceModelHeaders.setManaged(!empty);
    sourceModelsEmptyLabel.setVisible(empty);
    sourceModelsEmptyLabel.setManaged(empty);

    for (int index = 0; index < source.size(); index++) {
      sourceModelRows.getChildren().add(createRow(source.get(index), index, source.size()));
    }
  }

  private HBox createRow(MappingSource sourceModel, int index, int rowCount) {
    FontIcon dragHandle = new FontIcon(Icons.DRAG_HANDLE);
    dragHandle.setIconSize(18);
    dragHandle.getStyleClass().add("module-drag-handle");
    dragHandle.setCursor(Cursor.MOVE);

    Label nameLabel = createRowLabel(nullToEmpty(sourceModel.getName()), "sourceModelName-" + index, 150.0, sourceModel);
    Label modelLabel = createRowLabel(nullToEmpty(sourceModel.getDmId()), "sourceModelModel-" + index, 200.0, sourceModel);
    Label repetitionsLabel = createRowLabel(sourceModel.getMaxRepeat() != null ? String.valueOf(sourceModel.getMaxRepeat()) : "", "sourceModelRepetitions-" + index, 100.0, sourceModel);
    Label skipValidationLabel = createRowLabel(Boolean.TRUE.equals(sourceModel.getNoSourceValidation()) ? "Yes" : "No", "sourceModelSkipValidation-" + index, 150.0, sourceModel);

    HBox row = new HBox(10.0, dragHandle, nameLabel, modelLabel, repetitionsLabel, skipValidationLabel, createActionsBox(sourceModel, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    setupDragAndDrop(row, dragHandle, index);
    return row;
  }

  private static String nullToEmpty(String value) {
    return value != null ? value : "";
  }

  private Label createRowLabel(String text, String id, double width, MappingSource sourceModel) {
    Label label = new Label(text);
    label.setId(id);
    label.setPrefWidth(width);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(sourceModel);
      }
    });
    return label;
  }

  private void openEditDialog(MappingSource sourceModel) {
    Dialogs.showSourceModel(Studio.stage, sourceModel);
    rebuildRows();
  }

  // Only the drag handle initiates a drag (so clicking a label or the action buttons doesn't start one); the
  // whole row is the drop target, so hovering anywhere over another row while dragging offers reordering there.
  private void setupDragAndDrop(HBox row, Node dragHandle, int index) {
    dragHandle.setOnDragDetected(event -> {
      Dragboard dragboard = dragHandle.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(SOURCE_MODEL_INDEX, String.valueOf(index));
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
      if (event.getDragboard().hasContent(SOURCE_MODEL_INDEX)) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, isAboveMidpoint(row, event.getY()));
      }
      event.consume();
    });
    row.setOnDragExited(event -> clearDropIndicator(row));
    row.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = dragboard.hasContent(SOURCE_MODEL_INDEX);
      if (success) {
        int insertBeforeIndex = isAboveMidpoint(row, event.getY()) ? index : index + 1;
        moveSourceModel(Integer.parseInt((String) dragboard.getContent(SOURCE_MODEL_INDEX)), insertBeforeIndex);
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

  // targetIndex is the position the moved source model should end up at, indexed into the list as it stood
  // before the drag started (e.g. "landed above the row currently at index 2" is targetIndex 2).
  private void moveSourceModel(int fromIndex, int targetIndex) {
    int insertIndex = fromIndex < targetIndex ? targetIndex - 1 : targetIndex;
    if (insertIndex == fromIndex) {
      return;
    }
    List<MappingSource> source = getSource();
    MappingSource moved = source.remove(fromIndex);
    source.add(insertIndex, moved);
    rebuildRows();
    commitHeaderChange();
  }

  private HBox createActionsBox(MappingSource sourceModel, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(sourceModel));

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this source model?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getSource().remove(sourceModel);
        rebuildRows();
        commitHeaderChange();
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
    Collections.swap(getSource(), fromIndex, toIndex);
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
