package de.a12.studio.models.formmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NavigationButton extends Button {

  // Screen to navigate to; can be a Screen id, "#previous"/"#next" or similar special targets.
  private String target;

  public NavigationButton() {
    setType(ButtonType.NAVIGATION);
  }
}
