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
 * One {@code content.PatternErrors} entry ("String Pattern Error Messages"): applied to every generated String field
 * whose pattern is exactly {@link #pattern} (the regex string as the XSD has it). What happens is set by
 * {@link #action} (see {@link PatternErrorAction}; absent = update the message); {@link #errors} are the localized
 * error messages (at most {@value #MAX_ERRORS}, one per locale of the model), {@link #replacement} the new regex for
 * {@link PatternErrorAction#REPLACE}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonPropertyOrder({"pattern", "action", "replacement", "errors"})
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class PatternError {

  public static final int MAX_ERRORS = 19;

  private String pattern;
  private String action;
  private String replacement;
  private List<Label> errors;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }

  /** The action in effect: the stored one, or {@link PatternErrorAction#UPDATE_MESSAGE} when none is set. */
  @JsonIgnore
  public PatternErrorAction getEffectiveAction() {
    PatternErrorAction stored = PatternErrorAction.fromValue(action);
    return stored != null ? stored : PatternErrorAction.UPDATE_MESSAGE;
  }

  @JsonIgnore
  public List<Label> getErrorsOrEmpty() {
    return errors == null ? List.of() : errors;
  }

  @JsonIgnore
  public List<Label> getOrCreateErrors() {
    if (errors == null) {
      errors = new ArrayList<>();
    }
    return errors;
  }

  @JsonIgnore
  @Nullable
  public Label findError(String locale) {
    return getErrorsOrEmpty().stream().filter(label -> locale.equals(label.getLocale())).findFirst().orElse(null);
  }
}
