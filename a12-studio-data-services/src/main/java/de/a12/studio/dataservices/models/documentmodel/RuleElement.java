package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RuleElement extends Element {

  @JsonProperty("Rule")
  private RuleConfig rule;
}
