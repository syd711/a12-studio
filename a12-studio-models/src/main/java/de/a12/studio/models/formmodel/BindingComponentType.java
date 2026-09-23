package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Which widget a {@link BindingComponent} renders the bound relationship's candidates/links with - SME's
 * {@code I_BindingComponent.json} {@code name} field's enumeration values.
 */
public enum BindingComponentType {

  DROP_DOWN_SELECTION("DropDownSelection"),
  DUAL_PANE_SELECTION("DualPaneSelection"),
  TABLE_LIST("TableList");

  private final String value;

  BindingComponentType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static BindingComponentType fromValue(String value) {
    for (BindingComponentType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return null;
  }
}
