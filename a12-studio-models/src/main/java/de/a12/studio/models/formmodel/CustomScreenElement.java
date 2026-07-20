package de.a12.studio.models.formmodel;

import lombok.Getter;
import lombok.Setter;

// A placeholder for a custom UI component registered by the application embedding this form; the name
// identifies which custom component to render.
@Getter
@Setter
public class CustomScreenElement extends ScreenElement {

  public CustomScreenElement() {
    setType(ScreenElementType.CUSTOM_SCREEN_ELEMENT);
  }
}
