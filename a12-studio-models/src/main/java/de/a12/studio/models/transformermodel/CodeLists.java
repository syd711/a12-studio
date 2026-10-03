package de.a12.studio.models.transformermodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * {@code content.CodeLists} ("Code Lists - Xoev"): how the genericode code list files next to the XSD are read. SME
 * has no editor for it (file only); it is read, validated and kept.
 * <ul>
 *   <li>{@link #codeIdentifiers} - column ids whose values become enumeration values</li>
 *   <li>{@link #valueIdentifiersDe}/{@link #valueIdentifiersEn} - column ids whose values become the German/English labels</li>
 *   <li>{@link #elementNamesInXsd} - name of the XSD element holding the code value (required once code lists are configured)</li>
 *   <li>{@link #uriVersionList} - default version per code list URI (required once code lists are configured)</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"CodeIdentifiers", "ValueIdentifiersDe", "ValueIdentifiersEn", "ElementNamesInXsd", "UriVersionList"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CodeLists {

  @JsonProperty("CodeIdentifiers")
  private List<CodeListValue> codeIdentifiers;

  @JsonProperty("ValueIdentifiersDe")
  private List<CodeListValue> valueIdentifiersDe;

  @JsonProperty("ValueIdentifiersEn")
  private List<CodeListValue> valueIdentifiersEn;

  @JsonProperty("ElementNamesInXsd")
  private List<CodeListValue> elementNamesInXsd;

  @JsonProperty("UriVersionList")
  private List<UriVersion> uriVersionList;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }

  /** One entry of a plain value list ({@code {"value": "..."}}). */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Getter
  @Setter
  public static class CodeListValue {

    private String value;
  }

  /** One {@code UriVersionList} entry: the version used for a code list URI that has none of its own. */
  @JsonIgnoreProperties(ignoreUnknown = true)
  @JsonPropertyOrder({"uri", "version"})
  @JsonInclude(JsonInclude.Include.NON_NULL)
  @Getter
  @Setter
  public static class UriVersion {

    private String uri;
    private String version;
  }
}
