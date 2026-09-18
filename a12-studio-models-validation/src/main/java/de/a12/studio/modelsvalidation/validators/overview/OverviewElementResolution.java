package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;

/**
 * Shared field-reference resolution used by every Overview Model validator that checks an
 * {@code elementRef}/{@code fieldId} against the referenced Document Model: columns
 * ({@link OverviewFieldReferenceValidator}), the filter's custom field list, and filter sections.
 * Mirrors the SME rules that a referenced field must exist, must not be annotated {@code indexed}
 * = false, and must not live inside a repeatable group (its data isn't unique per document).
 */
public final class OverviewElementResolution {

  private OverviewElementResolution() {
  }

  /** The single Document Model referenced by this Overview Model's header, if any. */
  public static DocumentModel referencedDocumentModel(OverviewModel model, ValidationContext context) {
    if (model.getModelReferences() == null) {
      return null;
    }
    return model.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.DOCUMENT)
        .map(ModelReference::getReference)
        .map(context::findOtherDocumentModel)
        .filter(documentModel -> documentModel != null)
        .findFirst()
        .orElse(null);
  }

  /**
   * Resolves {@code elementRef} against the referenced Document Model, following {@link
   * ElementIndex#resolveElement} through Include groups - a column can just as validly reference a field
   * that lives inside an included model via the compound {@code "<includeGroupId>_<targetId>"} id shape, so
   * {@code index} must have been built with the project's other Document Models (see {@link
   * ElementIndex#ElementIndex(DocumentModel, java.util.List)}) for that to resolve.
   */
  public static Element resolve(ElementIndex index, String elementRef) {
    if (elementRef == null || elementRef.isBlank()) {
      return null;
    }
    return index.resolveElement(elementRef).orElse(null);
  }

  /**
   * The kernel injects the same fixed {@code __meta} group (id {@value #META_GROUP_ID_PREFIX}{@code 8f4b1})
   * into every Document Model at load time - {@code docRef}, {@code modelReference}, {@code modelVersion},
   * {@code creator}, {@code createdAt}, {@code modifier}, {@code modifiedAt} (plus a nested {@code
   * extensions} group) - with the exact same element ids in every model (confirmed identical across multiple
   * unrelated SME fixtures, e.g. {@code Person_DM.json}, {@code PersonWithTeamsAndContracts_COM.json}), so
   * these are legitimately referenceable fields even though a12-studio's own Document Model files never
   * author them explicitly and {@link ElementIndex} has no record of them. Recognized by id prefix rather
   * than a fixed id set so any of the group's known fields (present or future) resolve.
   */
  private static final String META_GROUP_ID_PREFIX = "abc6a6767a60488754aace2accb73824_";

  /** True when {@code elementRef} (an {@code elementId}/{@code fieldId}) refers to the kernel-injected {@code
   * __meta} group or one of its fields - see {@link #META_GROUP_ID_PREFIX}. Callers should skip existence/
   * indexed/repeatable checks for these instead of resolving them against {@link ElementIndex}, which never
   * contains them. */
  public static boolean isMetaFieldId(String elementRef) {
    return elementRef != null && elementRef.startsWith(META_GROUP_ID_PREFIX);
  }

  public static boolean isIndexedFalse(Element element) {
    if (element.getAnnotations() == null) {
      return false;
    }
    for (Annotation annotation : element.getAnnotations()) {
      if ("indexed".equals(annotation.getName()) && "false".equalsIgnoreCase(String.valueOf(annotation.getValue()))) {
        return true;
      }
    }
    return false;
  }

  /**
   * True when {@code elementRef} resolves to an element with a repeatable ancestor - delegates to {@link
   * ElementIndex#isInRepeatableGroup}, which (unlike a plain {@link ElementIndex#parentOf} walk against
   * {@code index}) correctly accounts for an Include's own repeatability when {@code elementRef} resolves
   * into an included model rather than {@code index}'s own tree.
   */
  public static boolean isInRepeatableGroup(ElementIndex index, String elementRef) {
    return index.isInRepeatableGroup(elementRef);
  }

  /**
   * True when {@code element} is a multi-select group itself, or a field living directly inside one -
   * columns may reference either shape depending on how the field was picked. Mirrors SME's
   * {@code DocumentModelApi.isMultiSelect}, used e.g. to disallow {@code sortable} on such columns
   * ({@link OverviewSortableMultiSelectValidator}).
   */
  public static boolean isMultiSelect(ElementIndex index, Element element) {
    if (isMultiSelectGroup(element)) {
      return true;
    }
    GroupElement parent = index.parentOf(element);
    return parent != null && isMultiSelectGroup(parent);
  }

  private static boolean isMultiSelectGroup(Element element) {
    return element instanceof GroupElement groupElement && groupElement.getGroup() != null
        && GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(groupElement.getGroup().getUsageType());
  }

  /**
   * True when {@code element} is an attachment group itself, or a field living directly inside one - mirrors
   * {@link #isMultiSelect}, used to detect a column's element-specific display options (e.g. {@code
   * attachmentDisplayMode}) in the Column dialog.
   */
  public static boolean isAttachment(ElementIndex index, Element element) {
    if (isAttachmentGroup(element)) {
      return true;
    }
    GroupElement parent = index.parentOf(element);
    return parent != null && isAttachmentGroup(parent);
  }

  private static boolean isAttachmentGroup(Element element) {
    return element instanceof GroupElement groupElement && groupElement.getGroup() != null
        && GroupConfig.USAGE_TYPE_ATTACHMENT.equals(groupElement.getGroup().getUsageType());
  }
}
