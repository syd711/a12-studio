package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class IncludeConfig {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String reference;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean excludeRules;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean excludeComputations;
  // "SINGLE_RG" | "MODEL_ROOT" (kernel A12K-4102). Round-trip only: neither SME's Include form nor a12-studio
  // edits it, MODEL_ROOT is SME's internal marker for an Additive Document Model's base model.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String includeLevel;
}
