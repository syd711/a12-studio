package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Column element references must resolve against the referenced Document Model, must not point to
 * fields annotated "indexed" = false, and must not point to fields inside repeatable groups (their
 * data is not unique per document). Mirrors the corresponding SME overview meta model rules.
 */
public final class OverviewFieldReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/columns/elementRef";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    DocumentModel documentModel = referencedDocumentModel(overviewModel, context);
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel);
    List<ModelValidationError> errors = new ArrayList<>();
    for (Column column : overviewModel.getContent().getColumns()) {
      String elementRef = column.getElementRef();
      if (elementRef == null || elementRef.isBlank()) {
        continue;
      }
      Element element = index.allElements().stream()
          .filter(candidate -> elementRef.equals(candidate.getId()))
          .findFirst()
          .orElse(null);
      if (element == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "The reference is invalid. The referenced field \"" + elementRef
                + "\" does not exist in the document model.", Severity.ERROR.name()));
        continue;
      }
      if (isIndexedFalse(element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "The \"indexed\" annotation of field \"" + element.getName()
                + "\" should not be false. Please resolve this problem in the corresponding Document Model.",
            Severity.ERROR.name()));
      }
      if (isInRepeatableGroup(index, element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "The reference is invalid. The referenced field \"" + element.getName() + "\" is repeatable.",
            Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static DocumentModel referencedDocumentModel(OverviewModel model, ValidationContext context) {
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

  private static boolean isIndexedFalse(Element element) {
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

  /** True when any ancestor group (not the root group itself) has a repeatability above 1. */
  private static boolean isInRepeatableGroup(ElementIndex index, Element element) {
    GroupElement parent = index.parentOf(element);
    while (parent != null) {
      if (parent.getGroup() != null && parent.getGroup().getRepeatability() != null
          && parent.getGroup().getRepeatability() > 1 && index.parentOf(parent) != null) {
        return true;
      }
      parent = index.parentOf(parent);
    }
    return false;
  }
}
