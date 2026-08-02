package de.a12.studio.models.structuralmappingmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ResolutionStrategy {

  private ResolutionStrategyType type;
  private String sourceGroupFullName;
  private String targetGroupFullName;

  // Only present when type is SLICE.
  @JsonProperty("Slice")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Slice slice;
}
