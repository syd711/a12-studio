package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ElementType {

  GROUP("Group"),
  FIELD("Field"),
  RULE("Rule"),
  COMPUTATION("Computation"),
  // Falls back to this instead of throwing so unrecognized/future element types (deserialized as
  // GenericElement, see Element's defaultImpl) still load instead of failing the whole document.
  OTHER("Other");

  private final String value;

  ElementType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ElementType fromValue(String value) {
    for (ElementType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    return OTHER;
  }
}
