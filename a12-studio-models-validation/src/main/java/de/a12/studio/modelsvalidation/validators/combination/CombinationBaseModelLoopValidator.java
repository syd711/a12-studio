package de.a12.studio.modelsvalidation.validators.combination;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * Ports SME's base-model loop check ({@code CombModelReferenceHelper.modelCausesOrHasLoop}, used by the
 * {@code BaseModelMustBeValid} condition and to filter the Base Model picker): the base model must not lead
 * back to this Combined Document Model, nor contain a reference loop of its own. See
 * {@link CombinationReferenceGraph} for what counts as a reference.
 */
public final class CombinationBaseModelLoopValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof CombinedDocumentModel combinedDocumentModel) || combinedDocumentModel.getContent() == null) {
      return List.of();
    }
    String baseModelId = combinedDocumentModel.getContent().getBaseModelId();
    if (baseModelId == null || baseModelId.isBlank()) {
      return List.of();
    }
    return CombinationReferenceGraph.findLoop(combinedDocumentModel, baseModelId, context)
        .map(loop -> List.of(new ModelValidationError(model, "content/baseModelId",
            ValidationMessages.get("validation.combinationBaseModelLoop", baseModelId, loop.describe()),
            Severity.ERROR.name())))
        .orElse(List.of());
  }
}
