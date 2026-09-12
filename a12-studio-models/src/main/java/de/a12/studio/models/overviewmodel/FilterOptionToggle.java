package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

/**
 * One entry of {@link FilterItemOptions#getRanges()} or {@link FilterItemOptions#getPeriods()} - a single
 * selectable Range (e.g. {@code fromTo}/{@code fromOnly}/{@code toOnly}/{@code exact}) or Period (e.g. {@code
 * date}/{@code time}/{@code dateTime}/{@code year}/{@code yearMonth}/{@code month}) option that the Custom Filter
 * Configuration editor lets a modeler enable/disable and mark as the default, e.g. {@code
 * testing/workspaces/advanced_new/models/10_People/Person_Ov.json}'s {@code dateTime} filter item: {@code
 * {"option": "fromTo", "default": true, "enabled": true}}. {@link #defaultOption} is omitted (not {@code false})
 * on every non-default entry in every fixture examined, hence {@code NON_NULL} rather than a primitive default.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterOptionToggle {

  private String option;
  private Boolean enabled;
  @JsonProperty("default")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean defaultOption;
}
