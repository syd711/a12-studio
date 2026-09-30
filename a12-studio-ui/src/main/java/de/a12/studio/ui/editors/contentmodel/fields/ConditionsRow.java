package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.editors.contentmodel.ContentReferences;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The conditions of a Conditional element (SME's "Condition n" sections): each compares a field of the Document Model
 * with a value - "equal" or "not equal" - and the content is shown when all conditions hold. A condition is stored as
 * {@code {operator, fieldId, value}}; the kind of value editor depends on the field's data type (Yes / No / No data for a
 * boolean, Yes / No data for a confirmation, the values of an enumeration, text for the rest). Choosing another field
 * clears the value, as in SME. A value is optional: an empty one is not stored.
 */
public class ConditionsRow extends SettingRow {

  private static final String EQUAL = "equal";
  private static final String NOT_EQUAL = "not_equal";
  private static final String INVALID_STYLE = "content-setting-invalid";

  private final VBox conditions = new VBox(8);
  private final Button add = new Button();

  private List<Map<String, Object>> current = new ArrayList<>();
  private ContentElement element;

  public ConditionsRow() {
    add.setText(StudioBundle.get("content_settings.condition_add"));
    add.getStyleClass().add("primary-button");
    add.setOnAction(event -> edited(props -> {
      Map<String, Object> condition = new LinkedHashMap<>();
      condition.put("operator", EQUAL);
      current.add(condition);
      write(props);
      rebuild();
    }));
    addBelow(conditions);
    HBox addLine = new HBox(add);
    addLine.setAlignment(Pos.CENTER_LEFT);
    addBelow(addLine);
  }

  @Override
  @SuppressWarnings("unchecked")
  protected void load(@NonNull ContentProps props) {
    element = props.getElement();
    current = new ArrayList<>();
    if (props.get(getPath()) instanceof List<?> list) {
      for (Object entry : list) {
        if (entry instanceof Map<?, ?> map) {
          current.add((Map<String, Object>) map);
        }
      }
    }
    rebuild();
  }

  private void write(ContentProps props) {
    // Kept as a list even when empty: a Conditional without conditions has "conditions": [] in SME.
    props.set(getPath(), new ArrayList<>(current));
  }

  private void rebuild() {
    conditions.getChildren().clear();
    ContentReferences references = context() != null ? context().references() : null;
    for (int i = 0; i < current.size(); i++) {
      conditions.getChildren().add(block(i, current.get(i), references));
    }
  }

