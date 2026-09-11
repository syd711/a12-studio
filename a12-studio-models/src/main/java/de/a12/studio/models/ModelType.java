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
  // AM/CM/DM/FM/OM/PM/MDM/TDM/Ru (Ru: all 12 "*_Ru.json" files under
  // testing/workspaces/advanced_new/models). COMBINATION/MAPPING/QUERY/RELATIONSHIP/STRUCTURALMAPPING have
  // no documented or fixture-confirmed convention; CDM/MM/QM/RM/SMM in model-versions.json are a
  // best-effort, collision-free scheme consistent with the confirmed ones (SMM is also the literal
  // abbreviation the a12 kernel uses for Structural Mapping Model, see
  // kernel-kernel-documentation-dev.md's "_SMM_..." annotation name).
  // QM specifically: checked 2026-09-05 against real SME fixtures and found inconsistent in the wild -
  // client/resources/input/models/example/.../HighExperienceInterns_QeM.json uses "_QeM", but
  // integrationTest/cypress/testData/models/omm/refactoring/OverviewModelRefactoring_QM.json (modelType
  // "query", so a genuine Query Model despite its own file's "OverviewModelRefactoring" name) uses "_QM".
  // Left as "QM" (not changed to "QeM") since the evidence contradicts itself rather than confirming one
  // convention over the other. SELECTION's "SeM" is the suffix documented in this repo's CLAUDE.md Model
  // Types table, not a best-effort guess.
  APPLICATION("application"),
  COMBINATION("combination"),
  CONTENT("content"),
  DOCUMENT("document"),
  FORM("form"),
  MAPPING("mapping"),
  MASTERDETAIL("module-masterdetail"),
  OVERVIEW("overview"),
  PRINT("print"),
  QUERY("query"),
  RELATIONSHIP("relationship"),
  RELATIONSHIPUI("relationship-ui"),
  SELECTION("selection"),
  STRUCTURALMAPPING("structuralmapping"),
  TREE("tree"),
  TYPEDEFINITION("typedefinition");

  private static final String VERSIONS_RESOURCE = "model-versions.json";
  private static final Map<String, JsonNode> MODEL_CONFIG = loadModelConfig();

  private final String value;

  ModelType(String value) {
    this.value = value;
  }

  @JsonValue
  public String getValue() {
    return value;
  }

  /**
   * The filename suffix (without the leading underscore) conventionally used for this model type,
   * e.g. {@code "DM"} for {@link #DOCUMENT} so a model is named {@code "SomeName_DM"}. Used by the
   * "Enforce Model Suffixes" validation setting and to look up a localized display name (UI code should
   * look this up via {@code "model_type_name." + modelType.getSuffix()} in the resource bundle rather
   * than calling a display-name getter here, since this module has no UI/localization dependency).
   * Returns {@code null} if no suffix is configured for this type in {@code model-versions.json},
   * mirroring {@link #fromValue}'s leniency so callers that already null-check a model type's suffix
   * (e.g. {@code ModelSuffixValidator}) keep working for a type added without one.
   */
  public String getSuffix() {
    JsonNode config = MODEL_CONFIG.get(value);
    if (config == null || !config.has("suffix")) {
      return null;
    }
    return config.get("suffix").asText();
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
   * a real a12 platform model type (Additive Document Model's {@code "additive-document"}, Composed
   * Document Model's {@code "composed-document"}) that a12-studio has no editor for yet. This is
   * deliberately lenient: {@link de.a12.studio.models.ModelReference#modelType} is
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
