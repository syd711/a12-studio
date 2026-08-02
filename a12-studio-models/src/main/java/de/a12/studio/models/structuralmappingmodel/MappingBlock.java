package de.a12.studio.models.structuralmappingmodel;

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
public class MappingBlock {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;

  @JsonProperty("ResolutionStrategies")
  private List<ResolutionStrategy> resolutionStrategies = new ArrayList<>();

  @JsonProperty("FieldMappings")
  private List<FieldMapping> fieldMappings = new ArrayList<>();
}
