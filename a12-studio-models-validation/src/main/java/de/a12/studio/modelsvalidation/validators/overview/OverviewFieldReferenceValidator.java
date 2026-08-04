package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
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
    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(overviewModel, context);
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
      Element element = OverviewElementResolution.resolve(index, elementRef);
      if (element == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.the_reference_is_invalid_the_referenced_field"" + elementRef
                + "\" does not exist in the document model.", Severity.ERROR.name()));
        continue;
      }
      if (OverviewElementResolution.isIndexedFalse(element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.the"indexed\" annotation of field \"" + element.getName()
                + "\" should not be false. Please resolve this problem in the corresponding Document Model.",
            Severity.ERROR.name()));
      }
      if (OverviewElementResolution.isInRepeatableGroup(index, element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.the_reference_is_invalid_the_referenced_field"" + element.getName() + "\" is repeatable.",
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
