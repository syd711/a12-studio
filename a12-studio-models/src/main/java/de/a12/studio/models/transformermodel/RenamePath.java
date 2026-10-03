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
import java.util.Map;
import java.util.regex.Pattern;

/**
 * One {@code content.RenamePaths} entry: the element at {@link #originalPath} (a path in the generated Document
 * Model, e.g. {@code /EnrollmentCertificate/IdNr}) is renamed to {@link #newElementName}; the original name is kept as
 * an annotation of the renamed element. Needed e.g. when a name exceeds the Document Model's limits (group 60,
 * field 200 characters).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"OriginalPath", "NewElementName"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class RenamePath {

  /** SME's pattern for {@code NewElementName}. */
  public static final Pattern ELEMENT_NAME = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_\\-.]*$");

  @JsonProperty("OriginalPath")
  private String originalPath;

  @JsonProperty("NewElementName")
  private String newElementName;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }
}
