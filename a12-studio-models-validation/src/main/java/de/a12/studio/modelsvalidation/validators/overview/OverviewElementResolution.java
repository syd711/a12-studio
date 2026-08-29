package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
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
}
