package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GroupElement extends Element {

  @JsonProperty("Group")
  private GroupConfig group;

  public GroupElement() {
    setType(ElementType.GROUP);
  }
}
