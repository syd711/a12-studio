package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Alignment {

  // Tree Model columns may set only the vertical part, so an absent key must stay absent.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String horizontal;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String vertical;
}
