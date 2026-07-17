package de.a12.studio.dataservices.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum ModelType {

  DOCUMENT("document", "Document Model", "28.4.0"),
  FORM("form", "Form Model", "37.3.0"),
  OVERVIEW("overview", "Overview Model", "38.2.0"),
  RELATIONSHIP("relationship", "Relationship Model", "3.0.0"),
  APPLICATION("application", "Application Model", "6.0.0"),
  CONTENT("content", "Content Model", "0.8.0");

  private final String value;
  private final String displayName;
  private final String currentVersion;

  ModelType(String value, String displayName, String currentVersion) {
    this.value = value;
    this.displayName = displayName;
    this.currentVersion = currentVersion;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getCurrentVersion() {
    return currentVersion;
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
