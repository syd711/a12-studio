package de.a12.studio.modelsvalidation.validators.composeddocument;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.composeddocumentmodel.CdmRelationshipStep;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModelResolver;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Each {@link CdmRelationshipStep} of a {@link ComposedDocumentModel}'s relationship chain must reference a
 * real {@link RelationshipModel}, its {@code sourceRole}/{@code targetRole} must be roles that relationship
 * actually declares, and {@code targetDocumentModel} must be the Document Model the relationship's
 * {@code targetRole} actually points at - see the BA docs' "Additionally, the Relationship elements require
 * some Annotations that are managed automatically... These Annotations must not be changed manually."
 */
public final class CdmRelationshipStepValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ComposedDocumentModel composedDocumentModel)) {
      return List.of();
    }
    List<CdmRelationshipStep> steps = ComposedDocumentModelResolver.getRelationshipSteps(composedDocumentModel);
    List<ModelValidationError> errors = new ArrayList<>();
    for (int index = 0; index < steps.size(); index++) {
      validateStep(model, context, steps.get(index), index, errors);
    }
    return errors;
  }

  private void validateStep(A12Model<?> model, ValidationContext context, CdmRelationshipStep step, int index,
      List<ModelValidationError> errors) {
    String elementId = "header/cdm.relationship" + (index == 0 ? "" : "." + index);

    if (!(context.findOtherModel(step.getRelationshipName()) instanceof RelationshipModel relationshipModel)) {
      errors.add(new ModelValidationError(model, elementId,
          ValidationMessages.get("validation.cdmRelationshipStep.relationshipNotFound", step.getRelationshipName()), Severity.ERROR.name()));
      return;
    }

    Optional<EntityCharacteristic> sourceCharacteristic = findCharacteristic(relationshipModel, step.getSourceRole());
    if (sourceCharacteristic.isEmpty()) {
      errors.add(new ModelValidationError(model, elementId,
          ValidationMessages.get("validation.cdmRelationshipStep.sourceRoleNotFound", step.getSourceRole(), step.getRelationshipName()),
          Severity.ERROR.name()));
    }

    Optional<EntityCharacteristic> targetCharacteristic = findCharacteristic(relationshipModel, step.getTargetRole());
    if (targetCharacteristic.isEmpty()) {
      errors.add(new ModelValidationError(model, elementId,
          ValidationMessages.get("validation.cdmRelationshipStep.targetRoleNotFound", step.getTargetRole(), step.getRelationshipName()),
          Severity.ERROR.name()));
      return;
    }

    String actualTargetDocumentModel = targetCharacteristic.get().getDocumentModel();
    if (step.getTargetDocumentModel() != null && !step.getTargetDocumentModel().equals(actualTargetDocumentModel)) {
      errors.add(new ModelValidationError(model, elementId,
          ValidationMessages.get("validation.cdmRelationshipStep.targetDocumentModelMismatch",
              step.getTargetDocumentModel(), actualTargetDocumentModel), Severity.ERROR.name()));
    }
  }

  private Optional<EntityCharacteristic> findCharacteristic(RelationshipModel relationshipModel, String role) {
    if (role == null || role.isBlank()) {
      return Optional.empty();
    }
    return relationshipModel.getContent().getEntityCharacteristics().stream()
        .filter(characteristic -> role.equals(characteristic.getRole()))
        .findFirst();
  }
}
