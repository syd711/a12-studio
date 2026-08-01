package de.a12.studio.models.mappingmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class MappingSource {

  private String name;
  private String dmId;
  private Integer maxRepeat;

  // "SINGLE_RG" (default) or "MODEL_ROOT".
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String includeLevel;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean noSourceValidation;

  @JsonProperty("SortInfo")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private SortInfo sortInfo;
}
