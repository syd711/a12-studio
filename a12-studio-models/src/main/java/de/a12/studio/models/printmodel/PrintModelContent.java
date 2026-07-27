package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PrintModelContent extends PrintNode {

  private PrintGeneral general;
  private PrintSegments segments;
  private List<PrintElementDefinition> elementDefinitions = new ArrayList<>();
}
