package de.a12.studio.dataservices.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegionClearDirective extends Directive {

  // The layout to apply to the region after clearing it of all previously added views.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Layout layout;

  public RegionClearDirective() {
    setType(DirectiveType.REGION_CLEAR);
  }
}
