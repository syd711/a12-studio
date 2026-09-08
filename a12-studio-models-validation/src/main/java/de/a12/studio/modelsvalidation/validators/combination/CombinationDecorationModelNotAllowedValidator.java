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
 * Ports SME's {@code DECORATION_MODEL_NOT_ALLOWED} rule ({@code DomainCombination.json}): only a
 * {@code DecorationForFields} or {@code DecorationForGroups} step may reference a Model for Decoration.
 * Defense-in-depth - see {@link CombinationAdditiveModelNotAllowedValidator}.
 */
public final class CombinationDecorationModelNotAllowedValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof CombinedDocumentModel combinedDocumentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    List<CombinationStep> steps = combinedDocumentModel.getContent().getCombinationSteps();
    for (int index = 0; index < steps.size(); index++) {
      CombinationStep step = steps.get(index);
      boolean allowsDecoration = step.getType() == CombinationStepType.DECORATION_FOR_FIELDS
          || step.getType() == CombinationStepType.DECORATION_FOR_GROUPS;
      boolean notAllowed = step.getType() != null && !allowsDecoration
          && step.getDecorationModel() != null && !isBlank(step.getDecorationModel().getDmId());
      if (notAllowed) {
        errors.add(new ModelValidationError(model, "content/combinationSteps/" + index,
            ValidationMessages.get("validation.combinationDecorationModelNotAllowed"), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
