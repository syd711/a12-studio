package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Generated Document Models are named "&lt;model&gt;_&lt;role&gt;____generated" (14 extra characters);
 * that name must not exceed the 100 character model-name limit (SME: "Given the role ... the generated
 * document model name will exceed 100 characters").
 */
public final class RelationshipGeneratedDmNameLengthValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/entityCharacteristics/role";

  /** "_" + "____generated" appended around the role by the generator. */
  private static final int GENERATED_SUFFIX_LENGTH = 14;
  private static final int MAX_NAME_LENGTH = 100;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipModel relationshipModel) || model.getId() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (EntityCharacteristic entity : relationshipModel.getContent().getEntityCharacteristics()) {
      if (entity.getRole() == null) {
        continue;
      }
      if (model.getId().length() + entity.getRole().length() + GENERATED_SUFFIX_LENGTH > MAX_NAME_LENGTH) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "Given the role \"" + entity.getRole() + "\" and the model name \"" + model.getId()
                + "\", the generated document model name will exceed 100 characters.",
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
