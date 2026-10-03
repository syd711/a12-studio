package de.a12.studio.models.transformermodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The content of a {@link TransformerModel}: SME's {@code TransformerConfigModel} meta model
 * ({@code resources/models/transformerModel/TransformerConfigModel.json}, version 29.4.0 of the installed SME 13.0.2)
 * and the {@code transformer} library's configuration model. The sections map onto SME's editor tabs:
 * <ul>
 *   <li><b>Transformation</b> - {@link #cmd} (main XSD, root element) and {@link #typeMapping}</li>
 *   <li><b>Element Selection</b> - {@link #renamePaths} and {@link #deletePaths}</li>
 *   <li><b>Custom Texts</b> - {@link #patternErrors} and {@link #enumLabels}</li>
 * </ul>
 * {@link #configuration} and {@link #codeLists} have no editor in SME (they are written by hand in the file), so they
 * are only read, validated and preserved here.
 *
 * <p>Every list is {@code null} while its key is absent from the file and only written when it was present or has
 * entries (some fixtures write {@code "TypeMapping": []}, others omit it), the same absent-vs-explicit-empty trick
 * {@link de.a12.studio.models.A12Model} uses for the header lists. Use the {@code ...OrEmpty()} accessors to read and
 * {@code getOrCreate...()} to edit. Unknown keys round-trip through {@link #extras}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"Cmd", "Configuration", "TypeMapping", "CodeLists", "RenamePaths", "DeletePaths", "PatternErrors", "EnumLabels"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TransformerModelContent {

  @JsonProperty("Cmd")
  private TransformerCmd cmd;

  @JsonProperty("Configuration")
  private TransformerConfiguration configuration;

  @JsonProperty("TypeMapping")
  private List<TypeMappingEntry> typeMapping;

  @JsonProperty("CodeLists")
  private CodeLists codeLists;

  @JsonProperty("RenamePaths")
  private List<RenamePath> renamePaths;

  @JsonProperty("DeletePaths")
  private List<DeletePath> deletePaths;

  @JsonProperty("PatternErrors")
  private List<PatternError> patternErrors;

  @JsonProperty("EnumLabels")
  private List<EnumLabel> enumLabels;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }

  @JsonIgnore
  public TransformerCmd getOrCreateCmd() {
    if (cmd == null) {
      cmd = new TransformerCmd();
    }
    return cmd;
  }

  @JsonIgnore
  public TransformerConfiguration getOrCreateConfiguration() {
    if (configuration == null) {
      configuration = new TransformerConfiguration();
    }
    return configuration;
  }

  @JsonIgnore
  public List<TypeMappingEntry> getTypeMappingOrEmpty() {
    return typeMapping == null ? List.of() : typeMapping;
  }

  @JsonIgnore
  public List<TypeMappingEntry> getOrCreateTypeMapping() {
    if (typeMapping == null) {
      typeMapping = new ArrayList<>();
    }
    return typeMapping;
  }

  @JsonIgnore
  public List<RenamePath> getRenamePathsOrEmpty() {
    return renamePaths == null ? List.of() : renamePaths;
  }

  @JsonIgnore
  public List<RenamePath> getOrCreateRenamePaths() {
    if (renamePaths == null) {
      renamePaths = new ArrayList<>();
    }
    return renamePaths;
  }

  @JsonIgnore
  public List<DeletePath> getDeletePathsOrEmpty() {
    return deletePaths == null ? List.of() : deletePaths;
  }

  @JsonIgnore
  public List<DeletePath> getOrCreateDeletePaths() {
    if (deletePaths == null) {
      deletePaths = new ArrayList<>();
    }
    return deletePaths;
  }

  @JsonIgnore
  public List<PatternError> getPatternErrorsOrEmpty() {
    return patternErrors == null ? List.of() : patternErrors;
  }

  @JsonIgnore
  public List<PatternError> getOrCreatePatternErrors() {
    if (patternErrors == null) {
      patternErrors = new ArrayList<>();
    }
    return patternErrors;
  }

  @JsonIgnore
  public List<EnumLabel> getEnumLabelsOrEmpty() {
    return enumLabels == null ? List.of() : enumLabels;
  }

  @JsonIgnore
  public List<EnumLabel> getOrCreateEnumLabels() {
    if (enumLabels == null) {
      enumLabels = new ArrayList<>();
    }
    return enumLabels;
  }
}
