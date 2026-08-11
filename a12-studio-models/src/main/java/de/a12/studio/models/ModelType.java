package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

public enum ModelType {

  APPLICATION("application", "Application Model"),
  COMBINATION("combination", "Combination Model"),
  CONTENT("content", "Content Model"),
  DOCUMENT("document", "Document Model"),
  FORM("form", "Form Model"),
  MAPPING("mapping", "Mapping Model"),
  MASTERDETAIL("module-masterdetail", "Master-Detail Model"),
  OVERVIEW("overview", "Overview Model"),
  PRINT("print", "Print Model"),
  QUERY("query", "Query Model"),
  RELATIONSHIP("relationship", "Relationship Model"),
  STRUCTURALMAPPING("structuralmapping", "Structural Mapping Model"),
  TREE("tree", "Tree Model"),
  TYPEDEFINITION("typedefinition", "Type Definition Model");

  private static final String VERSIONS_RESOURCE = "model-versions.json";
  private static final Map<String, JsonNode> MODEL_CONFIG = loadModelConfig();

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
    JsonNode config = MODEL_CONFIG.get(value);
    if (config == null || !config.has("version")) {
      throw new IllegalStateException("No version configured for model type \"" + value + "\" in " + VERSIONS_RESOURCE);
    }
    return config.get("version").asText();
  }

  /**
   * Returns {@code true} when this model type has a working editor and can be opened.
   * Types with {@code "enabled": false} in {@code model-versions.json} show a
   * "not supported yet" message instead of opening an editor.
   */
  public boolean isEnabled() {
    JsonNode config = MODEL_CONFIG.get(value);
    if (config == null || !config.has("enabled")) {
      return true; // default to enabled if the flag is absent
    }
    return config.get("enabled").asBoolean(true);
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

  private static Map<String, JsonNode> loadModelConfig() {
    try (InputStream in = ModelType.class.getResourceAsStream(VERSIONS_RESOURCE)) {
      if (in == null) {
        throw new IllegalStateException("Missing resource: " + VERSIONS_RESOURCE);
      }
      return JsonMapper.shared().readValue(in, new TypeReference<Map<String, JsonNode>>() {
      });
    }
    catch (IOException e) {
      throw new UncheckedIOException("Failed to load " + VERSIONS_RESOURCE, e);
    }
  }
}
