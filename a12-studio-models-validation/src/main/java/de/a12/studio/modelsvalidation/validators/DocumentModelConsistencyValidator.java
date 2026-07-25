package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.DocumentModelConsistencyRules;
import de.a12.studio.modelsvalidation.ElementIndex;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.List;

/** Thin {@link ModelValidator} adapter over the ported kernel rule set in {@link DocumentModelConsistencyRules}. */
public final class DocumentModelConsistencyValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    return DocumentModelConsistencyRules.checkAll(documentModel, new ElementIndex(documentModel)).stream()
        .map(problem -> new ModelValidationError(model, problem.elementId(), problem.message(), problem.severity().name()))
        .toList();
  }
}
