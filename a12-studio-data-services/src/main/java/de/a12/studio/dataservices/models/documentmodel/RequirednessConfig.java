package de.a12.studio.dataservices.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RequirednessConfig {

  // The field is always required.
  public static final String MODE_REQUIRED = "absoluteOrRelativeToNextRepAncestor";

  // The field is only required if its parent group is filled (adds a GroupFilled(RuleGroup) condition).
  public static final String MODE_REQUIRED_IF_PARENT_FILLED = "relativeToParent";

  private String mode;
}
