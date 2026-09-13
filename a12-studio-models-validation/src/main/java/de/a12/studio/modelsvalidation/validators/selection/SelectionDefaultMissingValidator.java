package de.a12.studio.modelsvalidation.validators.selection;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Ports SME's {@code DEFAULT_MISSING} rule ({@code DomainSelectionSpecification.json}, one copy per
 * section): each of Data/Computation/Validation must specify whether unmatched elements of the reference
 * Document Model are selected or unselected by default.
 */
public final class SelectionDefaultMissingValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof SelectionModel selectionModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (SelectionCategories.Entry category : SelectionCategories.ALL) {
      if (category.get(selectionModel).getDefaultValue() == null) {
        errors.add(new ModelValidationError(model, category.elementIdPrefix() + "/Default",
            ValidationMessages.get("validation.selectionDefaultMissing"), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
