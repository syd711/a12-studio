package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/** When a multiplicity is not unbounded, an upper limit of at least 1 is required (SME: "When not unbounded, upperLimit must have a value"). */
public final class RelationshipUpperLimitValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/entityCharacteristics/linkConstraints/multiplicity";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipModel relationshipModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (EntityCharacteristic entity : relationshipModel.getContent().getEntityCharacteristics()) {
      Multiplicity multiplicity = entity.getLinkConstraints() != null ? entity.getLinkConstraints().getMultiplicity() : null;
      if (multiplicity == null || Boolean.TRUE.equals(multiplicity.getUnbounded())) {
        continue;
      }
      if (multiplicity.getUpperLimit() == null || multiplicity.getUpperLimit() < 1) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "When not unbounded, the multiplicity of role \"" + entity.getRole() + "\" must have an upper limit of at least 1.",
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
