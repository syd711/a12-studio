package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.formmodel.Button;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.input.DataFormat;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Edits a list of {@link Button} rows (a footer/subheader box's Major or Minor button list) as a compact,
 * reorderable table of Name/Type, matching the SME reference's "Major Buttons"/"Minor Buttons" tables. Each
 * row's Name/Type cells and its Edit button open the full button editor ({@link
 * de.a12.studio.ui.editors.formmodel.dialogs.Dialogs#showButtonForEdit}, supplied via {@code editRowFunction}
 * so this shared/propertyeditors-package class doesn't depend on the formmodel-package dialog directly); the
 * Add button opens the same editor via the caller-supplied {@code newRowFactory} ({@link
 * de.a12.studio.ui.editors.formmodel.dialogs.Dialogs#showButtonForAdd}), only adding the new row once confirmed.
 * Copy duplicates a row in place with a fresh id (from {@code idGenerator}) and a disambiguated name.
 * <p>
 * Rows can be reordered within one panel instance, and also dragged into a sibling instance (e.g. from
 * "Subheader Major Buttons" into "Footer Minor Buttons") to move a button between sections - see {@link
 * de.a12.studio.ui.editors.formmodel.FormModelEditorController}, which configures one instance per
 * Major/Minor x Subheader/Footer combination, all sharing {@link #DRAG_FORMAT}. Not tied to a single {@link
 * de.a12.studio.models.documentmodel.Element}, so it follows the model-header pattern ({@link
 * #commitHeaderChange()}) rather than {@link #commitChange()}.
 */
public class ToolbarButtonsPanelController extends AbstractPropertyEditor {

  // Shared by every panel instance (unlike a per-instance DataFormat) so a row dragged out of one panel can be
  // dropped into a sibling one. The dragged payload is "<instanceId>:<index>", resolved back to its source
  // panel/list via INSTANCES on drop.
  private static final DataFormat DRAG_FORMAT = new DataFormat("application/x-a12-toolbar-button-move");

  // Every currently configured panel instance, keyed by its instanceId. Intentionally never pruned: editor-tab
  // panels like this one are never explicitly torn down on tab close (see AbstractPropertyEditor's
  // StudioEventManager registration, which has the same lifetime), so this mirrors that existing behavior
  // rather than being a new leak.
  private static final Map<String, ToolbarButtonsPanelController> INSTANCES = new HashMap<>();

  // settingsKeySuffix alone (e.g. ".footerMajor") repeats every time a Form Model tab is (re)opened, so a
  // counter is appended to keep each configure() call's instanceId unique for the life of the process.
  private static final AtomicLong INSTANCE_COUNTER = new AtomicLong();

  @FXML
  private HBox buttonsHeader;

  @FXML
  private VBox buttonsList;

  @FXML
  private Label emptyLabel;

  private List<Button> rows;
  private Supplier<Optional<Button>> newRowFactory;
  private Function<Button, Optional<Button>> editRowFunction;
  private Supplier<String> idGenerator;

  private String instanceId;

  /**
   * Binds this panel to {@code rows} - add/remove/move/edit here mutate the caller's real list in place.
   * {@code newRowFactory} and {@code editRowFunction} open the caller's Add/Edit dialogs, returning the
   * resulting button (or empty if cancelled); {@code idGenerator} mints a fresh id for a Copy.
   */
  public void configure(@NonNull String title, @NonNull String settingsKeySuffix, @NonNull List<Button> rows,
      @NonNull Supplier<Optional<Button>> newRowFactory, @NonNull Function<Button, Optional<Button>> editRowFunction,
      @NonNull Supplier<String> idGenerator) {
    setTitle(title);
    setSettingsKeySuffix(settingsKeySuffix);
    this.rows = rows;
    this.newRowFactory = newRowFactory;
    this.editRowFunction = editRowFunction;
    this.idGenerator = idGenerator;
    if (instanceId == null) {
      instanceId = settingsKeySuffix + "-" + INSTANCE_COUNTER.incrementAndGet();
      setupEmptyListDropTarget();
    }
    INSTANCES.put(instanceId, this);
    rebuildRows();
  }

  /**
   * A row being dragged into an empty panel has no sibling row to drop onto (see {@link
   * RowFactory#setupCrossContainerRowDragAndDrop}'s per-row drop targets), so {@code emptyLabel} itself accepts
   * the drop in that case, appending the moved button at the end. The drop target is {@code emptyLabel} rather
   * than {@code buttonsList} because the latter has zero size while empty (no children) and would never
   * receive drag-over events; {@code emptyLabel} also gains an accent-colored dashed border while a compatible
   * drag hovers over it, so it visibly reads as a drop zone instead of just static placeholder text.
   */
  private void setupEmptyListDropTarget() {
    emptyLabel.setOnDragOver(event -> {
      if (rows.isEmpty() && event.getDragboard().hasContent(DRAG_FORMAT)) {
        event.acceptTransferModes(TransferMode.MOVE);
      }
      event.consume();
    });
    emptyLabel.setOnDragEntered(event -> {
      if (rows.isEmpty() && event.getDragboard().hasContent(DRAG_FORMAT)) {
        emptyLabel.getStyleClass().add("toolbar-buttons-empty-drop-target-active");
      }
    });
    emptyLabel.setOnDragExited(event -> emptyLabel.getStyleClass().remove("toolbar-buttons-empty-drop-target-active"));
    emptyLabel.setOnDragDropped(event -> {
      Dragboard dragboard = event.getDragboard();
      boolean success = rows.isEmpty() && dragboard.hasContent(DRAG_FORMAT);
      if (success) {
        handleDrop((String) dragboard.getContent(DRAG_FORMAT), 0);
      }
      emptyLabel.getStyleClass().remove("toolbar-buttons-empty-drop-target-active");
      event.setDropCompleted(success);
      event.consume();
    });
  }

  @FXML
  private void onAdd() {
    newRowFactory.get().ifPresent(row -> {
      rows.add(row);
      rebuildRows();
      commitHeaderChange();
    });
  }

  private void rebuildRows() {
    buttonsList.getChildren().clear();

    boolean empty = rows.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    buttonsHeader.setVisible(!empty);
    buttonsHeader.setManaged(!empty);

    for (int index = 0; index < rows.size(); index++) {
      buttonsList.getChildren().add(createRow(rows.get(index), index, rows.size()));
    }
  }

  private HBox createRow(Button row, int index, int rowCount) {
    FontIcon dragHandle = RowFactory.createDragHandle();

    Label nameLabel = createCell(row.getName() == null ? "" : row.getName(), "toolbarButtonName-" + index, row);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);

    Label typeLabel = createCell(row.getType() != null ? row.getType().getValue() : "", "toolbarButtonType-" + index, row);
    typeLabel.setPrefWidth(140.0);

    HBox rowBox = new HBox(10.0, dragHandle, nameLabel, typeLabel, createActionsBox(row, index, rowCount));
    rowBox.setAlignment(Pos.CENTER_LEFT);
    rowBox.getStyleClass().add("module-row");
    RowFactory.setupCrossContainerRowDragAndDrop(rowBox, dragHandle, DRAG_FORMAT, instanceId + ":" + index, index, this::handleDrop);
    return rowBox;
  }

  private Label createCell(String text, String id, Button row) {
    Label label = new Label(text);
    label.setId(id);
    label.setCursor(Cursor.HAND);
    label.setOnMouseClicked(event -> {
      if (event.getClickCount() == 1) {
        openEditDialog(row);
      }
    });
    return label;
  }

  private void openEditDialog(Button row) {
    editRowFunction.apply(row).ifPresent(edited -> {
      int index = rows.indexOf(row);
      if (index >= 0) {
        rows.set(index, edited);
        rebuildRows();
        commitHeaderChange();
      }
    });
  }

  private void handleDrop(String payload, int insertBeforeIndex) {
    int separator = payload.lastIndexOf(':');
    String sourceInstanceId = payload.substring(0, separator);
    int sourceIndex = Integer.parseInt(payload.substring(separator + 1));

    ToolbarButtonsPanelController source = INSTANCES.get(sourceInstanceId);
    if (source == null || sourceIndex < 0 || sourceIndex >= source.rows.size()) {
      return;
    }

    if (source == this) {
      if (RowFactory.reorder(rows, sourceIndex, insertBeforeIndex)) {
        rebuildRows();
        commitHeaderChange();
      }
      return;
    }

    Button moved = source.rows.remove(sourceIndex);
    rows.add(Math.min(insertBeforeIndex, rows.size()), moved);
    source.rebuildRows();
    rebuildRows();
    commitHeaderChange();
  }

  private HBox createActionsBox(Button row, int index, int rowCount) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, rowCount, this::moveRow);

    javafx.scene.control.Button editButton = RowFactory.createActionButton(Icons.PENCIL, "Edit", () -> openEditDialog(row));

    javafx.scene.control.Button copyButton = RowFactory.createActionButton(Icons.COPY, StudioBundle.get("copy"), () -> {
      Button copy = cloneButton(row);
      copy.setId(idGenerator.get());
      copy.setName(uniqueCopyName(row.getName()));
      rows.add(rows.indexOf(row) + 1, copy);
      rebuildRows();
      commitHeaderChange();
    });

    javafx.scene.control.Button deleteButton = RowFactory.createActionButton(Icons.TRASH, "Delete", () -> {
      Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage, StudioBundle.get("delete_this_button"), null, null, "Delete");
      if (result.isPresent() && result.get() == ButtonType.OK) {
        rows.remove(row);
        rebuildRows();
        commitHeaderChange();
      }
    });

    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, copyButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private void moveRow(int fromIndex, int toIndex) {
    Collections.swap(rows, fromIndex, toIndex);
    rebuildRows();
    commitHeaderChange();
  }

  private static Button cloneButton(Button original) {
    String json = JsonSettings.objectMapper.writeValueAsString(original);
    return JsonSettings.objectMapper.readValue(json, Button.class);
  }

  private String uniqueCopyName(String baseName) {
    String base = baseName == null ? "" : baseName;
    Set<String> usedNames = new HashSet<>();
    for (Button button : rows) {
      usedNames.add(button.getName());
    }
    String candidate = base + "_copy";
    int suffix = 2;
    while (usedNames.contains(candidate)) {
      candidate = base + "_copy" + suffix;
      suffix++;
    }
    return candidate;
  }
}
