package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import org.jspecify.annotations.NonNull;

import java.math.BigDecimal;

/**
 * A whole-number setting (SME's numeric input, e.g. the years of a date picker): stored as a JSON number, removed
 * when the field is emptied. Text that is not a whole number is not stored - the previous value stays and the field
 * is marked invalid, like the other rows do for values they refuse.
 */
public class NumberRow extends SettingRow {

  private static final String INVALID_STYLE = "content-setting-invalid";

  private final TextField input = new TextField();

  public NumberRow() {
    input.setPrefColumnCount(8);
    HBox.setHgrow(input, Priority.SOMETIMES);
    controls().getChildren().add(input);
    input.textProperty().addListener((observable, oldValue, text) -> onTyped(text == null ? "" : text.strip()));
  }

  public String getPrompt() {
    return input.getPromptText();
  }

  public void setPrompt(String prompt) {
    input.setPromptText(prompt);
  }

  private void onTyped(String text) {
    input.getStyleClass().remove(INVALID_STYLE);
    if (text.isEmpty()) {
      edited(props -> props.set(getPath(), null));
      return;
    }
    try {
      long value = Long.parseLong(text);
      edited(props -> props.set(getPath(), value));
    }
    catch (NumberFormatException notANumber) {
      if (!isLoading()) {
        input.getStyleClass().add(INVALID_STYLE);
      }
    }
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    input.getStyleClass().remove(INVALID_STYLE);
    Object value = props.get(getPath());
    // A number that came from a hand-edited file may have a fraction (2000.0): show it without one when it is whole.
    String text = value instanceof Number number ? new BigDecimal(number.toString()).stripTrailingZeros().toPlainString()
        : value instanceof String string ? string : "";
    input.setText(text);
  }
}