  private VBox block(int index, Map<String, Object> condition, @Nullable ContentReferences references) {
    VBox block = new VBox(4);
    block.getStyleClass().add("content-setting-group");
    Label title = new Label(StudioBundle.get("content_settings.condition_title", index + 1));
    title.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(title, Priority.ALWAYS);
    Button remove = new Button();
    remove.setGraphic(WidgetFactory.createIcon("mdi2t-trash-can-outline", 14, null));
    remove.getStyleClass().add("default-button");
    remove.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("content_settings.list_remove")));
    remove.setOnAction(event -> edited(props -> {
      current.remove(condition);
      write(props);
      rebuild();
    }));
    HBox header = new HBox(6, title, remove);
    header.setAlignment(Pos.CENTER_LEFT);

    String fieldId = condition.get("fieldId") instanceof String id && !id.isBlank() ? id : null;
    HBox valueLine = new HBox(6);
    valueLine.setAlignment(Pos.CENTER_LEFT);
    block.getChildren().addAll(header, fieldLine(condition, fieldId, references, valueLine), operatorLine(condition), valueLine);
    fillValueLine(valueLine, condition, fieldId, references);
    return block;
  }

  /** The field picker: the fields the element may reference, by path; an unavailable stored one stays, marked. */
  private HBox fieldLine(Map<String, Object> condition, @Nullable String fieldId, @Nullable ContentReferences references, HBox valueLine) {
    boolean bound = references != null && references.isBound();
    Map<String, String> labels = new HashMap<>();
    List<String> ids = new ArrayList<>();
    if (bound) {
      for (ContentReferences.Choice choice : references.fields(element)) {
        labels.put(choice.id(), choice.label());
        ids.add(choice.id());
      }
    }
    if (fieldId != null && !labels.containsKey(fieldId)) {
      labels.put(fieldId, StudioBundle.get("content_settings.reference_unavailable", bound ? references.labelOf(fieldId) : fieldId));
      ids.add(fieldId);
    }
    ComboBox<String> combo = new ComboBox<>();
    combo.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(combo, Priority.ALWAYS);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String id) {
        return id == null ? "" : labels.getOrDefault(id, id);
      }

      @Override
      public String fromString(String string) {
        return string;
      }
    });
    combo.getItems().setAll(ids);
    combo.setValue(fieldId);
    combo.setDisable(!bound && fieldId == null);
    combo.setPromptText(StudioBundle.get(bound ? "content_settings.reference_select" : "content_settings.reference_needs_document_model"));
    combo.valueProperty().addListener((observable, oldValue, id) ->
        edited(props -> {
          condition.remove("value");
          if (id == null || id.isBlank()) {
            condition.remove("fieldId");
          }
          else {
            condition.put("fieldId", id);
          }
          write(props);
          fillValueLine(valueLine, condition, id, references);
        }));
    Label label = new Label(StudioBundle.get("content_settings.condition_field"));
    label.setMinWidth(LABEL_WIDTH);
    HBox line = new HBox(LINE_SPACING, label, combo);
    line.setAlignment(Pos.CENTER_LEFT);
    return line;
  }

  private HBox operatorLine(Map<String, Object> condition) {
    ToggleGroup group = new ToggleGroup();
    ToggleButton equal = segment("content_settings.condition_equal", EQUAL, group, "first");
    ToggleButton notEqual = segment("content_settings.condition_not_equal", NOT_EQUAL, group, "last");
    group.selectToggle(NOT_EQUAL.equals(condition.get("operator")) ? notEqual : equal);
    group.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
      if (newToggle == null) {
        group.selectToggle(oldToggle);
        return;
      }
      edited(props -> {
        condition.put("operator", newToggle.getUserData());
        write(props);
      });
    });
    HBox segments = new HBox(equal, notEqual);
    segments.getStyleClass().add("content-setting-segments");
    Label label = new Label(StudioBundle.get("content_settings.condition_operator"));
    label.setMinWidth(LABEL_WIDTH);
    HBox line = new HBox(LINE_SPACING, label, segments);
    line.setAlignment(Pos.CENTER_LEFT);
    return line;
  }

  private static ToggleButton segment(String key, String operator, ToggleGroup group, String position) {
    ToggleButton button = new ToggleButton(StudioBundle.get(key));
    button.setUserData(operator);
    button.setToggleGroup(group);
    button.getStyleClass().addAll("content-setting-segment", position);
    return button;
  }

  /** The value editor that fits the data type of the chosen field. */
  private void fillValueLine(HBox line, Map<String, Object> condition, @Nullable String fieldId, @Nullable ContentReferences references) {
    line.getChildren().clear();
    Label label = new Label(StudioBundle.get("content_settings.condition_value"));
    label.setMinWidth(LABEL_WIDTH);
    line.getChildren().add(label);
    if (fieldId == null) {
      line.setDisable(true);
      return;
    }
    line.setDisable(false);
    ContentReferences.FieldInfo info = references != null ? references.fieldInfo(fieldId) : null;
    ContentReferences.ValueKind kind = info != null ? info.kind() : ContentReferences.ValueKind.OTHER;
    line.getChildren().add(switch (kind) {
      case BOOLEAN -> choice(condition, List.of(Boolean.TRUE, Boolean.FALSE, NO_DATA));
      case CONFIRM -> choice(condition, List.of(Boolean.TRUE, NO_DATA));
      case ENUMERATION -> choice(condition, new ArrayList<Object>(info.values()));
      default -> text(condition, kind == ContentReferences.ValueKind.NUMBER);
    });
  }

  // Stands for the stored value null ("no data"), which a combo box cannot hold as a selection.
  private static final Object NO_DATA = new Object();

  private ComboBox<Object> choice(Map<String, Object> condition, List<Object> options) {
    ComboBox<Object> combo = new ComboBox<>();
    combo.setMaxWidth(Double.MAX_VALUE);
    HBox.setHgrow(combo, Priority.ALWAYS);
    combo.getItems().setAll(options);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(Object value) {
        if (value == null) {
          return "";
        }
        if (value == NO_DATA) {
          return StudioBundle.get("content_settings.condition_no_data");
        }
        if (value instanceof Boolean flag) {
          return StudioBundle.get(flag ? "content_settings.condition_yes" : "content_settings.condition_no");
        }
        return String.valueOf(value);
      }

      @Override
      public Object fromString(String string) {
        return string;
      }
    });
    if (condition.containsKey("value")) {
      Object stored = condition.get("value");
      Object shown = stored == null ? NO_DATA : stored;
      if (!options.contains(shown)) {
        combo.getItems().add(shown);
      }
      combo.setValue(shown);
    }
    combo.valueProperty().addListener((observable, oldValue, value) ->
        edited(props -> {
          if (value == null) {
            condition.remove("value");
          }
          else {
            condition.put("value", value == NO_DATA ? null : value);
          }
          write(props);
        }));
    return combo;
  }

  private TextField text(Map<String, Object> condition, boolean number) {
    TextField field = new TextField(condition.get("value") == null ? "" : String.valueOf(condition.get("value")));
    HBox.setHgrow(field, Priority.ALWAYS);
    field.setPromptText(StudioBundle.get("content_settings.condition_value_prompt"));
    field.textProperty().addListener((observable, oldValue, text) -> {
      field.getStyleClass().remove(INVALID_STYLE);
      Object value = text.isEmpty() ? null : text;
      if (number && !text.isEmpty()) {
        value = parseNumber(text);
        if (value == null) {
          // A value that is no number is not stored; the row says so and keeps what was stored before.
          field.getStyleClass().add(INVALID_STYLE);
          return;
        }
      }
      Object stored = value;
      edited(props -> {
        if (stored == null) {
          condition.remove("value");
        }
        else {
          condition.put("value", stored);
        }
        write(props);
      });
    });
    return field;
  }

  private static @Nullable Number parseNumber(String text) {
    try {
      return Long.parseLong(text.trim());
    }
    catch (NumberFormatException notAnInteger) {
      try {
        return Double.parseDouble(text.trim());
      }
      catch (NumberFormatException notANumber) {
        return null;
      }
    }
  }
}
