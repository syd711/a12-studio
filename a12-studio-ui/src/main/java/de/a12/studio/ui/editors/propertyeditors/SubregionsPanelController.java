package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Layout;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.applicationmodel.dialogs.SubregionDialogController;
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

/**
 * Edits {@link ApplicationModelContent#getRegion()}'s {@link Region#getSubRegions()}: a list of subregions, each
 * reorderable (move up/down), editable and copyable via {@link SubregionDialogController}, and deletable. Same
 * row-based layout as {@link ModulesPanelController}, with an additional read-only "Layout" column showing each
 * subregion's {@link Layout#getName()}. Not bound to a single {@link de.a12.studio.models.documentmodel.Element}
 * (the region lives on the model's content), so it follows the model-header pattern used by e.g. {@link
 * RegionPanelController}.
 */
public class SubregionsPanelController extends AbstractPropertyEditor {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into getSubRegions().
  private static final DataFormat SUBREGION_INDEX = new DataFormat("application/x-a12-subregion-index");

  @FXML
  private HBox columnHeaders;

  @FXML
  private VBox subregionsList;

  private ApplicationModel model;

  public void setModel(@NonNull ApplicationModel model) {
    this.model = model;
    rebuildRows();
  }

  @FXML
  private void onAdd() {
    SubregionDialogController.showForAdd(Studio.stage).ifPresent(name -> {
      Region subregion = new Region();
      subregion.setName(name);
      getOrCreateSubRegions().add(subregion);
      rebuildRows();
      commitChange();
    });
  }

  private Region getRegion() {
    return model == null || model.getContent() == null ? null : model.getContent().getRegion();
  }

  private List<Region> getSubRegions() {
    Region region = getRegion();
    return region != null ? region.getSubRegions() : List.of();
  }

  private List<Region> getOrCreateSubRegions() {
    ApplicationModelContent content = model.getContent();
    if (content == null) {
      content = new ApplicationModelContent();
      model.setContent(content);
    }
    Region region = content.getRegion();
    if (region == null) {
      region = new Region();
      content.setRegion(region);
    }
    return region.getSubRegions();
  }

  private void rebuildRows() {
    subregionsList.getChildren().clear();

    List<Region> subregions = getSubRegions();
    columnHeaders.setVisible(!subregions.isEmpty());
    columnHeaders.setManaged(!subregions.isEmpty());
    if (subregions.isEmpty()) {
      Label emptyLabel = new Label("No subregions configured.");
      emptyLabel.getStyleClass().add("placeholder-label");
      subregionsList.getChildren().add(emptyLabel);
      return;
    }

    for (int index = 0; index < subregions.size(); index++) {
      subregionsList.getChildren().add(createRow(subregions.get(index), index, subregions.size()));
    }
  }

  private HBox createRow(Region subregion, int index, int rowCount) {
    FontIcon dragHandle = new FontIcon(Icons.DRAG_HANDLE);
    dragHandle.setIconSize(18);
    dragHandle.getStyleClass().add("module-drag-handle");
    dragHandle.setCursor(Cursor.MOVE);

    Label nameLabel = new Label(subregion.getName());
    nameLabel.setId("subregion-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    Label layoutLabel = new Label(subregion.getLayout() != null ? subregion.getLayout().getName() : "");
    layoutLabel.setId("subregion-layout-" + index);
    layoutLabel.setPrefWidth(140.0);

    HBox row = new HBox(10.0, dragHandle, nameLabel, layoutLabel, createActionsBox(subregion, index, rowCount));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    setupDragAndDrop(row, dragHandle, index);
    return row;
  }

  // Only the drag handle initiates a drag (so clicking name text or the action buttons doesn't start one); the
  // whole row is the drop target, so hovering anywhere over another row while dragging offers reordering there.
  // The drop position is shown as an accent-colored line on the row's top or bottom edge, depending on which
  // half of the row the cursor is over, so it's unambiguous whether the dragged subregion will land above or below.
  private void setupDragAndDrop(HBox row, Node dragHandle, int index) {
    dragHandle.setOnDragDetected(event -> {
      Dragboard dragboard = dragHandle.startDragAndDrop(TransferMode.MOVE);
      ClipboardContent content = new ClipboardContent();
      content.put(SUBREGION_INDEX, String.valueOf(index));
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
      if (event.getDragboard().hasContent(SUBREGION_INDEX)) {
        event.acceptTransferModes(TransferMode.MOVE);
        showDropIndicator(row, isAboveMidpoint(row, event.getY()));
      }
      event.consume();
    });
    row.setOnDragExited(event -> clearDropIndicator(row));
    row.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = dragboard.hasContent(SUBREGION_INDEX);
      if (success) {
        int insertBeforeIndex = isAboveMidpoint(row, event.getY()) ? index : index + 1;
        moveSubregion(Integer.parseInt((String) dragboard.getContent(SUBREGION_INDEX)), insertBeforeIndex);
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

  // targetIndex is the position the moved subregion should end up at, indexed into the list as it stood before
  // the drag started (e.g. "landed above the row currently at index 2" is targetIndex 2).
  private void moveSubregion(int fromIndex, int targetIndex) {
    int insertIndex = fromIndex < targetIndex ? targetIndex - 1 : targetIndex;
    if (insertIndex == fromIndex) {
      return;
    }
    List<Region> subregions = getOrCreateSubRegions();
    Region moved = subregions.remove(fromIndex);
    subregions.add(insertIndex, moved);
    rebuildRows();
    commitChange();
  }

  private HBox createActionsBox(Region subregion, int index, int rowCount) {
    VBox moveButtonsBox = createMoveButtonsBox(index, rowCount);

    Button editButton = createActionButton(Icons.PENCIL, "Edit", () ->
        SubregionDialogController.showForEdit(Studio.stage, subregion.getName()).ifPresent(name -> {
          subregion.setName(name);
          rebuildRows();
          commitChange();
        }));

    Button copyButton = createActionButton(Icons.COPY, "Copy", () -> {
      Region copy = new Region();
      copy.setName(subregion.getName());
      copy.setLayout(subregion.getLayout());
      copy.setSubRegions(new ArrayList<>(subregion.getSubRegions()));
      List<Region> subregions = getOrCreateSubRegions();
      subregions.add(subregions.indexOf(subregion) + 1, copy);
      rebuildRows();
      commitChange();
    });

    Button deleteButton = createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, "Delete this subregion?", null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        getOrCreateSubRegions().remove(subregion);
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
    Collections.swap(getOrCreateSubRegions(), fromIndex, toIndex);
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
