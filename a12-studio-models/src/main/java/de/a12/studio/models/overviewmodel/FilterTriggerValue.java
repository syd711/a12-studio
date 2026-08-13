package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code newFilterConfiguration.filterSelector.trigger.value}: the Filter Button's own configuration - the icon
 * and multilingual label shown on the button that opens the Filter Selector, and whether the label is hidden
 * (icon-only button). See {@code testing/basic/models/Company_OM.json}'s {@code trigger.value.label}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterTriggerValue {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean hideLabel;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
}
