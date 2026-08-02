package de.a12.studio.models.structuralmappingmodel;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ResolutionStrategyType {

  FOLD("Fold"),
  SLICE("Slice");

  private final String value;

  ResolutionStrategyType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ResolutionStrategyType fromValue(String value) {
    for (ResolutionStrategyType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown resolution strategy type: " + value);
  }
}
