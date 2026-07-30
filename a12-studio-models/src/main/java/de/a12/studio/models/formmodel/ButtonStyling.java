package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ButtonStyling {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  // "PRIMARY" or "SECONDARY".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String priority;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean destructive;
}
