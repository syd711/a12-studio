package de.a12.studio.models.transformermodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One {@code content.EnumLabels} entry ("Enumeration Display Texts"): the display text of the enumeration value
 * {@link #value} in the generated Document Model, per locale ({@link #replacements}, at most
 * {@value #MAX_REPLACEMENTS}). It applies to every enumeration with that value unless restricted to one
 * enumeration type ({@link #typeDefinitionId}) or one field ({@link #enumFieldPath}) - never both.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"value", "replacements", "typeDefinitionId", "enumFieldPath"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class EnumLabel {

  public static final int MAX_REPLACEMENTS = 19;

  private String value;
  private List<Label> replacements;
  private String typeDefinitionId;
  private String enumFieldPath;

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
  public List<Label> getReplacementsOrEmpty() {
    return replacements == null ? List.of() : replacements;
  }

  @JsonIgnore
  public List<Label> getOrCreateReplacements() {
    if (replacements == null) {
      replacements = new ArrayList<>();
    }
    return replacements;
  }

  @JsonIgnore
  @Nullable
  public Label findReplacement(String locale) {
    return getReplacementsOrEmpty().stream().filter(label -> locale.equals(label.getLocale())).findFirst().orElse(null);
  }
}
