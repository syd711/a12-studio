package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.ContentTableColumns;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.contentmodel.dialogs.Dialogs;
import de.a12.studio.ui.editors.propertyeditors.RowFactory;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.input.DataFormat;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * SME's "Columns" section of a Table: the "Enable resizing" switch and the list of columns, one draggable row per
 * column named by the text of its head cell (or {@code <id>} when it has none, as in SME). A row can be moved up or
 * down with its buttons or by dragging its handle - only among columns pinned the same way, like in SME - and
 * edited or deleted; clicking the row or its edit button opens {@link Dialogs#showTableColumn}, which holds all the
 * column's settings (pin direction, action column, widths, alignments). "Insert above"/"Insert below" are in the
 * row's context menu, "Add column" appends one. Structure changes go through {@link ContentTableColumns}, which keeps
 * the head/body/foot cells aligned with the columns, and then tell the editor to rebuild the tree below the table.
 */
public class TableColumnsPanelController extends ContentSettingsPanelController {

  // Identifies a row-reorder drag; the dragboard content is the dragged row's current index into the columns.
  private static final DataFormat COLUMN_INDEX = new DataFormat("application/x-a12-content-table-column-index");

  @FXML
  private VBox columnsBox;

  @FXML
  private Label emptyLabel;

  @Override
  protected void populate(@NonNull ContentElement element) {
    super.populate(element);
    rebuild();
  }

  private ContentElement table() {
    return currentElement();
  }

  private void rebuild() {
    columnsBox.getChildren().clear();
    ContentElement table = table();
    if (table == null) {
      return;
    }
    List<Map<String, Object>> columns = ContentTableColumns.columns(table);
    boolean empty = columns.isEmpty();
    emptyLabel.setVisible(empty);
    emptyLabel.setManaged(empty);
    for (int i = 0; i < columns.size(); i++) {
      columnsBox.getChildren().add(createRow(table, columns, i));
    }
  }

  private HBox createRow(ContentElement table, List<Map<String, Object>> columns, int index) {
    Map<String, Object> column = columns.get(index);
    String head = ContentTableColumns.headLabel(table, index);
    Label nameLabel = new Label(head != null && !head.isBlank() ? head : "<" + column.get("id") + ">");
    nameLabel.setId("tableColumn-" + index);
    nameLabel.setMaxWidth(Double.MAX_VALUE);
    nameLabel.setCursor(Cursor.HAND);
    HBox.setHgrow(nameLabel, Priority.ALWAYS);
    nameLabel.setOnMouseClicked(event -> {
      if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
        editColumn(index);
      }
    });

    FontIcon dragHandle = RowFactory.createDragHandle();
    HBox row = new HBox(10.0, dragHandle, nameLabel, createActionsBox(columns, index));
    row.setAlignment(Pos.CENTER_LEFT);
    row.getStyleClass().add("module-row");
    row.setOnContextMenuRequested(event -> {
      insertMenu(columns, index).show(row, event.getScreenX(), event.getScreenY());
      event.consume();
    });
    RowFactory.setupRowDragAndDrop(row, dragHandle, COLUMN_INDEX, index,
        (from, insertBefore) -> canDrop(columns, from, insertBefore), this::dropColumn);
    return row;
  }

  private HBox createActionsBox(List<Map<String, Object>> columns, int index) {
    VBox moveButtonsBox = RowFactory.createMoveButtonsBox(index, canMove(columns, index, -1), canMove(columns, index, 1),
        this::moveColumn);
    Button editButton = RowFactory.createActionButton(Icons.PENCIL, StudioBundle.get("content_settings.column_edit"),
        () -> editColumn(index));
    Button deleteButton = RowFactory.createActionButton(Icons.TRASH, StudioBundle.get("content_settings.column_delete"),
        () -> confirmDelete(index));
    HBox actionsBox = new HBox(4.0, moveButtonsBox, editButton, deleteButton);
    actionsBox.setAlignment(Pos.CENTER_LEFT);
    return actionsBox;
  }

  private ContextMenu insertMenu(List<Map<String, Object>> columns, int index) {
    Map<String, Object> column = columns.get(index);
    MenuItem above = new MenuItem(StudioBundle.get("content_settings.column_insert_above"));
    above.setOnAction(event -> insert(index, column));
    MenuItem below = new MenuItem(StudioBundle.get("content_settings.column_insert_below"));
    below.setOnAction(event -> insert(index + 1, column));
    return new ContextMenu(above, below);
  }

  @FXML
  private void onAdd() {
    ContentElement table = table();
    if (table == null) {
      return;
    }
    // Before the right-pinned columns, which stay last
    List<Map<String, Object>> columns = ContentTableColumns.columns(table);
    int position = columns.size();
    while (position > 0 && "right".equals(columns.get(position - 1).get("pinning"))) {
      position--;
    }
    ContentTableColumns.insert(table, position, null);
    structureEdited();
  }

  private void editColumn(int index) {
    ContentElement table = table();
    List<Map<String, Object>> columns = ContentTableColumns.columns(table);
    if (index < 0 || index >= columns.size()) {
      return;
    }
    Map<String, Object> column = columns.get(index);
    boolean resizing = new ContentProps(table).getBoolean("enableColumnsResizing", true);
    Optional<Map<String, Object>> edited = Dialogs.showTableColumn(Studio.stage, column, resizing);
    if (edited.isEmpty() || edited.get().equals(column)) {
      return;
    }
    applyEdit(table, index, edited.get());
  }

  /** Writes the dialog's result onto the live column; a changed pin direction also moves the column, like in SME. */
  void applyEdit(ContentElement table, int index, Map<String, Object> edited) {
    Map<String, Object> column = ContentTableColumns.columns(table).get(index);
    Object oldPinning = column.get("pinning");
    Object newPinning = edited.get("pinning");
    column.clear();
    column.putAll(edited);
    if (Objects.equals(oldPinning, newPinning)) {
      changed();
      rebuild();
    }
    else {
      ContentTableColumns.setPinning(table, index, (String) newPinning);
      structureEdited();
    }
  }

  private boolean canMove(List<Map<String, Object>> columns, int index, int offset) {
    int target = index + offset;
    if (target < 0 || target >= columns.size()) {
      return false;
    }
    return Objects.equals(columns.get(index).get("pinning"), columns.get(target).get("pinning"));
  }

  /** SME: a column may only be dropped next to a column pinned the same way. */
  static boolean canDrop(List<Map<String, Object>> columns, int from, int insertBefore) {
    if (from < 0 || from >= columns.size()) {
      return false;
    }
    Object pinning = columns.get(from).get("pinning");
    return (insertBefore > 0 && insertBefore - 1 < columns.size()
        && Objects.equals(columns.get(insertBefore - 1).get("pinning"), pinning))
        || (insertBefore < columns.size() && Objects.equals(columns.get(insertBefore).get("pinning"), pinning));
  }

  private void dropColumn(int from, int insertBefore) {
    int to = from < insertBefore ? insertBefore - 1 : insertBefore;
    if (to != from) {
      moveColumn(from, to);
    }
  }

  private void insert(int index, Map<String, Object> neighbour) {
    ContentTableColumns.insert(table(), index, neighbour.get("pinning") instanceof String p ? p : null);
    structureEdited();
  }

  void moveColumn(int from, int to) {
    ContentTableColumns.move(table(), from, to);
    structureEdited();
  }

  private void confirmDelete(int index) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(Studio.stage,
        StudioBundle.get("content_settings.column_delete_confirm"), null, null, StudioBundle.get("delete"));
    if (result.isPresent() && result.get() == ButtonType.OK) {
      deleteColumn(index);
    }
  }

  void deleteColumn(int index) {
    ContentTableColumns.remove(table(), index);
    structureEdited();
  }

  /** A column was inserted, removed, moved or repinned: cells changed, so the tree needs a rebuild. */
  private void structureEdited() {
    if (context() != null) {
      context().structureChanged();
    }
    changed();
    rebuild();
  }
}
