package de.a12.studio.modelsvalidation.validators.combination;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinationStepType;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Ports SME's {@code ADDITIVE_MODEL_NOT_ALLOWED} rule ({@code DomainCombination.json}): only an
 * {@code Addition} step may reference a Model for Addition. Defense-in-depth for hand-edited JSON - the
 * Combination Step dialog (see {@code CombinationStepDialogController}) already clears this field whenever the
 * Type is switched away from {@code Addition}, so this can't be produced through the UI.
 */
public final class CombinationAdditiveModelNotAllowedValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof CombinedDocumentModel combinedDocumentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    List<CombinationStep> steps = combinedDocumentModel.getContent().getCombinationSteps();
    for (int index = 0; index < steps.size(); index++) {
      CombinationStep step = steps.get(index);
      boolean notAllowed = step.getType() != null && step.getType() != CombinationStepType.ADDITION
          && step.getAdditiveModel() != null && !isBlank(step.getAdditiveModel().getDmId());
      if (notAllowed) {
        errors.add(new ModelValidationError(model, "content/combinationSteps/" + index,
            ValidationMessages.get("validation.combinationAdditiveModelNotAllowed"), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
