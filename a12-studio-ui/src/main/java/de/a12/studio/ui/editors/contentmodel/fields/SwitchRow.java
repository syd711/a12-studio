package de.a12.studio.ui.editors.contentmodel.fields;

import de.a12.studio.models.contentmodel.ContentProps;
import javafx.scene.control.CheckBox;
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
