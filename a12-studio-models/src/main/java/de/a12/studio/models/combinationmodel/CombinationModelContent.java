package de.a12.studio.models.combinationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CombinationModelContent {

  private String baseModelId;

  @JsonProperty("CombinationSteps")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<CombinationStep> combinationSteps = new ArrayList<>();
}
