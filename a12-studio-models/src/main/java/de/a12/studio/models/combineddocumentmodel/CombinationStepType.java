package de.a12.studio.models.combineddocumentmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CombinationStepType {

  ADDITION("Addition"),
  SELECTION("Selection"),
  DECORATION_FOR_FIELDS("DecorationForFields"),
  DECORATION_FOR_GROUPS("DecorationForGroups");

  private final String value;

  CombinationStepType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static CombinationStepType fromValue(String value) {
    for (CombinationStepType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown combination step type: " + value);
  }
}
