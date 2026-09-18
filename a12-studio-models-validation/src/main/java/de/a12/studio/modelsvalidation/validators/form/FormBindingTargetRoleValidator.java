package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Every {@link Binding}'s {@code binding.details.targetRole} must be set and, when {@code relationshipName}
 * resolves to a real {@link RelationshipModel} (see {@link FormBindingRelationshipReferenceValidator}), must
 * match one of that relationship's entity roles - the Form Model analogue of {@code
 * de.a12.studio.modelsvalidation.validators.relationshipui.RelationshipUiTargetRoleValidator}.
 */
public final class FormBindingTargetRoleValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Binding binding : FormBindingElements.findBindings(formModel)) {
      String targetRole = binding.getBinding().getDetails().getTargetRole();
      if (targetRole == null || targetRole.isBlank()) {
        errors.add(new ModelValidationError(model, binding.getId(),
            ValidationMessages.get("validation.formBindingTargetRole.missing"), Severity.ERROR.name()));
        continue;
      }
      String relationshipName = binding.getBinding().getDetails().getRelationshipName();
      if (!(context.findOtherModel(relationshipName) instanceof RelationshipModel relationshipModel)) {
        // Unresolved relationship is already reported by FormBindingRelationshipReferenceValidator.
        continue;
      }
      boolean matches = relationshipModel.getContent().getEntityCharacteristics().stream()
          .map(EntityCharacteristic::getRole)
          .anyMatch(targetRole::equals);
      if (!matches) {
        errors.add(new ModelValidationError(model, binding.getId(),
            ValidationMessages.get("validation.formBindingTargetRole.notFound", targetRole, relationshipName), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
