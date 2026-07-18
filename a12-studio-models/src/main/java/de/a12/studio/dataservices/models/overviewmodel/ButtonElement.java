package de.a12.studio.dataservices.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.dataservices.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ButtonElement extends BoxElement {

  private String event;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Confirmation confirmation;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> description = new ArrayList<>();
  private Boolean destructive;
  private Boolean primary;

  public ButtonElement() {
    setType(BoxElementType.BUTTON);
  }
}
