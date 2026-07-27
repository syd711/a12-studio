package de.a12.studio.models.printmodel;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class PrintElementReference extends PrintNode {

  // References a PrintElementDefinition id in content.elementDefinitions.
  private String refId;
  private PrintPosition position;
  private PrintDimensions dimensions;
  private ScreenReadingOrder screenReadingOrder;
  private List<Object> hideConditions = new ArrayList<>();
  private OverridableValue pageBreakBehavior;
}
