package de.a12.studio.dataservices.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ModelType {

  DOCUMENT("document"),
  FORM("form"),
  OVERVIEW("overview"),
  RELATIONSHIP("relationship"),
  APPLICATION("application"),
  CONTENT("content");

  private final String value;

  ModelType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  @JsonCreator
  public static ModelType fromValue(String value) {
    for (ModelType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    throw new IllegalArgumentException("Unknown model type: " + value);
  }
}
