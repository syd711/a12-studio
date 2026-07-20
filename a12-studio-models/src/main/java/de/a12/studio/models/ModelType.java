package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

public enum ModelType {

  DOCUMENT("document", "Document Model"),
  FORM("form", "Form Model"),
  TYPEDEFINITION("typedefinition", "Type Definition Model"),
  OVERVIEW("overview", "Overview Model"),
  RELATIONSHIP("relationship", "Relationship Model"),
  APPLICATION("application", "Application Model"),
  CONTENT("content", "Content Model");

  private static final String VERSIONS_RESOURCE = "model-versions.json";
  private static final Map<String, String> CURRENT_VERSIONS = loadCurrentVersions();

  private final String value;
  private final String displayName;

  ModelType(String value, String displayName) {
    this.value = value;
    this.displayName = displayName;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public String getDisplayName() {
    return displayName;
  }

  public String getCurrentVersion() {
    String version = CURRENT_VERSIONS.get(value);
    if (version == null) {
      throw new IllegalStateException("No version configured for model type \"" + value + "\" in " + VERSIONS_RESOURCE);
    }
    return version;
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

  private static Map<String, String> loadCurrentVersions() {
    try (InputStream in = ModelType.class.getResourceAsStream(VERSIONS_RESOURCE)) {
      if (in == null) {
        throw new IllegalStateException("Missing resource: " + VERSIONS_RESOURCE);
      }
      return JsonMapper.shared().readValue(in, new TypeReference<Map<String, String>>() {
      });
    }
    catch (IOException e) {
      throw new UncheckedIOException("Failed to load " + VERSIONS_RESOURCE, e);
    }
  }
}
