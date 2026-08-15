package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonPropertyOrder({"type", "id", "name", "buttonStyling", "target", "validation", "annotations", "scope"})
public class NavigationButton extends Button {

  // Screen to navigate to; can be a Screen id, "#previous"/"#next" or similar special targets.
  private String target;

  public NavigationButton() {
    setType(ButtonType.NAVIGATION);
  }
}
