package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.models.contentmodel.ContentTableColumns;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * SME's "Columns" section of a Table: the "Enable resizing" switch and the list of columns with their width, pinning
 * and action-column flag, plus per column the details (fixed width, minimum width while resizing is enabled, and the
 * horizontal/vertical alignment of the general, head, body and foot areas) and the column actions: insert above or
 * below, move up or down (only among columns pinned the same way), and delete. Structure changes go through {@link
 * ContentTableColumns}, which keeps the head/body/foot cells aligned with the columns, and then tell the editor to
 * rebuild the tree below the table.
 */
public class TableColumnsPanelController extends ContentSettingsPanelController {

  private static final List<String> PINNINGS = List.of("none", "left", "right");
  private static final List<String> HORIZONTAL = List.of("auto", "left", "center", "right");
  private static final List<String> VERTICAL = List.of("auto", "top", "middle", "bottom");
  private static final List<String> AREAS = List.of("general", "head", "body", "foot");

  @FXML
  private VBox columnsBox;

  private final Set<String> detailsOpen = new HashSet<>();

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
    for (int i = 0; i < columns.size(); i++) {
      columnsBox.getChildren().add(columnRow(table, columns, i));
    }
  }

  private Node columnRow(ContentElement table, List<Map<String, Object>> columns, int index) {
    Map<String, Object> column = columns.get(index);
    String id = String.valueOf(column.get("id"));
    String head = ContentTableColumns.headLabel(table, index);
    Label title = new Label(head != null && !head.isBlank() ? head : "<" + id + ">");
    title.setMaxWidth(Double.MAX_VALUE);
    title.getStyleClass().add("content-setting-heading");
    HBox.setHgrow(title, Priority.ALWAYS);

    TextField width = new TextField(column.get("width") == null ? "" : String.valueOf(column.get("width")));
    width.setPrefWidth(56);
    width.setPromptText(StudioBundle.get("content_settings.width"));
    commitOnLeave(width, text -> commitWidth(column, "width", text));

    ComboBox<String> pinning = new ComboBox<>();
    pinning.getItems().setAll(PINNINGS);
    pinning.setConverter(labels("content_settings.pinning_"));
    pinning.setValue(column.get("pinning") instanceof String p ? p : "none");
    pinning.setPrefWidth(88);
    pinning.valueProperty().addListener((observable, oldValue, value) -> {
      if (value != null && !value.equals(oldValue)) {
        ContentTableColumns.setPinning(table, index, "none".equals(value) ? null : value);
        structureEdited();
      }
    });

    CheckBox action = new CheckBox(StudioBundle.get("content_settings.column_action"));
    action.setSelected(Boolean.TRUE.equals(column.get("actionColumn")));
    action.selectedProperty().addListener((observable, oldValue, selected) -> {
      if (selected) {
        column.put("actionColumn", true);
      }
      else {
        column.remove("actionColumn");
      }
      changed();
    });

    ToggleButton details = new ToggleButton(StudioBundle.get("content_settings.column_details"));
    details.setSelected(detailsOpen.contains(id));
    Button delete = new Button();
    delete.setGraphic(WidgetFactory.createIcon("mdi2d-delete-outline", 14, null));
    delete.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("content_settings.column_delete")));
    delete.setOnAction(event -> confirmDelete(index));
    MenuButton actions = new MenuButton(StudioBundle.get("content_settings.column_actions"));
    actions.getItems().addAll(
        item("content_settings.column_insert_above", false, () -> insert(index, column)),
        item("content_settings.column_insert_below", false, () -> insert(index + 1, column)),
        item("content_settings.column_move_up", !canMove(columns, index, -1), () -> moveColumn(index, index - 1)),
        item("content_settings.column_move_down", !canMove(columns, index, 1), () -> moveColumn(index, index + 1)));

    HBox summary = new HBox(6, title, width, pinning);
    summary.setAlignment(Pos.CENTER_LEFT);
    HBox buttons = new HBox(6, action, details, actions, delete);
    buttons.setAlignment(Pos.CENTER_LEFT);

    VBox card = new VBox(4, summary, buttons);
    card.getStyleClass().add("content-setting-card");
    card.setPadding(new Insets(6));
    VBox detailsBox = detailsBox(column);
    detailsBox.setVisible(details.isSelected());
    detailsBox.setManaged(details.isSelected());
    details.selectedProperty().addListener((observable, oldValue, selected) -> {
      detailsBox.setVisible(selected);
      detailsBox.setManaged(selected);
      if (selected) {
        detailsOpen.add(id);
      }
      else {
        detailsOpen.remove(id);
      }
    });
    card.getChildren().add(detailsBox);
    return card;
  }

  private VBox detailsBox(Map<String, Object> column) {
    VBox box = new VBox(4);
    CheckBox fixed = new CheckBox(StudioBundle.get("content_settings.column_fixed_width"));
    fixed.setSelected(Boolean.TRUE.equals(column.get("fixedWidth")));
    fixed.selectedProperty().addListener((observable, oldValue, selected) -> {
      if (selected) {
        column.put("fixedWidth", true);
      }
      else {
        column.remove("fixedWidth");
      }
      changed();
    });
    box.getChildren().add(fixed);
    if (new ContentProps(table()).getBoolean("enableColumnsResizing", true)) {
      TextField minWidth = new TextField(column.get("minResizeWidth") == null ? "" : String.valueOf(column.get("minResizeWidth")));
      minWidth.setPrefWidth(70);
      commitOnLeave(minWidth, text -> commitWidth(column, "minResizeWidth", text));
      box.getChildren().add(labeled(StudioBundle.get("content_settings.column_min_width"), minWidth));
    }
    box.getChildren().add(alignment(column, "horizontalAlignment", "content_settings.column_horizontal_alignment", HORIZONTAL));
    box.getChildren().add(alignment(column, "verticalAlignment", "content_settings.column_vertical_alignment", VERTICAL));
    return box;
  }

  private Node alignment(Map<String, Object> column, String key, String titleKey, List<String> values) {
    VBox box = new VBox(2);
    box.getChildren().add(new Label(StudioBundle.get(titleKey)));
    Object current = column.get(key);
    for (String area : AREAS) {
      ComboBox<String> combo = new ComboBox<>();
      combo.getItems().setAll(values);
      combo.setConverter(labels("content_settings.alignment_"));
      combo.setValue(current instanceof Map<?, ?> map && map.get(area) instanceof String value ? value : "auto");
      combo.valueProperty().addListener((observable, oldValue, value) -> {
        if (value == null) {
          return;
        }
        Map<String, Object> areas = column.get(key) instanceof Map<?, ?> existing
            ? castMap(existing) : new LinkedHashMap<>();
        if ("auto".equals(value)) {
          areas.remove(area);
        }
        else {
          areas.put(area, value);
        }
        if (areas.isEmpty()) {
          column.remove(key);
        }
        else {
          column.put(key, areas);
        }
        changed();
      });
      box.getChildren().add(labeled(StudioBundle.get("content_settings.area_" + area), combo));
    }
    return box;
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> castMap(Map<?, ?> map) {
    return (Map<String, Object>) map;
  }

  private static HBox labeled(String text, Node control) {
    Label label = new Label(text);
    label.setMinWidth(100);
    HBox line = new HBox(8, label, control);
    line.setAlignment(Pos.CENTER_LEFT);
    return line;
  }

  private static StringConverter<String> labels(String keyPrefix) {
    return new StringConverter<>() {
      @Override
      public String toString(String value) {
        return value == null ? "" : StudioBundle.get(keyPrefix + value);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    };
  }

  private static MenuItem item(String key, boolean disabled, Runnable action) {
    MenuItem item = new MenuItem(StudioBundle.get(key));
    item.setDisable(disabled);
    item.setOnAction(event -> action.run());
    return item;
  }

  private static void commitOnLeave(TextField field, java.util.function.Consumer<String> action) {
    field.setOnAction(event -> action.accept(field.getText()));
    field.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
      if (!hasFocus) {
        action.accept(field.getText());
      }
    });
  }

  /** SME: blank removes the value, otherwise a non-negative number rounded down to one decimal. */
  private void commitWidth(Map<String, Object> column, String key, String text) {
    Object before = column.get(key);
    if (text == null || text.isBlank()) {
      column.remove(key);
    }
    else {
      double parsed;
      try {
        parsed = Double.parseDouble(text.trim());
      }
      catch (NumberFormatException e) {
        return;
      }
      if (Double.isNaN(parsed) || parsed < 0) {
        return;
      }
      double rounded = Math.floor(parsed * 10) / 10;
      column.put(key, rounded == Math.floor(rounded) ? (Object) (int) rounded : (Object) rounded);
    }
    if (!java.util.Objects.equals(before, column.get(key))) {
      changed();
    }
  }

  private boolean canMove(List<Map<String, Object>> columns, int index, int offset) {
    int target = index + offset;
    if (target < 0 || target >= columns.size()) {
      return false;
    }
    return java.util.Objects.equals(columns.get(index).get("pinning"), columns.get(target).get("pinning"));
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
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, StudioBundle.get("content_settings.column_delete_confirm"),
        ButtonType.OK, ButtonType.CANCEL);
    if (alert.showAndWait().orElse(ButtonType.CANCEL) == ButtonType.OK) {
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
