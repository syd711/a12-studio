package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ClearConfirmation {

  private Boolean enabled;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Confirmation confirmation;
}
