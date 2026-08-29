package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.ui.util.Icons;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ElementViewModel {

  private final Element element;

  // Every other Document Model in the project, needed to resolve an Include group's children (see
  // #includedChildren) from the Document Model it references. Empty for call sites that only need
  // self-contained checks like #hasFixedChildren, which never look past this element itself.
  private final List<DocumentModel> otherDocumentModels;

  private List<String> errorMessages = List.of();

  public ElementViewModel(@NonNull Element element) {
    this(element, List.of());
  }

  public ElementViewModel(@NonNull Element element, @NonNull List<DocumentModel> otherDocumentModels) {
    this.element = element;
    this.otherDocumentModels = otherDocumentModels;
  }

  public Element getElement() {
    return element;
  }

  public boolean hasError() {
    return !errorMessages.isEmpty();
  }

  /** The messages of every validation error reported against this element's id, for display in a row tooltip. */
  public List<String> getErrorMessages() {
    return errorMessages;
  }

  public void setErrorMessages(@NonNull List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }

  public String getName() {
    String name = element.getName();
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null && showsRepetitions(groupElement)) {
      Integer repeatability = groupElement.getGroup().getRepeatability();
      if (repeatability != null) {
        return name + " [" + repeatability + "]";
      }
    }
    return name;
  }

  public String getType() {
    if (element instanceof FieldElement fieldElement
        && fieldElement.getField() != null
        && fieldElement.getField().getFieldType() != null) {
      return fieldElement.getField().getFieldType().getType();
    }
    if (element instanceof GroupElement groupElement) {
      if (isInclude(groupElement)) {
        String reference = groupElement.getGroup().getIncludeConfig().getReference();
        return reference == null || reference.isBlank() ? "Include" : "Include [" + reference + "]";
      }
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
      if (isInclude(groupElement)) {
        return Icons.ELEMENT_INCLUDE;
      }
      if (hasUsageType(groupElement, GroupConfig.USAGE_TYPE_ATTACHMENT)) {
        return Icons.ELEMENT_ATTACHMENT;
      }
      if (hasUsageType(groupElement, GroupConfig.USAGE_TYPE_MULTI_SELECT)) {
        return Icons.ELEMENT_MULTI_SELECT;
      }
      return Icons.ELEMENT_GROUP;
    }
    if (element instanceof FieldElement) {
      return Icons.ELEMENT_FIELD;
    }
    if (element instanceof ComputationElement) {
      return Icons.ELEMENT_COMPUTATION;
    }
    if (element instanceof RuleElement) {
      return Icons.ELEMENT_VALIDATION_RULE;
    }
    return Icons.ELEMENT_GENERIC;
  }

  /**
   * Whether this element is a group whose children are fixed (attachment, multi-select) or externally
   * determined by a referenced document model (include), so no elements may be added to or removed from it.
   */
  public boolean hasFixedChildren() {
    return element instanceof GroupElement groupElement
        && (isInclude(groupElement)
            || hasUsageType(groupElement, GroupConfig.USAGE_TYPE_ATTACHMENT)
            || hasUsageType(groupElement, GroupConfig.USAGE_TYPE_MULTI_SELECT));
  }

  private static boolean hasUsageType(GroupElement groupElement, String usageType) {
    return groupElement.getGroup() != null && usageType.equals(groupElement.getGroup().getUsageType());
  }

  /**
   * Plain Groups and Includes show their repetition count in the tree (e.g. "Group [5]"); attachment and
   * multi-select groups have a fixed/derived repeatability (see {@link #hasFixedChildren}) that isn't
   * meaningful to surface the same way.
   */
  private static boolean showsRepetitions(GroupElement groupElement) {
    return !hasUsageType(groupElement, GroupConfig.USAGE_TYPE_ATTACHMENT)
        && !hasUsageType(groupElement, GroupConfig.USAGE_TYPE_MULTI_SELECT);
  }

  /**
   * A group is an Include (a reference to another Document Model) if its {@link GroupConfig} carries an
   * {@code includeConfig}, distinct from the "attachment"/"multi-select" {@code usageType} groups and from
   * plain groups, which have neither. Mirrors {@code DocumentModelEditorController#groupEditorFxml}.
   */
  private static boolean isInclude(GroupElement groupElement) {
    return groupElement.getGroup() != null && groupElement.getGroup().getIncludeConfig() != null;
  }

  /**
   * An Include group's own children are shown as this element's children too: the referenced Document
   * Model's root group(s) stand in for this node rather than nesting as an extra tree level, mirroring
   * how {@code elementRef}s into an Include (e.g. {@code "<includeGroupId>_<targetId>"}, see {@link
   * de.a12.studio.modelsvalidation.validators.ElementIndex#resolve}) skip straight past the included
   * model's root group to its children. Those elements belong to a different model file and are shown
   * read-only (see {@code ElementEditorController#isWithinInclude}, {@code
   * DocumentModelElementsTreeController#hasFixedChildrenAncestor}).
   */
  public List<ElementViewModel> getChildren() {
    List<ElementViewModel> children = new ArrayList<>();
    if (element instanceof GroupElement groupElement && groupElement.getGroup() != null) {
      List<? extends Element> elements = isInclude(groupElement) ? includedChildren(groupElement) : groupElement.getGroup().getElements();
      for (Element child : elements) {
        children.add(new ElementViewModel(child, otherDocumentModels));
      }
    }
    return children;
  }

  private List<Element> includedChildren(@NonNull GroupElement includeGroup) {
    DocumentModel included = resolveDocumentModel(includeGroup.getGroup().getIncludeConfig().getReference());
    if (included == null || included.getContent() == null || included.getContent().getModelRoot() == null
        || included.getContent().getModelRoot().getRootGroups() == null) {
      return List.of();
    }
    List<Element> children = new ArrayList<>();
    for (GroupElement rootGroup : included.getContent().getModelRoot().getRootGroups()) {
      if (rootGroup.getGroup() != null && rootGroup.getGroup().getElements() != null) {
        children.addAll(rootGroup.getGroup().getElements());
      }
    }
    return children;
  }

  private DocumentModel resolveDocumentModel(String reference) {
    if (reference == null) {
      return null;
    }
    return otherDocumentModels.stream().filter(model -> reference.equals(model.getId())).findFirst().orElse(null);
  }

  @Override
  public String toString() {
    return getName();
  }
}
