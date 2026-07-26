package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * Edits {@link ApplicationModelContent#getModules()}: a list of module names, each reorderable (move up/down),
 * copyable and deletable, with a full editor (name, menu label, roles) opened inline via {@link #onEditModule}
 * (see {@link #setOnEditModule}) when a row's "Edit" button is pressed or a row is double-clicked. Not bound to
 * a single Element (modules live on the model's content), so it follows the model-header pattern used by e.g.
 * {@link ActivityPanelController}.
 */
public class ModulesPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getModules().
  private static final DataFormat MODULE_INDEX = new DataFormat("application/x-a12-module-index");

  @FXML
  private VBox modulesList;

  private ApplicationModel model;

  // Notified with the module to open in the inline editor, e.g. by ApplicationModelEditorController to show
  // it in its editorContainer. Set via setOnEditModule once this panel is loaded from FXML.
  private Consumer<Module> onEditModule;

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    rebuildRows();
  }

  public void setOnEditModule(@NonNull Consumer<Module> onEditModule) {
    this.onEditModule = onEditModule;
  }

  @FXML
  private void onAdd() {
    Dialogs.showModuleForAdd(Studio.stage).ifPresent(name -> {
      Module module = new Module();
      module.setName(name);
      getModules().add(module);
      rebuildRows();
      commitChange();
    });
  }

  private List<Module> getModules() {
    return model.getContent().getModules();
  }

  private void rebuildRows() {
    modulesList.getChildren().clear();

    List<Module> modules = getModules();
    if (modules.isEmpty()) {
      Label emptyLabel = new Label("No modules found.");
      emptyLabel.getStyleClass().add("placeholder-label");
      modulesList.getChildren().add(emptyLabel);
      return;
    }

    for (int index = 0; index < modules.size(); index++) {
      modulesList.getChildren().add(createRow(modules.get(index), index, modules.size()));
    }
  }

  private HBox createRow(Module module, int index, int rowCount) {
    FontIcon dragHandle = new FontIcon(Icons.DRAG_HANDLE);
    dragHandle.setIconSize(18);
    dragHandle.getStyleClass().add("module-drag-handle");
    dragHandle.setCursor(Cursor.MOVE);

    Label nameLabel = new Label(module.getName());
    nameLabel.setId("module-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    HBox row = new HBox(10.0, dragHandle, nameLabel, createActionsBox(module, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    row.setOnMouseClicked(event -> {
      if (event.getClickCount() == 2) {
        onEditModule.accept(module);
      }
    });
    setupDragAndDrop(row, dragHandle, index);
    return row;
  }

  // Only the drag handle initiates a drag (so clicking name text or the action buttons doesn't start one); the
  // whole row is the drop target, so hovering anywhere over another row while dragging offers reordering there.
  // The drop position is shown as an accent-colored line on the row's top or bottom edge, depending on which
  // half of the row the cursor is over, so it's unambiguous whether the dragged module will land above or below.
  private void setupDragAndDrop(HBox row, Node dragHandle, int index) {
    dragHandle.setOnDragDetected(event -> {
      Dragboard dragboard = dragHandle.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(MODULE_INDEX, String.valueOf(index));
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
      if (event.getDragboard().hasContent(MODULE_INDEX)) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, isAboveMidpoint(row, event.getY()));
      }
      event.consume();
    });
    row.setOnDragExited(event -> clearDropIndicator(row));
    row.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = dragboard.hasContent(MODULE_INDEX);
      if (success) {
        int insertBeforeIndex = isAboveMidpoint(row, event.getY()) ? index : index + 1;
        moveModule(Integer.parseInt((String) dragboard.getContent(MODULE_INDEX)), insertBeforeIndex);
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

  // targetIndex is the position the moved module should end up at, indexed into the list as it stood before
  // the drag started (e.g. "landed above the row currently at index 2" is targetIndex 2).
  private void moveModule(int fromIndex, int targetIndex) {
    int insertIndex = fromIndex < targetIndex ? targetIndex - 1 : targetIndex;
    if (insertIndex == fromIndex) {
      return;
    }
    List<Module> modules = getModules();
    Module moved = modules.remove(fromIndex);
    modules.add(insertIndex, moved);
    rebuildRows();
    commitChange();
  }

  private HBox createActionsBox(Module module, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () -> onEditModule.accept(module));

    Button copyButton = createActionButton(Icons.COPY, "Copy", () -> {
      Module copy = new Module();
      copy.setName(module.getName());
      copy.setMenu(module.getMenu());
      copy.setFlows(new ArrayList<>(module.getFlows()));
      getModules().add(getModules().indexOf(module) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this module?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getModules().remove(module);
        rebuildRows();
        commitChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
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
    Collections.swap(getModules(), fromIndex, toIndex);
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
