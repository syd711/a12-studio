package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum LocalizedTextType {

  MULTILINGUAL("Multilingual"),
  EXPRESSION("Expression"),
  // Falls back to this instead of throwing so unrecognized/future text types (deserialized as
  // GenericLocalizedText, see LocalizedText's defaultImpl) still load instead of failing the whole document.
  OTHER("Other");

  private final String value;

  LocalizedTextType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static LocalizedTextType fromValue(String value) {
    for (LocalizedTextType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return OTHER;
  }
}
