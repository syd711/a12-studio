package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.scene.control.Button;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A color setting (Text, Background, Border color). The value is written as SME writes it, {@code rgb(r, g, b)} or
 * {@code rgba(r, g, b, a)}; any CSS color already in the file (hex, names, {@code rgb()}) is understood. The clear
 * button removes the key. {@code alpha="false"} (SME's "disabled opacity" for text colors) makes the color opaque.
 */
public class ColorRow extends SettingRow {

  private final ColorPicker picker = new ColorPicker(Color.TRANSPARENT);
  private final Button clear = new Button();

  private boolean alpha = true;
  private boolean updatingPicker;

  public ColorRow() {
    picker.setMaxWidth(Double.MAX_VALUE);
    clear.setGraphic(WidgetFactory.createIcon("mdi2c-close", 12, null));
    clear.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("content_settings.clear_color")));
    clear.setVisible(false);
    clear.setManaged(false);
    controls().getChildren().addAll(picker, clear);

    picker.valueProperty().addListener((observable, oldValue, color) -> {
      if (updatingPicker || color == null) {
        return;
      }
      showClear(true);
      edited(props -> props.set(getPath(), format(color, alpha)));
    });
    clear.setOnAction(event -> {
      setPicker(Color.TRANSPARENT);
      showClear(false);
      edited(props -> props.remove(getPath()));
    });
  }

  public boolean isAlpha() {
    return alpha;
  }

  /** Whether translucent colors are allowed; {@code false} always writes an opaque color. */
  public void setAlpha(boolean alpha) {
    this.alpha = alpha;
  }

  private void showClear(boolean visible) {
    clear.setVisible(visible);
    clear.setManaged(visible);
  }

  private void setPicker(Color color) {
    updatingPicker = true;
    try {
      picker.setValue(color);
    }
    finally {
      updatingPicker = false;
    }
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    String raw = props.getString(getPath());
    Color parsed = parse(raw);
    setPicker(parsed != null ? parsed : Color.TRANSPARENT);
    showClear(raw != null);
  }

  /** Any CSS color JavaFX understands (hex, names, rgb(), rgba(), hsl()), or {@code null} when it is none. */
  static @Nullable Color parse(@Nullable String text) {
    if (text == null || text.isBlank()) {
      return null;
    }
    try {
      return Color.web(text.trim());
    }
    catch (IllegalArgumentException e) {
      return null;
    }
  }

  /** {@code rgb(51, 51, 51)}, or {@code rgba(51, 51, 51, 0.5)} for a translucent color, as SME writes colors. */
  static String format(Color color, boolean withOpacity) {
    int red = (int) Math.round(color.getRed() * 255);
    int green = (int) Math.round(color.getGreen() * 255);
    int blue = (int) Math.round(color.getBlue() * 255);
    double alpha = withOpacity ? color.getOpacity() : 1.0;
    if (alpha >= 0.995) {
      return "rgb(" + red + ", " + green + ", " + blue + ")";
    }
    String alphaText = BigDecimal.valueOf(alpha).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    return "rgba(" + red + ", " + green + ", " + blue + ", " + alphaText + ")";
  }
}
