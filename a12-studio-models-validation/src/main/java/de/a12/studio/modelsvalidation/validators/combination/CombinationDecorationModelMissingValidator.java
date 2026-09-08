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
 * Ports SME's {@code DECORATION_MODEL_MISSING} rule ({@code DomainCombination.json}): a
 * {@code DecorationForFields} or {@code DecorationForGroups} step must reference a Model for Decoration.
 */
public final class CombinationDecorationModelMissingValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof CombinedDocumentModel combinedDocumentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    List<CombinationStep> steps = combinedDocumentModel.getContent().getCombinationSteps();
    for (int index = 0; index < steps.size(); index++) {
      CombinationStep step = steps.get(index);
      boolean needsDecoration = step.getType() == CombinationStepType.DECORATION_FOR_FIELDS
          || step.getType() == CombinationStepType.DECORATION_FOR_GROUPS;
      boolean missing = needsDecoration && (step.getDecorationModel() == null || isBlank(step.getDecorationModel().getDmId()));
      if (missing) {
        errors.add(new ModelValidationError(model, "content/combinationSteps/" + index,
            ValidationMessages.get("validation.combinationDecorationModelMissing"), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
