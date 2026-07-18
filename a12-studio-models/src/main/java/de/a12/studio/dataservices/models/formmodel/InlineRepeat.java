package de.a12.studio.dataservices.models.formmodel;

import lombok.Getter;
import lombok.Setter;

// Renders each row's Controls directly inline within the overview table.
@Getter
@Setter
public class InlineRepeat extends AbstractRepeat {

  public InlineRepeat() {
    setType(ScreenElementType.INLINE_REPEAT);
  }
}
