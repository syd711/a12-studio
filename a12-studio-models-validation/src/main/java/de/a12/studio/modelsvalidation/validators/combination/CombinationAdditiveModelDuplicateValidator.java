package de.a12.studio.modelsvalidation.validators.combination;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ports SME's {@code ADDITIVE_MODEL_DUPLICATE} rule ({@code DomainCombination.json}): the same Model for
 * Addition must not be used by more than one {@code Addition} step. Flags every step past the first that
 * reuses a given model, mirroring SME's {@code RepetitionNotUnique} (every repeated occurrence is an error,
 * not just the second one).
 */
public final class CombinationAdditiveModelDuplicateValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof CombinedDocumentModel combinedDocumentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    List<CombinationStep> steps = combinedDocumentModel.getContent().getCombinationSteps();
    for (int index = 0; index < steps.size(); index++) {
      CombinationStep step = steps.get(index);
      String dmId = step.getAdditiveModel() != null ? step.getAdditiveModel().getDmId() : null;
      if (dmId == null || dmId.isBlank()) {
        continue;
      }
      if (!seen.add(dmId)) {
        errors.add(new ModelValidationError(model, "content/combinationSteps/" + index,
            ValidationMessages.get("validation.combinationAdditiveModelDuplicate"), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
