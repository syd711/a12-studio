package de.a12.studio.models.transformermodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code content.Configuration}: the version of the configuration, the time zone of the generated Document Model
 * and the set of supported characters - given either as a regex ({@link #supportedCharacters}) or as the name of an
 * XSD simple type whose pattern facet is used ({@link #supportedCharactersTypeInXsd}), never both.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class TransformerConfiguration {

  /** The time zone SME's editor assigns a Transformer Model that has none ({@code ensureTimeZoneAssigned}). */
  public static final String DEFAULT_TIME_ZONE = "UTC";

  private String version;
  private String timeZone;
  private String supportedCharacters;
  private String supportedCharactersTypeInXsd;

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
