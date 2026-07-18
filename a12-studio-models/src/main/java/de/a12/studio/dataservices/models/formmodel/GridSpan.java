package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Number of grid columns per responsive breakpoint; reused for both a Control's offset and its span.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class GridSpan {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer lg;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer md;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer sm;
}
