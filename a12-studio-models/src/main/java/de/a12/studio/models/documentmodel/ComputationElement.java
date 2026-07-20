package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ComputationElement extends Element {

  @JsonProperty("Computation")
  private ComputationConfig computation;

  public ComputationElement() {
    setType(ElementType.COMPUTATION);
  }
}
