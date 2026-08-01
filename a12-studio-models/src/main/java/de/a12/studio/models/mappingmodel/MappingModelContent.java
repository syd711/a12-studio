package de.a12.studio.models.mappingmodel;

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
public class MappingModelContent {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String mappingType;

  @JsonProperty("Source")
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<MappingSource> source = new ArrayList<>();

  @JsonProperty("Target")
  private MappingTarget target;

  @JsonProperty("PreComputationFragment")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private PreComputationFragmentRef preComputationFragment;

  @JsonProperty("StructuralMappingModel")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private StructuralMappingModelRef structuralMappingModel;

  @JsonProperty("OverallModel")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private OverallModelRef overallModel;
}
