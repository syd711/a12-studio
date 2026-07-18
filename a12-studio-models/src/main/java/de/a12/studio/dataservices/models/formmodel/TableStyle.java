package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class TableStyle {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer rowHeight;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer tableHeight;
}
