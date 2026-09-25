package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.editors.contentmodel.fields.LengthEditor;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * SME's "Responsive behavior" section of the Grid: for a Row the column layout ({@code layoutConfig.layout}), offsets
 * and spans per breakpoint (large/medium/small), for a Column its width ({@code size}) per breakpoint and its height
 * ({@code height}) per breakpoint including extra small. Numbers are column counts from 0 to 12 and applied when a
 * field is left or confirmed with Enter. As in SME, the large value is required once a smaller one is given, clearing
 * the row's layout drops its whole responsive config, and Row layout and Column widths exclude each other (the
 * disabled side is the one SME disables).
 */
public class ResponsivePanelController extends AbstractContentSettingsPanel {

  private static final List<String> BREAKPOINTS = List.of("lg", "md", "sm");
  private static final List<String> CONFIGS = List.of("layout", "offsets", "spans");
  private static final Pattern NUMBERS_ONLY = Pattern.compile("^[\\d_,.;| ]+$");
  private static final String INVALID_STYLE = "content-setting-invalid";

  @FXML
  private VBox rowBox;

  @FXML
  private VBox columnBox;

  private final Map<String, TextField> rowFields = new LinkedHashMap<>();
  private final Map<String, TextField> sizeFields = new LinkedHashMap<>();
  private final Map<String, LengthEditor> heightEditors = new LinkedHashMap<>();
  private boolean populating;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);
    for (String breakpoint : BREAKPOINTS) {
      rowBox.getChildren().add(heading(breakpointLabel(breakpoint)));
      for (String config : CONFIGS) {
        TextField field = numberField();
        rowFields.put(config + "." + breakpoint, field);
        onCommit(field, text -> commitRow(config, breakpoint, field, text));
        rowBox.getChildren().add(line(StudioBundle.get("content_settings.responsive_" + config), field));
      }
    }
    columnBox.getChildren().add(heading(StudioBundle.get("content_settings.width")));
    for (String breakpoint : BREAKPOINTS) {
      TextField field = numberField();
      sizeFields.put(breakpoint, field);
      onCommit(field, text -> commitSize(breakpoint, field, text));
      columnBox.getChildren().add(line(breakpointLabel(breakpoint), field));
    }
    columnBox.getChildren().add(heading(StudioBundle.get("content_settings.height")));
    for (String breakpoint : List.of("lg", "md", "sm", "xs")) {
      LengthEditor editor = new LengthEditor();
      editor.configure(List.of("auto", "fit-content"), LengthEditor.parseUnits("px:80,%:100"), true, List.of());
      editor.setOnValue(value -> commitHeight(breakpoint, value));
      heightEditors.put(breakpoint, editor);
      columnBox.getChildren().add(line(breakpointLabel(breakpoint), editor));
    }
  }

  private static String breakpointLabel(String breakpoint) {
    return StudioBundle.get("content_settings.breakpoint_" + breakpoint);
  }

  private static Label heading(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("content-setting-heading");
    label.setPadding(new Insets(6, 0, 0, 0));
    return label;
  }

  private static TextField numberField() {
    TextField field = new TextField();
    field.setPromptText("6");
    return field;
  }

  private static HBox line(String label, javafx.scene.Node control) {
    Label text = new Label(label);
    text.setMinWidth(118);
    HBox line = new HBox(8, text, control);
    line.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(control, Priority.ALWAYS);
    return line;
  }

  private static void onCommit(TextField field, Consumer<String> action) {
    field.setOnAction(event -> action.accept(field.getText()));
    field.focusedProperty().addListener((observable, hadFocus, hasFocus) -> {
      if (!hasFocus) {
        action.accept(field.getText());
      }
    });
  }

  @Override
  protected boolean appliesTo(String type) {
    return "GridRow".equals(type) || "GridColumn".equals(type);
  }

  @Override
  protected void populate(@NonNull ContentElement element) {
    boolean isRow = "GridRow".equals(element.getType());
    rowBox.setVisible(isRow);
    rowBox.setManaged(isRow);
    columnBox.setVisible(!isRow);
    columnBox.setManaged(!isRow);
    populating = true;
    try {
      ContentProps props = new ContentProps(element);
      if (isRow) {
        boolean hasColumnSizes = element.getChildren() != null && element.getChildren().stream()
            .anyMatch(column -> new ContentProps(column).getMap("size") != null);
        for (String config : CONFIGS) {
          for (String breakpoint : BREAKPOINTS) {
            TextField field = rowFields.get(config + "." + breakpoint);
            field.getStyleClass().remove(INVALID_STYLE);
            field.setText(formatNumbers(props.get("layoutConfig." + config + "." + breakpoint)));
            field.setDisable(hasColumnSizes);
          }
        }
      }
      else {
        ContentElement parent = context() != null ? context().parentOf(element) : null;
        boolean rowHasLayout = parent != null && new ContentProps(parent).getMap("layoutConfig") != null;
        for (String breakpoint : BREAKPOINTS) {
          TextField field = sizeFields.get(breakpoint);
          field.getStyleClass().remove(INVALID_STYLE);
          String size = props.getString("size." + breakpoint);
          field.setText(size != null ? size : "");
          field.setDisable(rowHasLayout);
        }
        heightEditors.forEach((breakpoint, editor) -> editor.setValue(props.getString("height." + breakpoint)));
      }
    }
    finally {
      populating = false;
    }
  }

  // ---------------------------------------------------------------------------------------------- Row

  private void commitRow(String config, String breakpoint, TextField field, String text) {
    ContentElement element = currentElement();
    if (populating || element == null) {
      return;
    }
    Parsed parsed = parseNumbers(text, true);
    field.getStyleClass().remove(INVALID_STYLE);
    if (parsed.error()) {
      field.getStyleClass().add(INVALID_STYLE);
      return;
    }
    ContentProps props = new ContentProps(element);
    Map<String, Object> layoutConfig = copy(props.getMap("layoutConfig"));
    Map<String, Object> responsive = copy(asMap(layoutConfig.get(config)));
    if (parsed.numbers() == null) {
      responsive.remove(breakpoint);
    }
    else {
      responsive.put(breakpoint, new ArrayList<Object>(parsed.numbers()));
    }
    // SME: the large value is required once a smaller one is given.
    if (!responsive.containsKey("lg") && !responsive.isEmpty()) {
      field.getStyleClass().add(INVALID_STYLE);
      return;
    }
    if (responsive.isEmpty()) {
      layoutConfig.remove(config);
    }
    else {
      layoutConfig.put(config, ordered(responsive, BREAKPOINTS));
    }
    Object newValue = layoutConfig.containsKey("layout") ? ordered(layoutConfig, CONFIGS) : null;
    if (java.util.Objects.equals(newValue, props.get("layoutConfig"))) {
      return;
    }
    props.set("layoutConfig", newValue);
    changed();
  }

  // ------------------------------------------------------------------------------------------- Column

  private void commitSize(String breakpoint, TextField field, String text) {
    ContentElement element = currentElement();
    if (populating || element == null) {
      return;
    }
    Parsed parsed = parseNumbers(text, false);
    field.getStyleClass().remove(INVALID_STYLE);
    if (parsed.error()) {
      field.getStyleClass().add(INVALID_STYLE);
      return;
    }
    ContentProps props = new ContentProps(element);
    Map<String, Object> size = copy(props.getMap("size"));
    if (parsed.numbers() == null) {
      size.remove(breakpoint);
    }
    else {
      size.put(breakpoint, parsed.numbers().get(0));
    }
    if (!size.isEmpty() && !size.containsKey("lg")) {
      field.getStyleClass().add(INVALID_STYLE);
      return;
    }
    Object newValue = size.isEmpty() ? null : ordered(size, BREAKPOINTS);
    if (java.util.Objects.equals(newValue, props.get("size"))) {
      return;
    }
    props.set("size", newValue);
    changed();
  }

  private void commitHeight(String breakpoint, String value) {
    ContentElement element = currentElement();
    if (populating || element == null) {
      return;
    }
    ContentProps props = new ContentProps(element);
    Map<String, Object> height = copy(props.getMap("height"));
    if (value == null) {
      height.remove(breakpoint);
    }
    else {
      height.put(breakpoint, value);
    }
    props.set("height", height.isEmpty() ? null : ordered(height, List.of("lg", "md", "sm", "xs")));
    changed();
  }

  // --------------------------------------------------------------------------------------------- utils

  private record Parsed(List<Integer> numbers, boolean error) {
  }

  /** SME's column number transformers: blank means unset, otherwise integers from 0 to 12 (a list for Rows). */
  private static Parsed parseNumbers(String text, boolean list) {
    String trimmed = text == null ? "" : text.trim();
    if (trimmed.isEmpty()) {
      return new Parsed(null, false);
    }
    if (list ? !NUMBERS_ONLY.matcher(trimmed).matches() : !trimmed.matches("\\d+")) {
      return new Parsed(null, true);
    }
    List<Integer> numbers = new ArrayList<>();
    java.util.regex.Matcher matcher = Pattern.compile("\\d+").matcher(trimmed);
    while (matcher.find()) {
      int number = Integer.parseInt(matcher.group());
      if (number > 12) {
        return new Parsed(null, true);
      }
      numbers.add(number);
    }
    return new Parsed(numbers, false);
  }

  private static String formatNumbers(Object value) {
    if (value instanceof List<?> list) {
      return list.stream().map(String::valueOf).collect(Collectors.joining(" "));
    }
    return value == null ? "" : String.valueOf(value);
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> asMap(Object value) {
    return value instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
  }

  private static Map<String, Object> copy(Map<String, Object> source) {
    return source == null ? new LinkedHashMap<>() : new LinkedHashMap<>(source);
  }

  private static Map<String, Object> ordered(Map<String, Object> source, List<String> order) {
    Map<String, Object> result = new LinkedHashMap<>();
    order.stream().filter(source::containsKey).forEach(key -> result.put(key, source.get(key)));
    return result;
  }
}
