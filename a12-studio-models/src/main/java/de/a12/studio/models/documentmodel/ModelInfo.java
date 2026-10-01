package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ModelInfo {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean immutable;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String comment;
  // SME's ModelInfo.baseYear: marks the model as related to a base year ("version zone"), which is what allows
  // a date range's interpretationOfYear to be anything but "standard".
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer baseYear;

  // Generator metadata written by the A12 model tooling; not edited here but must survive a save.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String joinedModelsInfo;
}
