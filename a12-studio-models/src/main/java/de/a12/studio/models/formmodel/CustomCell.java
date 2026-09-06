package de.a12.studio.models.formmodel;

import lombok.Getter;
import lombok.Setter;

// A placeholder for a custom UI component registered by the application, addable inside a ControlGrid row
// (unlike CustomScreenElement, which is a whole custom screen element, not a grid cell).
@Getter
@Setter
public class CustomCell extends Cell {

  public CustomCell() {
    setType(CellType.CUSTOM_CELL);
  }
}
