package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.validators.overview.OverviewStylesValidator;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
import java.util.Optional;

/**
 * Edits {@link de.a12.studio.models.overviewmodel.OverviewModelContent#getStyles()}: a list of CSS style
 * class names, each reorderable (drag handle or move up/down), copyable and deletable, with a directly
 * editable text field per row that must not be blank (see {@link OverviewStylesValidator}). Not bound to a
 * single Element (styles live on the model's content), so it follows the model-header pattern used by e.g.
 * {@link ModulesPanelController}.
 */
public class StylesPanelController extends AbstractPropertyEditor {

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

  private static final int COMMIT_DEBOUNCE_MS = 150;

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getStyles().
  private static final DataFormat STYLE_INDEX = new DataFormat("application/x-a12-style-index");

  @FXML
  private VBox stylesList;

  @FXML
  private Label stylesEmptyLabel;

  private final Debouncer debouncer = new Debouncer();

  private OverviewModel model;

  public void setModel(@NonNull OverviewModel model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    getStyles().add("");
    rebuildRows();
    commitChange();
  }

  private List<String> getStyles() {
    return model.getContent().getStyles();
  }

  private void rebuildRows() {
    refreshStylesError();
    stylesList.getChildren().clear();

    List<String> styles = getStyles();
    boolean empty = styles.isEmpty();
    stylesEmptyLabel.setVisible(empty);
    stylesEmptyLabel.setManaged(empty);

    for (int index = 0; index < styles.size(); index++) {
      stylesList.getChildren().add(createRow(index, styles.size()));
    }
  }

  /**
   * Not bound to an {@link de.a12.studio.models.documentmodel.Element}, so the base class's element-keyed
   * validation plumbing never runs for this panel; queries {@link OverviewStylesValidator}'s dedicated
   * element id directly instead. Called from {@link #rebuildRows} (itself called by every structural
   * mutation here, plus {@link #setModel}) and after every debounced text commit, so this always reflects
   * the list as currently shown.
   */
  private void refreshStylesError() {
    if (model == null) {
      return;
    }
    List<ModelValidationError> errors = Studio.getValidationService().validateElement(model, OverviewStylesValidator.ELEMENT_ID);
    if (errors.isEmpty()) {
      hideError();
    }
    else {
      showError(errors.get(0).severity(), errors.get(0).message());
    }
  }

  private HBox createRow(int index, int rowCount) {
    FontIcon dragHandle = new FontIcon(Icons.DRAG_HANDLE);
    dragHandle.setIconSize(18);
    dragHandle.getStyleClass().add("module-drag-handle");
    dragHandle.setCursor(Cursor.MOVE);

    TextField styleField = new TextField();
    styleField.setId("style-" + index);
    styleField.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(styleField, Priority.ALWAYS);
    String initialValue = getStyles().get(index);
    setFieldValue(styleField, initialValue);
    styleField.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, initialValue == null || initialValue.isBlank());
    styleField.textProperty().addListener((observable, oldValue, newValue) -> {
      getStyles().set(index, newValue);
      styleField.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, newValue == null || newValue.isBlank());
      debouncer.debounce(styleField.getId(), () -> {
        refreshStylesError();
        commitChange();
      }, COMMIT_DEBOUNCE_MS, true);
    });

    HBox row = new HBox(10.0, dragHandle, styleField, createActionsBox(index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    setupDragAndDrop(row, dragHandle, index);
    return row;
  }

  // Only the drag handle initiates a drag (so clicking the text field or the action buttons doesn't start
  // one); the whole row is the drop target, so hovering anywhere over another row while dragging offers
  // reordering there. The drop position is shown as an accent-colored line on the row's top or bottom edge,
  // depending on which half of the row the cursor is over, so it's unambiguous whether the dragged style
  // will land above or below.
  private void setupDragAndDrop(HBox row, Node dragHandle, int index) {
    dragHandle.setOnDragDetected(event -> {
      Dragboard dragboard = dragHandle.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(STYLE_INDEX, String.valueOf(index));
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
      if (event.getDragboard().hasContent(STYLE_INDEX)) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, isAboveMidpoint(row, event.getY()));
      }
      event.consume();
    });
    row.setOnDragExited(event -> clearDropIndicator(row));
    row.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = dragboard.hasContent(STYLE_INDEX);
      if (success) {
        int insertBeforeIndex = isAboveMidpoint(row, event.getY()) ? index : index + 1;
        moveStyle(Integer.parseInt((String) dragboard.getContent(STYLE_INDEX)), insertBeforeIndex);
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

  // targetIndex is the position the moved style should end up at, indexed into the list as it stood before
  // the drag started (e.g. "landed above the row currently at index 2" is targetIndex 2).
  private void moveStyle(int fromIndex, int targetIndex) {
    int insertIndex = fromIndex < targetIndex ? targetIndex - 1 : targetIndex;
    if (insertIndex == fromIndex) {
      return;
    }
    List<String> styles = getStyles();
    String moved = styles.remove(fromIndex);
    styles.add(insertIndex, moved);
    rebuildRows();
    commitChange();
  }

  private HBox createActionsBox(int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button copyButton = createActionButton(Icons.COPY, "Copy", () -> {
      List<String> styles = getStyles();
      styles.add(index + 1, styles.get(index));
      rebuildRows();
      commitChange();
    });

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this style?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getStyles().remove(index);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, copyButton, deleteButton);
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
    Collections.swap(getStyles(), fromIndex, toIndex);
    rebuildRows();
    commitChange();
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
