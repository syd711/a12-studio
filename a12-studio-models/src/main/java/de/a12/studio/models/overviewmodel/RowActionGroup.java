package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RowActionGroup {

  // Some files omit "actions" entirely (no rowActionGroup content yet) while others write it explicitly
  // as "[]"; actionsExplicit preserves that distinction across a load/save cycle instead of always
  // omitting (or always writing) an empty array.
  @JsonIgnore
  private List<Button> actions = new ArrayList<>();
  @JsonIgnore
  private boolean actionsExplicit;

  @JsonProperty("actions")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<Button> getActionsForJson() {
    return actionsExplicit || !actions.isEmpty() ? actions : null;
  }

  @JsonProperty("actions")
  private void setActionsForJson(List<Button> value) {
    this.actionsExplicit = value != null;
    this.actions = value != null ? value : new ArrayList<>();
  }
}
