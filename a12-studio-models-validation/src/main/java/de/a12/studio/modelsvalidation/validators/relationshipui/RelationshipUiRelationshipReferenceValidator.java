package de.a12.studio.modelsvalidation.validators.relationshipui;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * {@code content.relationshipName} must be set and must reference an existing {@link RelationshipModel} in the
 * workspace. Unlike a header {@code ModelReference} (see {@link
 * de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator}), this is a plain content string
 * with no corresponding header reference in any real fixture, so it needs its own check.
 */
public final class RelationshipUiRelationshipReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/relationshipName";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipUiModel relationshipUiModel)) {
      return List.of();
    }
    String relationshipName = relationshipUiModel.getContent().getRelationshipName();
    if (relationshipName == null || relationshipName.isBlank()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.relationshipUiRelationshipReference.missing"), Severity.ERROR.name()));
    }
    if (!(context.findOtherModel(relationshipName) instanceof RelationshipModel)) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.relationshipUiRelationshipReference.notFound", relationshipName), Severity.ERROR.name()));
    }
    return List.of();
  }
}
