package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PrintSegments extends PrintNode {

  private List<PrintSegmentDefinition> definitions = new ArrayList<>();
  private List<Object> references = new ArrayList<>();
}
