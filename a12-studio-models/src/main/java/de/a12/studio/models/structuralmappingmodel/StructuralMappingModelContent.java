package de.a12.studio.models.structuralmappingmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class StructuralMappingModelContent {

  @JsonProperty("GroupsToClearOnFirstFill")
  private List<GroupToClearOnFirstFill> groupsToClearOnFirstFill = new ArrayList<>();

  @JsonProperty("MappingBlocks")
  private List<MappingBlock> mappingBlocks = new ArrayList<>();
}
