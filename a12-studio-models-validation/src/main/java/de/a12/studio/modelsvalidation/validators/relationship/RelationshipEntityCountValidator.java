package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/** A relationship must connect exactly two related entities (SME: "Please ensure that exactly two related entities are specified."). */
public final class RelationshipEntityCountValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/entityCharacteristics";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipModel relationshipModel)) {
      return List.of();
    }
    int count = relationshipModel.getContent().getEntityCharacteristics().size();
    if (count == 2) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.relationshipEntityCount.wrongCount"), Severity.ERROR.name()));
  }
}
