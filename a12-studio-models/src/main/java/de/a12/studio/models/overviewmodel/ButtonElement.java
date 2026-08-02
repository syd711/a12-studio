package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.EventButtonLike;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ButtonElement extends BoxElement implements EventButtonLike {

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

  @Override
  @JsonIgnore
  public String getIconName() {
    return icon != null ? icon.getName() : null;
  }

  @Override
  @JsonIgnore
  public void setIconName(String name) {
    if (name == null || name.isEmpty()) {
      icon = null;
      return;
    }
    if (icon == null) {
      icon = new Icon();
    }
    icon.setName(name);
  }
}
