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
 * Ports SME's {@code AdmShouldNotCauseLoop} condition: the Additive Model of an {@code Addition} step must not
 * lead back to this Combined Document Model, nor contain a reference loop of its own. Same walk as
 * {@link CombinationBaseModelLoopValidator}, see {@link CombinationReferenceGraph}.
 */
public final class CombinationAdditiveModelLoopValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof CombinedDocumentModel combinedDocumentModel) || combinedDocumentModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    List<CombinationStep> steps = combinedDocumentModel.getContent().getCombinationSteps();
    for (int index = 0; index < steps.size(); index++) {
      CombinationStep step = steps.get(index);
      if (step.getType() != CombinationStepType.ADDITION || step.getAdditiveModel() == null) {
        continue;
      }
      String additiveModelId = step.getAdditiveModel().getDmId();
      if (additiveModelId == null || additiveModelId.isBlank()) {
        continue;
      }
      String elementId = "content/combinationSteps/" + index;
      CombinationReferenceGraph.findLoop(combinedDocumentModel, additiveModelId, context)
          .ifPresent(loop -> errors.add(new ModelValidationError(model, elementId,
              ValidationMessages.get("validation.combinationAdditiveModelLoop", additiveModelId, loop.describe()),
              Severity.ERROR.name())));
    }
    return errors;
  }
}
