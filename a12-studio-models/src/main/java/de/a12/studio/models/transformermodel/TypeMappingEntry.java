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
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One {@code content.TypeMapping} entry: an XSD simple type ({@link #xsdType}) and the A12 data type it becomes
 * ({@link #a12Type}), optionally with configuration of that data type. The configuration object is named after the
 * data type it configures ({@code StringType}, {@code NumberType}, {@code EnumerationType}) and may only be filled
 * when the {@link A12DataType#getSuperType() super type} of {@link #a12Type} fits (a validator checks that; SME's
 * editor hides the fields that do not apply). Each group is {@code null} while it has no value, so an absent group
 * stays absent in the file.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"xsdType", "a12Type", "StringType", "NumberType", "EnumerationType"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TypeMappingEntry {

  private String xsdType;

  private String a12Type;

  @JsonProperty("StringType")
  private StringTypeConfig stringType;

  @JsonProperty("NumberType")
  private NumberTypeConfig numberType;

  @JsonProperty("EnumerationType")
  private EnumerationTypeConfig enumerationType;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }

  /** {@link #a12Type} as the enum, null when none is set or the value is not one SME offers. */
  @JsonIgnore
  @Nullable
  public A12DataType getDataType() {
    return A12DataType.fromValue(a12Type);
  }

  @JsonIgnore
  public StringTypeConfig getOrCreateStringType() {
    if (stringType == null) {
      stringType = new StringTypeConfig();
    }
    return stringType;
  }

  @JsonIgnore
  public NumberTypeConfig getOrCreateNumberType() {
    if (numberType == null) {
      numberType = new NumberTypeConfig();
    }
    return numberType;
  }

  @JsonIgnore
  public EnumerationTypeConfig getOrCreateEnumerationType() {
    if (enumerationType == null) {
      enumerationType = new EnumerationTypeConfig();
    }
    return enumerationType;
  }

  /** Drops every configuration group without a value, so an emptied group is not written as {@code {}}. */
  public void pruneEmptyConfigs() {
    if (stringType != null && stringType.isEmpty()) {
      stringType = null;
    }
    if (numberType != null && numberType.isEmpty()) {
      numberType = null;
    }
    if (enumerationType != null && enumerationType.isEmpty()) {
      enumerationType = null;
    }
  }
}
