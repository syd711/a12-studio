package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ButtonType {

  NAVIGATION("NAVIGATION"),
  EVENT("EVENT"),
  // Falls back to this instead of throwing so unrecognized/future button types (deserialized as
  // GenericButton, see Button's defaultImpl) still load instead of failing the whole document.
  OTHER("OTHER");

  private final String value;

  ButtonType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ButtonType fromValue(String value) {
    for (ButtonType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return OTHER;
  }
}
