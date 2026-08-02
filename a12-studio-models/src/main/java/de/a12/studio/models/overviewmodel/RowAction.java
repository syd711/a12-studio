package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// What happens when a user clicks a row ("Row Activation"). Absent from OverviewModelContent entirely means
// the Overview Engine's default behavior applies. When present, custom is always true and either event is set
// (Type "Event": the row triggers that event) or omitted (Type "Non Interactive": the row is explicitly not
// clickable).
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RowAction {

  private Boolean custom;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String event;
}
