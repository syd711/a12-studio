package de.a12.studio.ui.editors.contentmodel.dialogs;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The edit dialog of one column of a Content Model {@code Table}, opened from the Columns panel
 * ({@link de.a12.studio.ui.editors.contentmodel.TableColumnsPanelController}). It carries every setting SME shows for
 * a column: pin direction and action-column flag (inline in SME's list), default width, minimum width while resizing
 * is enabled, fixed width, and the horizontal and vertical alignment of the general, head, body and foot areas.
 * Works on a copy of the column and hands the edited copy back on OK ({@link #getResult()}); values that stay at
 * their default are removed from it, as SME's formatters do, and keys the dialog does not know are kept.
 */
public class TableColumnDialogController implements DialogController {

  static final List<String> PINNINGS = List.of("none", "left", "right");
  static final List<String> HORIZONTAL = List.of("auto", "left", "center", "right");
  static final List<String> VERTICAL = List.of("auto", "top", "middle", "bottom");
  static final List<String> AREAS = List.of("general", "head", "body", "foot");

  @FXML
  private ComboBox<String> pinningCombo;
  @FXML
  private CheckBox actionColumnCheck;
  @FXML
  private TextField widthField;
  @FXML
  private VBox minWidthBox;
  @FXML
  private TextField minWidthField;
  @FXML
  private CheckBox fixedWidthCheck;
  @FXML
  private Label errorLabel;
  @FXML
  private GridPane horizontalGrid;
  @FXML
  private GridPane verticalGrid;
  @FXML
  private Button okButton;

  private final Map<String, ComboBox<String>> horizontalCombos = new LinkedHashMap<>();
  private final Map<String, ComboBox<String>> verticalCombos = new LinkedHashMap<>();

  private Stage stage;

  private Map<String, Object> source;

  private boolean minWidthShown;

  private Map<String, Object> result;

  @FXML
  private void initialize() {
    pinningCombo.getItems().setAll(PINNINGS);
    pinningCombo.setConverter(labels("content_settings.pinning_"));
    fillAlignments(horizontalGrid, horizontalCombos, HORIZONTAL);
    fillAlignments(verticalGrid, verticalCombos, VERTICAL);
    minWidthField.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("content_settings.column_min_width_hint")));

    errorLabel.textProperty().bind(Bindings.createStringBinding(this::validationMessage,
        widthField.textProperty(), minWidthField.textProperty()));
    errorLabel.visibleProperty().bind(errorLabel.textProperty().isNotEmpty());
    errorLabel.managedProperty().bind(errorLabel.visibleProperty());
    okButton.disableProperty().bind(errorLabel.visibleProperty());
  }

  private static void fillAlignments(GridPane grid, Map<String, ComboBox<String>> combos, List<String> values) {
    for (int i = 0; i < AREAS.size(); i++) {
      String area = AREAS.get(i);
      ComboBox<String> combo = new ComboBox<>();
      combo.getItems().setAll(values);
      combo.setConverter(labels("content_settings.alignment_"));
      combo.setMaxWidth(Double.MAX_VALUE);
      combos.put(area, combo);
      VBox cell = new VBox(2.0, labelOf(StudioBundle.get("content_settings.area_" + area)), combo);
      GridPane.setHgrow(cell, Priority.ALWAYS);
      grid.add(cell, i % 2, i / 2);
    }
  }

  private static Label labelOf(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("field-label");
    return label;
  }

  /**
   * @param column          the column to edit; not modified
   * @param resizingEnabled whether the table has "Enable resizing" on, which is when SME offers the minimum width
   */
  void init(Stage stage, @NonNull Map<String, Object> column, boolean resizingEnabled) {
    this.stage = stage;
    this.source = new LinkedHashMap<>(column);
    this.minWidthShown = resizingEnabled;

    pinningCombo.setValue(column.get("pinning") instanceof String pinning && PINNINGS.contains(pinning) ? pinning : "none");
    actionColumnCheck.setSelected(Boolean.TRUE.equals(column.get("actionColumn")));
    widthField.setText(text(column.get("width")));
    minWidthField.setText(text(column.get("minResizeWidth")));
    minWidthBox.setVisible(resizingEnabled);
    minWidthBox.setManaged(resizingEnabled);
    fixedWidthCheck.setSelected(Boolean.TRUE.equals(column.get("fixedWidth")));
    showAlignments(column.get("horizontalAlignment"), horizontalCombos);
    showAlignments(column.get("verticalAlignment"), verticalCombos);
  }

  private static void showAlignments(Object stored, Map<String, ComboBox<String>> combos) {
    combos.forEach((area, combo) ->
        combo.setValue(stored instanceof Map<?, ?> map && map.get(area) instanceof String value ? value : "auto"));
  }

  private static String text(Object value) {
    return value == null ? "" : String.valueOf(value);
  }

  private String validationMessage() {
    if (!isValidWidth(widthField.getText())) {
      return StudioBundle.get("content_settings.column_error_width");
    }
    if (minWidthShown && !isValidWidth(minWidthField.getText())) {
      return StudioBundle.get("content_settings.column_error_min_width");
    }
    return "";
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    if (okButton.isDisabled()) {
      return;
    }
    Map<String, Object> column = new LinkedHashMap<>(source);
    String pinning = pinningCombo.getValue();
    put(column, "pinning", "none".equals(pinning) ? null : pinning);
    put(column, "actionColumn", actionColumnCheck.isSelected() ? Boolean.TRUE : null);
    put(column, "width", parseWidth(widthField.getText()));
    if (minWidthShown) {
      put(column, "minResizeWidth", parseWidth(minWidthField.getText()));
    }
    put(column, "fixedWidth", fixedWidthCheck.isSelected() ? Boolean.TRUE : null);
    put(column, "horizontalAlignment", alignments(horizontalCombos));
    put(column, "verticalAlignment", alignments(verticalCombos));
    result = column;
    stage.close();
  }

  private static void put(Map<String, Object> column, String key, Object value) {
    if (value == null) {
      column.remove(key);
    }
    else {
      column.put(key, value);
    }
  }

  /** The areas that differ from "auto", or null when none does (SME then drops the whole key). */
  private static Map<String, Object> alignments(Map<String, ComboBox<String>> combos) {
    Map<String, Object> areas = new LinkedHashMap<>();
    combos.forEach((area, combo) -> {
      if (combo.getValue() != null && !"auto".equals(combo.getValue())) {
        areas.put(area, combo.getValue());
      }
    });
    return areas.isEmpty() ? null : areas;
  }

  /** The edited copy of the column, or null when the dialog was cancelled. */
  Map<String, Object> getResult() {
    return result;
  }

  static boolean isValidWidth(String text) {
    try {
      parseWidth(text);
      return true;
    }
    catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * SME's width rule: blank means "no value" (null), otherwise a non-negative number rounded down to one decimal,
   * kept as an integer when it is a whole number so {@code 1} stays {@code 1} in the JSON.
   *
   * @throws NumberFormatException when the text is not a non-negative finite number
   */
  static Object parseWidth(String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    double parsed = Double.parseDouble(text.trim());
    if (Double.isNaN(parsed) || Double.isInfinite(parsed) || parsed < 0) {
      throw new NumberFormatException(text);
    }
    double rounded = Math.floor(parsed * 10) / 10;
    return rounded == Math.floor(rounded) ? (Object) (int) rounded : (Object) rounded;
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
}
