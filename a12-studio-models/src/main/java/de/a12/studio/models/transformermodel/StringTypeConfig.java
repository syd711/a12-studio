package de.a12.studio.models.transformermodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@code TypeMapping.StringType}: how the generated String field is configured. Ranges are SME's: the lengths are
 * {@code MIN_MAX_LENGTH_1} (integers 1..99999), the pattern is {@code REGEX_TYPE_1} (at most 1000 characters).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class StringTypeConfig {

  public static final int MIN_LENGTH = 1;
  public static final int MAX_LENGTH = 99999;
  public static final int MAX_PATTERN_LENGTH = 1000;

  private Boolean lineBreaksPermitted;
  private String pattern;
  private Integer minLength;
  private Integer maxLength;
  private Boolean alphabeticalSorting;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }

  /** SME's {@code GroupFilled}: whether any value is set (an unchecked checkbox is stored as absent, not false). */
  @JsonIgnore
  public boolean isEmpty() {
    return lineBreaksPermitted == null && pattern == null && minLength == null && maxLength == null
        && alphabeticalSorting == null && extras.isEmpty();
  }
}
