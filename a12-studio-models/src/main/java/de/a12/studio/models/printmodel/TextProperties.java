package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextProperties extends PrintNode {

  private OverridableValue textStyleId;
  private OverridableValue alignment;
}
