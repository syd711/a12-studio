package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * A role must match SME's meta-model pattern {@code [_a-zA-Z][-_.a-zA-Z0-9]*} and be at most 100 characters long -
 * a12-studio's role text field otherwise accepts any non-blank text, and roles are referenced as bare identifiers
 * from Query/Form/Tree/Overview/Relationship UI Models ({@code targetRole}, {@code parentRole}, {@code Has(...)}).
 */
public final class RelationshipRoleFormatValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/entityCharacteristics/role";

  private static final int MAX_LENGTH = 100;
  private static final Pattern ROLE_PATTERN = Pattern.compile("[_a-zA-Z][-_.a-zA-Z0-9]*");

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipModel relationshipModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (EntityCharacteristic entity : relationshipModel.getContent().getEntityCharacteristics()) {
      String role = entity.getRole();
      if (role == null || role.isBlank()) {
        continue;
      }
      if (role.length() > MAX_LENGTH) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.relationshipRoleFormat.tooLong", role), Severity.ERROR.name()));
      }
      else if (!ROLE_PATTERN.matcher(role).matches()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.relationshipRoleFormat.invalid", role), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
