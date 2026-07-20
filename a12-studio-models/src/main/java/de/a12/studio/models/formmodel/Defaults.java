package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

// Model-wide overrides for built-in repeat button labels (keyed by action, e.g. "ADD", "CANCEL",
// "COMMIT_ADD", "APPLY") and confirmation dialog texts (keyed by action, e.g. "REMOVE").
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Defaults {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, TextContainer> buttonLabels = new LinkedHashMap<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, ConfirmationText> confirmationTexts = new LinkedHashMap<>();
}
