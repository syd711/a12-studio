package de.a12.studio.ui.editors.contentmodel.fields;

import javafx.beans.DefaultProperty;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Groups {@link SettingRow}s that apply to the same element types, so FXML does not repeat {@code types} on every
 * row. Rows inside a group without their own {@code types} follow the group; a group without {@code types} applies
 * to every type.
 */
@DefaultProperty("children")
public class SettingGroup extends VBox {

  private Set<String> types = Set.of();

  public SettingGroup() {
    super(2);
    getStyleClass().add("content-setting-group");
  }

  public String getTypes() {
    return String.join(",", types);
  }

  public void setTypes(String types) {
    Set<String> parsed = new LinkedHashSet<>();
    if (types != null) {
      Arrays.stream(types.split(",")).map(String::trim).filter(type -> !type.isEmpty()).forEach(parsed::add);
    }
    this.types = parsed;
  }

  public boolean appliesTo(String type) {
    return types.isEmpty() || types.contains(type);
  }
}
