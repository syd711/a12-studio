package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ContentDisplay;
import org.jspecify.annotations.NonNull;

/**
 * A boolean setting (SME's switch). A flag whose default is {@code false} is stored as {@code true} and removed
 * again when switched off; a flag whose {@code defaultValue} is {@code true} (e.g. "Enable resizing") records an
 * explicit {@code false}, see {@link ContentProps#setFlag}.
 */
public class SwitchRow extends SettingRow {

  private final CheckBox checkBox = new CheckBox();

  private boolean defaultValue;

  public SwitchRow() {
    controls().getChildren().add(checkBox);
    checkBox.selectedProperty().addListener((observable, oldValue, selected) ->
        edited(props -> props.setFlag(getPath(), selected, defaultValue)));
  }

  public String getText() {
    return checkBox.getText();
  }

  /** Puts the label on the checkbox itself instead of in the label column, for long labels that need the full width. */
  public void setText(String text) {
    checkBox.setText(text);
    updateCheckBoxHint();
  }

  /** With the label on the checkbox (see {@link #setText}), the hint's tooltip and info icon go on the checkbox too. */
  @Override
  public void setHint(String hint) {
    super.setHint(hint);
    updateCheckBoxHint();
  }

  private void updateCheckBoxHint() {
    String hint = getHint();
    boolean show = hint != null && !hint.isBlank() && checkBox.getText() != null && !checkBox.getText().isBlank();
    checkBox.setTooltip(show ? WidgetFactory.createTooltip(hint) : null);
    checkBox.setGraphic(show ? createInfoIcon() : null);
    checkBox.setContentDisplay(ContentDisplay.RIGHT);
  }

  public boolean isDefaultValue() {
    return defaultValue;
  }

  public void setDefaultValue(boolean defaultValue) {
    this.defaultValue = defaultValue;
  }

  @Override
  protected void load(@NonNull ContentProps props) {
    checkBox.setSelected(props.getBoolean(getPath(), defaultValue));
  }
}
