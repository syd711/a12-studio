package de.a12.studio.models.formmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// A button bar addable as its own node directly in the screen tree (unlike the model-level/per-screen
// header+footer boxes, which only ever hold buttons, not other screen elements).
@Getter
@Setter
public class ButtonPanel extends ScreenElement {

  private List<Button> button = new ArrayList<>();

  public ButtonPanel() {
    setType(ScreenElementType.BUTTON_PANEL);
  }
}
