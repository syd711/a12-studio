package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A reference column whose field is multi-select must not be sortable - multi-select values have no
 * well-defined sort order. Mirrors SME's "notAllowSortableForMultiSelectColumn" rule.
 */
public final class OverviewSortableMultiSelectValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/columns/sortable";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    DocumentModel documentModel = OverviewElementResolution.referencedDocumentModel(overviewModel, context);
    if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel, context.otherDocumentModels());
    List<ModelValidationError> errors = new ArrayList<>();
    for (Column column : overviewModel.getContent().getColumns()) {
      if (!Boolean.TRUE.equals(column.getSortable())) {
        continue;
      }
      Element element = OverviewElementResolution.resolve(index, column.getElementRef());
      if (element != null && OverviewElementResolution.isMultiSelect(index, element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewSortableMultiSelect.notAllowed", element.getName()), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
