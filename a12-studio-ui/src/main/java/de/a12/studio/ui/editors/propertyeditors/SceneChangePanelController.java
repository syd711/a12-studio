package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.SceneChange;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.applicationmodel.dialogs.Dialogs;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Edits a {@link SceneChange}'s {@code onEnter} and {@code onExit} {@link Directive} lists: each is a
 * non-inline-editable, reorderable (drag handle plus move up/down) row list (row = Type/Region/Name summary),
 * matching the SME reference's "On Enter"/"On Exit" tables. Same row-based layout as {@link
 * SubregionsPanelController}. Reused by both {@link de.a12.studio.ui.editors.applicationmodel.dialogs.SceneDialogController}
 * (where both lists apply) and {@link de.a12.studio.ui.editors.applicationmodel.dialogs.CaseDialogController}
 * (where only {@code onEnter} applies, see {@link de.a12.studio.models.applicationmodel.SceneChange}'s own
 * {@code onExit} javadoc), via {@link #bind}. Isn't wired through {@link
 * de.a12.studio.ui.editors.AbstractPropertyEditor} for the same reason as {@link
 * MatchConditionsPanelController}.
 */
public class SceneChangePanelController {

  // Identifies a row-reorder drag; separate formats for onEnter/onExit so a drag started in one list can't be
  // dropped into the other (both lists otherwise carry the same "index into the source list" payload shape).
  private static final DataFormat ON_ENTER_DIRECTIVE_INDEX = new DataFormat("application/x-a12-scene-change-on-enter-index");
  private static final DataFormat ON_EXIT_DIRECTIVE_INDEX = new DataFormat("application/x-a12-scene-change-on-exit-index");

  @FXML
  private HBox onEnterColumnHeaders;

  @FXML
  private VBox onEnterRows;

  @FXML
  private Label onEnterEmptyLabel;

  @FXML
  private VBox onExitSection;

  @FXML
  private HBox onExitColumnHeaders;

  @FXML
  private VBox onExitRows;

  @FXML
  private Label onExitEmptyLabel;

  private Supplier<SceneChange> getter;

  private Consumer<SceneChange> setter;

  public void bind(@NonNull Supplier<SceneChange> getter, @NonNull Consumer<SceneChange> setter, boolean showOnExit) {
    this.getter = getter;
    this.setter = setter;
    onExitSection.setVisible(showOnExit);
    onExitSection.setManaged(showOnExit);
    rebuildAll();
  }

  @FXML
  private void onAddOnEnter() {
    addDirective(getOrCreateOnEnter());
  }

  @FXML
  private void onAddOnExit() {
    addDirective(getOrCreateOnExit());
  }

  private void addDirective(List<Directive> directives) {
    Dialogs.showDirectiveForAdd(Studio.stage).ifPresent(directive -> {
      directives.add(directive);
      rebuildAll();
    });
  }

  private List<Directive> getOnEnter() {
    SceneChange sceneChange = getter.get();
    return sceneChange != null ? sceneChange.getOnEnter() : List.of();
  }

  private List<Directive> getOnExit() {
    SceneChange sceneChange = getter.get();
    return sceneChange != null ? sceneChange.getOnExit() : List.of();
  }

  private List<Directive> getOrCreateOnEnter() {
    return getOrCreateSceneChange().getOnEnter();
  }

  private List<Directive> getOrCreateOnExit() {
    return getOrCreateSceneChange().getOnExit();
  }

  private SceneChange getOrCreateSceneChange() {
    SceneChange sceneChange = getter.get();
    if (sceneChange == null) {
      sceneChange = new SceneChange();
      setter.accept(sceneChange);
    }
    return sceneChange;
  }

  private void rebuildAll() {
    rebuildRows(onEnterColumnHeaders, onEnterRows, onEnterEmptyLabel, getOnEnter(), ON_ENTER_DIRECTIVE_INDEX);
    if (onExitSection.isVisible()) {
      rebuildRows(onExitColumnHeaders, onExitRows, onExitEmptyLabel, getOnExit(), ON_EXIT_DIRECTIVE_INDEX);
    }
  }

  private void rebuildRows(HBox columnHeaders, VBox rows, Label emptyLabel, List<Directive> directives, DataFormat indexFormat) {
    rows.getChildren().clear();

    boolean empty = directives.isEmpty();
    columnHeaders.setVisible(!empty);
    columnHeaders.setManaged(!empty);
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);

