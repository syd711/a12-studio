package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PrintTextElement extends PrintElementDefinition {

  private RichText text;
  private BorderProperties borderProperties;
  private TextProperties textProperties;
}
