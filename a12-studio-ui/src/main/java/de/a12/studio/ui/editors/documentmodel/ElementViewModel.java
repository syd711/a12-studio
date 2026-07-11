package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.GroupElement;
import de.a12.studio.ui.util.Icons;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ElementViewModel {

  private final Element element;

  public ElementViewModel(@NonNull Element element) {
    this.element = element;
  }

  public Element getElement() {
    return element;
  }

  public String getName() {
    return element.getName();
  }

  public String getType() {
    return element.getType();
  }

  public String getIcon() {
    if (element instanceof GroupElement) {
      return Icons.ELEMENT_GROUP;
    }
    return switch (element.getType()) {
      case "Field" -> Icons.ELEMENT_FIELD;
      case "Rule" -> Icons.ELEMENT_RULE;
      case "Computation" -> Icons.ELEMENT_COMPUTATION;
      default -> Icons.ELEMENT_GENERIC;
    };
  }

  public List<ElementViewModel> getChildren() {
    List<ElementViewModel> children = new ArrayList<>();
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      for (Element child : groupElement.getGroup().getElements()) {
        children.add(new ElementViewModel(child));
      }
    }
    return children;
  }

  @Override
  public String toString() {
    return getName();
  }
}