    for (int index = 0; index < directives.size(); index++) {
      rows.getChildren().add(createRow(directives, directives.get(index), index, indexFormat));
    }
  }

  private HBox createRow(List<Directive> directives, Directive directive, int index, DataFormat indexFormat) {
    FontIcon dragHandle = new FontIcon(Icons.DRAG_HANDLE);
    dragHandle.setIconSize(18);
    dragHandle.getStyleClass().add("module-drag-handle");
    dragHandle.setCursor(Cursor.MOVE);

    Label typeLabel = new Label(directive.getType() != null ? directive.getType().getValue() : "");
    typeLabel.setPrefWidth(110.0);
    typeLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editDirective(directives, directive);
      }
    });

    Label regionLabel = new Label(String.join(", ", directive.getRegion()));
    regionLabel.setMaxWidth(Double.MAX_VALUE);
    regionLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(regionLabel, Priority.ALWAYS);
    regionLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editDirective(directives, directive);
      }
    });

    Label nameLabel = new Label(directive instanceof ViewAddDirective viewAdd && viewAdd.getName() != null ? viewAdd.getName() : "");
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    nameLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);
    nameLabel.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        editDirective(directives, directive);
      }
    });

    HBox row = new HBox(10.0, dragHandle, typeLabel, regionLabel, nameLabel, createActionsBox(directives, directive, index));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    setupDragAndDrop(row, dragHandle, directives, index, indexFormat);
    return row;
  }

  // Only the drag handle initiates a drag (so clicking row text or the action buttons doesn't start one); the
  // whole row is the drop target, so hovering anywhere over another row while dragging offers reordering there.
  // The drop position is shown as an accent-colored line on the row's top or bottom edge, depending on which
  // half of the row the cursor is over, so it's unambiguous whether the dragged directive will land above or below.
  private void setupDragAndDrop(HBox row, Node dragHandle, List<Directive> directives, int index, DataFormat indexFormat) {
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
        moveDirective(directives, Integer.parseInt((String) dragboard.getContent(indexFormat)), insertBeforeIndex);
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

  // targetIndex is the position the moved directive should end up at, indexed into the list as it stood before
  // the drag started (e.g. "landed above the row currently at index 2" is targetIndex 2).
  private void moveDirective(List<Directive> directives, int fromIndex, int targetIndex) {
    int insertIndex = fromIndex < targetIndex ? targetIndex - 1 : targetIndex;
    if (insertIndex == fromIndex) {
      return;
    }
    Directive moved = directives.remove(fromIndex);
    directives.add(insertIndex, moved);
    rebuildAll();
  }

  private void editDirective(List<Directive> directives, Directive directive) {
    Dialogs.showDirectiveForEdit(Studio.stage, directive).ifPresent(updated -> {
      directives.set(directives.indexOf(directive), updated);
      rebuildAll();
    });
  }

  private HBox createActionsBox(List<Directive> directives, Directive directive, int index) {
    VBox moveButtonsBox = createMoveButtonsBox(directives, index);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () -> editDirective(directives, directive));

    Button copyButton = createActionButton(Icons.COPY, "Duplicate", () -> {
      directives.add(index + 1, cloneDirective(directive));
      rebuildAll();
    });

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this directive?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        directives.remove(directive);
        rebuildAll();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  // Move up/down stacked in a VBox instead of side by side in the HBox: each button is half-height (see the
  // "move-button" style class), so the pair together takes up the same width/height as a single normal button.
  private VBox createMoveButtonsBox(List<Directive> directives, int index) {
    Button moveUpButton = createActionButton(Icons.ARROW_UP, "Move Up", () -> moveRow(directives, index, index - 1));
    moveUpButton.setDisable(index == 0);
    moveUpButton.getStyleClass().addAll("move-button", "move-button-top");

    Button moveDownButton = createActionButton(Icons.ARROW_DOWN, "Move Down", () -> moveRow(directives, index, index + 1));
    moveDownButton.setDisable(index == directives.size() - 1);
    moveDownButton.getStyleClass().addAll("move-button", "move-button-bottom");

    return new VBox(1, moveUpButton, moveDownButton);
  }

  private void moveRow(List<Directive> directives, int fromIndex, int toIndex) {
    Collections.swap(directives, fromIndex, toIndex);
    rebuildAll();
  }

  private static Directive cloneDirective(@NonNull Directive directive) {
    String json = JsonSettings.objectMapper.writeValueAsString(directive);
    return JsonSettings.objectMapper.readValue(json, Directive.class);
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
