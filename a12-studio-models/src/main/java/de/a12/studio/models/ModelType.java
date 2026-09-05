package de.a12.studio.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Map;

@Slf4j
public enum ModelType {

  // Filename suffix per the a12 platform's "Standardized Name" naming convention
  // (documentation/2606-06-doc/overall-model_naming_conventions.md), confirmed by real fixtures for
  // AM/CM/DM/FM/OM/PM/MDM/TDM. COMBINATION/MAPPING/QUERY/RELATIONSHIP/STRUCTURALMAPPING have no
  // documented or fixture-confirmed convention; CDM/MM/QM/RM/SMM here are a best-effort, collision-free
  // scheme consistent with the confirmed ones (SMM is also the literal abbreviation the a12 kernel uses
  // for Structural Mapping Model, see kernel-kernel-documentation-dev.md's "_SMM_..." annotation name).
  APPLICATION("application", "Application Model", "AM"),
  COMBINATION("combination", "Combination Model", "CDM"),
  CONTENT("content", "Content Model", "CM"),
  DOCUMENT("document", "Document Model", "DM"),
  FORM("form", "Form Model", "FM"),
  MAPPING("mapping", "Mapping Model", "MM"),
  MASTERDETAIL("module-masterdetail", "Master-Detail Model", "MDM"),
  OVERVIEW("overview", "Overview Model", "OM"),
  PRINT("print", "Print Model", "PM"),
  QUERY("query", "Query Model", "QM"),
  RELATIONSHIP("relationship", "Relationship Model", "RM"),
  STRUCTURALMAPPING("structuralmapping", "Structural Mapping Model", "SMM"),
  TREE("tree", "Tree Model", "TM"),
  TYPEDEFINITION("typedefinition", "Type Definition Model", "TDM");

  private static final String VERSIONS_RESOURCE = "model-versions.json";
  private static final Map<String, JsonNode> MODEL_CONFIG = loadModelConfig();

  private final String value;
  private final String displayName;
  private final String suffix;

  ModelType(String value, String displayName, String suffix) {
    this.value = value;
    this.displayName = displayName;
    this.suffix = suffix;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  public String getDisplayName() {
    return displayName;
  }

  /**
   * The filename suffix (without the leading underscore) conventionally used for this model type,
   * e.g. {@code "DM"} for {@link #DOCUMENT} so a model is named {@code "SomeName_DM"}. Used by the
   * "Enforce Model Suffixes" validation setting.
   */
  public String getSuffix() {
    return suffix;
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

  /**
   * Returns {@code null} (instead of throwing) for a {@code value} not among the ones declared here, e.g.
   * a real a12 platform model type (Relationship UI Model's {@code "relationship-ui"}, Additive Document
   * Model's {@code "additive-document"}, Selection Model's {@code "selection"}) that a12-studio has no
   * editor for yet. This is deliberately lenient: {@link de.a12.studio.models.ModelReference#modelType} is
   * routinely a reference to a model of a type a12-studio can't open, and callers already null-check it
   * (e.g. {@code HeaderModelReferenceValidator}) — throwing here would otherwise fail deserialization of the
   * whole containing model (a Form/Combined Document Model, say) just because one unrelated reference in its
   * header points at an unsupported type. See {@link ModelFactory#load} for the top-level-model-file case.
   */
  @JsonCreator
  public static ModelType fromValue(String value) {
    for (ModelType type : values()) {
      if (type.value.equals(value)) {
        return type;
      }
    }
    log.warn("Unknown model type: {}", value);
    return null;
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
