package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Constraints {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String type;
  // Preferred display width (1-11) of the added view when constraints type is "MasterDetail"; if unset,
  // the available space is split evenly, equivalent to a preferred width of 6.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer preferredWidth;
}
