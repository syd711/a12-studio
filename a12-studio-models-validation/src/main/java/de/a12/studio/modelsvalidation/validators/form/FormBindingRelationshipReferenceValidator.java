package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Every {@link Binding}'s {@code binding.details.relationshipName} must be set and must reference an existing
 * {@link RelationshipModel} in the workspace - the Form Model analogue of {@code
 * de.a12.studio.modelsvalidation.validators.relationshipui.RelationshipUiRelationshipReferenceValidator}.
 */
public final class FormBindingRelationshipReferenceValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Binding binding : FormBindingElements.findBindings(formModel)) {
      String relationshipName = binding.getBinding().getDetails().getRelationshipName();
      if (relationshipName == null || relationshipName.isBlank()) {
        errors.add(new ModelValidationError(model, binding.getId(),
            ValidationMessages.get("validation.formBindingRelationshipReference.missing"), Severity.ERROR.name()));
      }
      else if (!(context.findOtherModel(relationshipName) instanceof RelationshipModel)) {
        errors.add(new ModelValidationError(model, binding.getId(),
            ValidationMessages.get("validation.formBindingRelationshipReference.notFound", relationshipName), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
