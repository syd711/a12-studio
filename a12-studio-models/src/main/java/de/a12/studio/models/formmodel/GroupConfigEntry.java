package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class GroupConfigEntry {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DependentConfig dependentGroup;
  private String groupRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer numberOfInitialRows;
}
