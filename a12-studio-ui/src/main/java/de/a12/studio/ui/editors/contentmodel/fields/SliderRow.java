package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * A number setting shown as a slider with its value (SME's Image opacity: 0 to 1 in steps of 0.1). The number is
 * stored as text, like SME stores it, and removed again when it equals {@code defaultValue}.
 */
public class SliderRow extends SettingRow {

  private final Slider slider = new Slider(0, 1, 1);
  private final Label valueLabel = new Label();

  private double defaultValue = 1;
  private double step = 0.1;

  public SliderRow() {
    slider.setMajorTickUnit(0.1);
    slider.setBlockIncrement(0.1);
    HBox.setHgrow(slider, Priority.ALWAYS);
    valueLabel.setMinWidth(30);
    controls().getChildren().addAll(slider, valueLabel);
    slider.valueProperty().addListener((observable, oldValue, raw) -> {
      double value = snap(raw.doubleValue());
      valueLabel.setText(LengthEditor.format(value));
      edited(props -> {
        if (Math.abs(value - defaultValue) < 1e-9) {
          props.remove(getPath());
        }
        else {
          props.set(getPath(), LengthEditor.format(value));
        }
      });
    });
  }

  public double getMin() {
    return slider.getMin();
  }

  public void setMin(double min) {
    slider.setMin(min);
  }

  public double getMax() {
    return slider.getMax();
  }

  public void setMax(double max) {
    slider.setMax(max);
  }

  public double getStep() {
    return step;
  }

  public void setStep(double step) {
    this.step = step;
    slider.setBlockIncrement(step);
    slider.setMajorTickUnit(step);
  }

  public double getDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(double defaultValue) {
    this.defaultValue = defaultValue;
  }

  private double snap(double value) {
    return BigDecimal.valueOf(Math.round(value / step) * step).setScale(4, RoundingMode.HALF_UP).doubleValue();
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    double value = defaultValue;
    String stored = props.getString(getPath());
    if (stored != null) {
      try {
        value = Double.parseDouble(stored.trim());
      }
      catch (NumberFormatException e) {
        value = defaultValue;
      }
    }
    slider.setValue(value);
    valueLabel.setText(LengthEditor.format(snap(value)));
  }
}
