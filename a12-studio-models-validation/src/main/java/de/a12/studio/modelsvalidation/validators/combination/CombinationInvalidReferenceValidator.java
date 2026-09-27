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
 * Ports SME's 4 "Invalid Reference" rules ({@code DomainCombination.json}: {@code
 * A12_BASE_MODEL_ID_INVALID_REFERENCE}, {@code A12_ADDITIVE_MODEL_ID_INVALID_REFERENCE}, {@code
 * A12_SELECTION_MODEL_ID_INVALID_REFERENCE}, {@code A12_DECORATION_MODEL_ID_INVALID_REFERENCE}): a
 * base/additive/selection/decoration model reference that is set but doesn't resolve to a real project
 * model. The generic {@link de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator} already
 * catches every dangling reference here too (the header's {@code modelReferences} are kept in sync with
 * these same ids - see {@code CombinedDocumentModelEditorController#syncModelReferences}), but always under
 * the flat {@code header/modelReferences} element id; this validator reports the same problem under {@code
 * content/baseModelId} / {@code content/combinationSteps/<i>} instead, so it surfaces on the offending field
 * itself - {@link de.a12.studio.ui.editors.combineddocumentmodel.CombinationStepsPanelController#refreshValidation()
 * } only picks up errors whose element id starts with {@code content/combinationSteps/}. The blank-reference
 * ("missing") case is a separate rule, already covered by {@link CombinationAdditiveModelMissingValidator}/
 * {@link CombinationSelectionModelMissingValidator}/{@link CombinationDecorationModelMissingValidator}.
 */
public final class CombinationInvalidReferenceValidator implements ModelValidator {

  public static final String BASE_MODEL_ELEMENT_ID = "content/baseModelId";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof CombinedDocumentModel combinedDocumentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    String baseModelId = combinedDocumentModel.getContent().getBaseModelId();
    if (!isBlank(baseModelId) && !context.hasOtherDocumentOrCombinedModel(baseModelId)) {
      errors.add(new ModelValidationError(model, BASE_MODEL_ELEMENT_ID,
          ValidationMessages.get("validation.combinationBaseModelInvalidReference", baseModelId), Severity.ERROR.name()));
    }

    List<CombinationStep> steps = combinedDocumentModel.getContent().getCombinationSteps();
    for (int index = 0; index < steps.size(); index++) {
      CombinationStep step = steps.get(index);
      String elementId = "content/combinationSteps/" + index;

      if (step.getType() == CombinationStepType.ADDITION && step.getAdditiveModel() != null) {
        String dmId = step.getAdditiveModel().getDmId();
        if (!isBlank(dmId) && !context.hasOtherDocumentOrCombinedModel(dmId)) {
          errors.add(new ModelValidationError(model, elementId,
              ValidationMessages.get("validation.combinationAdditiveModelInvalidReference", dmId), Severity.ERROR.name()));
        }
      }

      boolean needsSelection = step.getType() == CombinationStepType.SELECTION
          || step.getType() == CombinationStepType.DECORATION_FOR_FIELDS
          || step.getType() == CombinationStepType.DECORATION_FOR_GROUPS;
      if (needsSelection && step.getSelectionModel() != null) {
        String smId = step.getSelectionModel().getSmId();
        if (!isBlank(smId) && context.findOtherModel(smId) == null) {
          errors.add(new ModelValidationError(model, elementId,
              ValidationMessages.get("validation.combinationSelectionModelInvalidReference", smId), Severity.ERROR.name()));
        }
      }

      boolean needsDecoration = step.getType() == CombinationStepType.DECORATION_FOR_FIELDS
          || step.getType() == CombinationStepType.DECORATION_FOR_GROUPS;
      if (needsDecoration && step.getDecorationModel() != null) {
        String dmId = step.getDecorationModel().getDmId();
        if (!isBlank(dmId) && !context.hasOtherDocumentOrCombinedModel(dmId)) {
          errors.add(new ModelValidationError(model, elementId,
              ValidationMessages.get("validation.combinationDecorationModelInvalidReference", dmId), Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
