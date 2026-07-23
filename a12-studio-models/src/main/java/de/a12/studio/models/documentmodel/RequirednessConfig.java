package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RequirednessConfig {

  // The field is always required.
  public static final String MODE_REQUIRED = "absoluteOrRelativeToNextRepAncestor";

  // The field is only required if its parent group is filled (adds a GroupFilled(RuleGroup) condition).
  public static final String MODE_REQUIRED_IF_PARENT_FILLED = "relativeToParent";

  private String mode;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> errorMessage = new ArrayList<>();
}
