package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintDimensions extends PrintNode {

  private Measure minHeight;
  private Measure minWidth;
}
