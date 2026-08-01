package de.a12.studio.models.combineddocumentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class CombinationStep {

  private CombinationStepType type;

  @JsonProperty("AdditiveModel")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DocumentModelIdRef additiveModel;

  @JsonProperty("SelectionModel")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private SelectionModelIdRef selectionModel;

  @JsonProperty("DecorationModel")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DocumentModelIdRef decorationModel;
}
