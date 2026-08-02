package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// One named group of context-menu actions. Actions here are like RowActionGroup's Row Action buttons but
// without Priority/Destructive/Hide Label, per the SME reference's Context Menu documentation.
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class ActionGroup {

  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> title = new ArrayList<>();
  private List<Button> actions = new ArrayList<>();
}
