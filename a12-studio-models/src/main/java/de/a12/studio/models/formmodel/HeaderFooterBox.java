package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class HeaderFooterBox {

  private String id;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ButtonGroup majorButtons;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ButtonGroup minorButtons;
}
