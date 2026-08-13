package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

/**
 * A boolean setting paired with whether end users may change it themselves, e.g. {@code
 * filterSelector.showSetFiltersOnly}/{@code filterSelector.searchBar}/{@code newFilterConfiguration.invert} in
 * {@code testing/basic/models/Company_OM.json}: {@code {"enabled": true, "value": false}}. {@code enabled} is the
 * "User Access" toggle shown in the Custom Filter Configuration editor, {@code value} is the initial/default
 * value of the setting itself.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class BooleanUserAccessOption {

  private Boolean enabled;
  private Boolean value;
}
