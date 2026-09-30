package de.a12.studio.modelsvalidation.validators.combination;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * Ports SME's cap of 99 Combination Steps on a Combined Document Model.
 */
public final class CombinationStepsMaxCountValidator implements ModelValidator {

  public static final int MAX_COMBINATION_STEPS = 99;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof CombinedDocumentModel combinedDocumentModel)) {
      return List.of();
    }
    List<CombinationStep> steps = combinedDocumentModel.getContent().getCombinationSteps();
    if (steps.size() <= MAX_COMBINATION_STEPS) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, "content/combinationSteps",
        ValidationMessages.get("validation.combinationStepsMaxCountExceeded", MAX_COMBINATION_STEPS),
        Severity.ERROR.name()));
  }
}
