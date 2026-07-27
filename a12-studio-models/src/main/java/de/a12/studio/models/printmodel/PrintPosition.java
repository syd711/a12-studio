package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintPosition extends PrintNode {

  private Measure x;
  private Measure y;
}
