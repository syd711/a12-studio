package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FieldRef extends PrintNode {

  // Name of the referenced document model and the slash-separated path of the field within it.
  private String model;
  private String path;
}
