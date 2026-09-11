package de.a12.studio.modelsvalidation.validators.relationshipui;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * {@code content.targetRole} must be set and, when {@code content.relationshipName} resolves to a real {@link
 * RelationshipModel} (see {@link RelationshipUiRelationshipReferenceValidator}), must match one of that
 * relationship's entity roles.
 */
public final class RelationshipUiTargetRoleValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/targetRole";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipUiModel relationshipUiModel)) {
      return List.of();
    }
    String targetRole = relationshipUiModel.getContent().getTargetRole();
    if (targetRole == null || targetRole.isBlank()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.relationshipUiTargetRole.missing"), Severity.ERROR.name()));
    }

    String relationshipName = relationshipUiModel.getContent().getRelationshipName();
    if (!(context.findOtherModel(relationshipName) instanceof RelationshipModel relationshipModel)) {
      // Unresolved relationship is already reported by RelationshipUiRelationshipReferenceValidator.
      return List.of();
    }
    boolean matches = relationshipModel.getContent().getEntityCharacteristics().stream()
        .map(EntityCharacteristic::getRole)
        .anyMatch(targetRole::equals);
    if (!matches) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.relationshipUiTargetRole.notFound", targetRole, relationshipName), Severity.ERROR.name()));
    }
    return List.of();
  }
}
