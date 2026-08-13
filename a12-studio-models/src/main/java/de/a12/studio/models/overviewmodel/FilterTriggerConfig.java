package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code newFilterConfiguration.filterSelector.trigger}: the Filter Button's "User Access" toggle ({@code
 * enabled}) paired with its actual configuration ({@code value}, see {@link FilterTriggerValue}). See {@code
 * testing/basic/models/Company_OM.json}'s {@code "trigger": {"enabled": true, "value": {...}}}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterTriggerConfig {

  private Boolean enabled;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private FilterTriggerValue value;
}
