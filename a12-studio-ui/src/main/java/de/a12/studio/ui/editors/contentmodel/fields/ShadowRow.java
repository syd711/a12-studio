package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.util.StudioBundle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SME's shadow setting ({@code style.boxShadow}): style (inside/outside), color and the horizontal offset, vertical
 * offset, blur and spread in pixels, stored as the CSS shorthand {@code [inset] Xpx Ypx Bpx Spx color}. {@code
 * outsideOnly="true"} (images) hides the style choice. A value that does not have that shape shows as zeros and is
 * left alone until one of the fields is edited; "Reset" removes the shadow.
 */
public class ShadowRow extends SettingRow {

  private static final List<String> FIELDS = List.of("offsetX", "offsetY", "blur", "spread");
  private static final String INVALID_STYLE = "content-setting-invalid";

  private final ToggleGroup styles = new ToggleGroup();
  private final ToggleButton outside = new ToggleButton(StudioBundle.get("content_settings.shadow_outside"));
  private final ToggleButton inside = new ToggleButton(StudioBundle.get("content_settings.shadow_inside"));
  private final HBox styleLine = new HBox();
  private final ColorPicker colorPicker = new ColorPicker(Color.BLACK);
  private final Map<String, TextField> numbers = new LinkedHashMap<>();
  private final VBox body = new VBox(4);

  private boolean outsideOnly;
  private boolean updating;

  public ShadowRow() {
    setLabel("");
    outside.setToggleGroup(styles);
    inside.setToggleGroup(styles);
    for (ToggleButton button : List.of(outside, inside)) {
      button.getStyleClass().add("content-setting-segment");
    }
    outside.getStyleClass().add("first");
    inside.getStyleClass().add("last");
    styleLine.getStyleClass().add("content-setting-segments");
    styleLine.getChildren().addAll(outside, inside);
    styles.selectedToggleProperty().addListener((observable, oldToggle, newToggle) -> {
      if (updating) {
        return;
      }
      if (newToggle == null) {
        if (oldToggle != null) {
          styles.selectToggle(oldToggle);
        }
        return;
      }
      write();
    });

    body.getChildren().add(line("content_settings.shadow_style", styleLine));
    colorPicker.setMaxWidth(Double.MAX_VALUE);
    colorPicker.valueProperty().addListener((observable, oldValue, color) -> {
      if (!updating) {
        write();
      }
    });
    body.getChildren().add(line("content_settings.color", colorPicker));
    for (String field : FIELDS) {
      TextField number = new TextField("0");
      number.setPrefWidth(80);
      number.setAlignment(Pos.CENTER_RIGHT);
      number.textProperty().addListener((observable, oldValue, text) -> {
        if (!updating) {
          write();
        }
      });
      numbers.put(field, number);
      body.getChildren().add(line("content_settings.shadow_" + field, number));
    }
    Button reset = new Button(StudioBundle.get("content_settings.shadow_reset"));
    reset.setOnAction(event -> {
      edited(props -> props.remove(getPath()));
      showShadow(null);
    });
    body.getChildren().add(reset);
    body.setPadding(new Insets(0, 0, 0, 0));
    addBelow(body);
  }

  public boolean isOutsideOnly() {
    return outsideOnly;
  }

  public void setOutsideOnly(boolean outsideOnly) {
    this.outsideOnly = outsideOnly;
    body.getChildren().get(0).setVisible(!outsideOnly);
    body.getChildren().get(0).setManaged(!outsideOnly);
  }

  private static HBox line(String labelKey, javafx.scene.Node control) {
    Label label = new Label(StudioBundle.get(labelKey));
    label.setMinWidth(118);
    HBox line = new HBox(8, label, control);
    line.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(control, control instanceof ColorPicker ? Priority.ALWAYS : Priority.NEVER);
    return line;
  }

  private void write() {
    Double[] values = new Double[FIELDS.size()];
    boolean valid = true;
    for (int i = 0; i < FIELDS.size(); i++) {
      TextField field = numbers.get(FIELDS.get(i));
      field.getStyleClass().remove(INVALID_STYLE);
      values[i] = parse(field.getText());
      if (values[i] == null) {
        field.getStyleClass().add(INVALID_STYLE);
        valid = false;
      }
    }
    if (!valid) {
      return;
    }
    StringBuilder shadow = new StringBuilder();
    if (!outsideOnly && styles.getSelectedToggle() == inside) {
      shadow.append("inset ");
    }
    for (Double value : values) {
      shadow.append(LengthEditor.format(value)).append("px ");
    }
    shadow.append(ColorRow.format(colorPicker.getValue(), true));
    edited(props -> props.set(getPath(), shadow.toString().trim()));
  }

  private static Double parse(String text) {
    try {
      double value = Double.parseDouble(text.trim());
      return Double.isFinite(value) ? value : null;
    }
    catch (NumberFormatException e) {
      return null;
    }
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    showShadow(props.getString(getPath()));
  }

  /** Shows the shadow text {@code raw} (or the defaults for {@code null}/an unexpected shape) in the fields. */
  private void showShadow(String raw) {
    boolean isInside = false;
    Double[] values = {0.0, 0.0, 0.0, 0.0};
    Color color = Color.BLACK;
    if (raw != null) {
      // Same normalization as SME: collapse blanks, and keep "rgb(0, 0, 0, 1)" as one token.
      String[] tokens = raw.trim().replaceAll("\\s+", " ").replaceAll(",\\s+", ",").split(" ");
      if (tokens.length == 5 || tokens.length == 6) {
        int offset = tokens.length == 6 ? 1 : 0;
        isInside = tokens.length == 6;
        for (int i = 0; i < 4; i++) {
          Double parsed = parse(tokens[offset + i].replace("px", ""));
          values[i] = parsed != null ? parsed : 0.0;
        }
        Color parsedColor = ColorRow.parse(tokens[offset + 4]);
        color = parsedColor != null ? parsedColor : Color.BLACK;
      }
    }
    updating = true;
    try {
      styles.selectToggle(isInside ? inside : outside);
      colorPicker.setValue(color);
      for (int i = 0; i < FIELDS.size(); i++) {
        TextField field = numbers.get(FIELDS.get(i));
        field.getStyleClass().remove(INVALID_STYLE);
        field.setText(LengthEditor.format(values[i]));
      }
    }
    finally {
      updating = false;
    }
  }
}
