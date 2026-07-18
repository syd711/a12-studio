package de.a12.studio.dataservices.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class RowActionGroup {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Button> actions = new ArrayList<>();
}
