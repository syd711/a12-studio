package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Roles of the related entities must be unique (SME: "The role ... is already taken by the other entity."). */
public final class RelationshipUniqueRolesValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/entityCharacteristics/role";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipModel relationshipModel)) {
      return List.of();
    }
    Set<String> seen = new HashSet<>();
    for (EntityCharacteristic entity : relationshipModel.getContent().getEntityCharacteristics()) {
      if (entity.getRole() != null && !entity.getRole().isBlank() && !seen.add(entity.getRole())) {
        return List.of(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.relationshipUniqueRoles.duplicate", entity.getRole()), Severity.ERROR.name()));
      }
    }
    return List.of();
  }
}
