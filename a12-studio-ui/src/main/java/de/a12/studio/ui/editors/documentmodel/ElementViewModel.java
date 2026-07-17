package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.models.documentmodel.FieldElement;
import de.a12.studio.dataservices.models.documentmodel.GroupConfig;
import de.a12.studio.dataservices.models.documentmodel.GroupElement;
import de.a12.studio.dataservices.models.documentmodel.RuleElement;
import de.a12.studio.ui.util.Icons;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ElementViewModel {

  private final Element element;

  private boolean hasError;

  public ElementViewModel(@NonNull Element element) {
    this.element = element;
  }

  public Element getElement() {
    return element;
  }

  public boolean hasError() {
    return hasError;
  }

  public void setHasError(boolean hasError) {
    this.hasError = hasError;
  }

  public String getName() {
    return element.getName();
  }

  public String getType() {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() != null) {
      return fieldElement.getField().getFieldType().getType();
    }
    if (element instanceof GroupElement groupElement) {
      if (hasUsageType(groupElement, GroupConfig.USAGE_TYPE_ATTACHMENT)) {
        return "Attachment";
      }
      if (hasUsageType(groupElement, GroupConfig.USAGE_TYPE_MULTI_SELECT)) {
        return "Multi-Select";
      }
    }
    return element.getType() == null ? null : element.getType().getValue();
  }

  public boolean isGroup() {
    return element instanceof GroupElement;
  }

  public boolean hasAnnotations() {
    return !element.getAnnotations().isEmpty();
  }

  public boolean isRequired() {
    return element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getRequirednessConfig() != null;
  }

  public String getIcon() {
    if (element instanceof GroupElement groupElement) {
      if (hasUsageType(groupElement, GroupConfig.USAGE_TYPE_ATTACHMENT)) {
        return Icons.ELEMENT_ATTACHMENT;
      }
      if (hasUsageType(groupElement, GroupConfig.USAGE_TYPE_MULTI_SELECT)) {
        return Icons.ELEMENT_MULTI_SELECT;
      }
      return Icons.ELEMENT_GROUP;
    }
    return Icons.ELEMENT_GENERIC;
  }

  /**
   * Whether this element is a group whose children are fixed by its usage type (attachment, multi-select),
   * so no elements may be added to or removed from it.
   */
  public boolean hasFixedChildren() {
    return element instanceof GroupElement groupElement
        && (hasUsageType(groupElement, GroupConfig.USAGE_TYPE_ATTACHMENT)
            || hasUsageType(groupElement, GroupConfig.USAGE_TYPE_MULTI_SELECT));
  }

  private static boolean hasUsageType(GroupElement groupElement, String usageType) {
    return groupElement.getGroup() != null && usageType.equals(groupElement.getGroup().getUsageType());
  }

  /**
   * PNG icon for this element's node in the document model tree, or {@code null} if it should fall back to
   * the Ikonli glyph returned by {@link #getIcon()} (groups are handled separately by the tree controller).
   */
  public String getIconPath() {
    if (element instanceof RuleElement ruleElement) {
      String severity = ruleElement.getRule() != null ? ruleElement.getRule().getSeverity() : null;
      return switch (severity == null ? "" : severity.toUpperCase()) {
        case "ERROR" -> Icons.PNG_DMM_VALIDATION_RULE_ERROR;
        case "INFO" -> Icons.PNG_DMM_VALIDATION_RULE_INFO;
        default -> Icons.PNG_DMM_VALIDATION_RULE_WARNING;
      };
    }
    return switch (element.getType()) {
      case FIELD -> Icons.PNG_DMM_FIELD;
      case COMPUTATION -> Icons.PNG_DMM_COMPUTATION_RULE;
      default -> null;
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
