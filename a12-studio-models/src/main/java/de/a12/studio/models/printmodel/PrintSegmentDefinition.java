package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PrintSegmentDefinition extends PrintNode {

  private String title;
  private String type;
  private DefaultSegment defaultSegment;
  private List<PrintElementReference> elementReferences = new ArrayList<>();
  private List<Object> dataContexts = new ArrayList<>();
}
