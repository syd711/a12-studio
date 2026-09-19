package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.CustomScreenElement;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The fixed height of a custom screen element must not be zero (SME's {@code NumberType} with
 * {@code zeroNotAllowed} on {@code CustomScreenElement.json}). An absent height is fine.
 */
public final class FormCustomScreenElementHeightValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (CustomScreenElement element : FormModelWalker.find(formModel.getContent(), CustomScreenElement.class)) {
      if (element.getHeight() != null && element.getHeight() == 0) {
        String name = element.getName() != null && !element.getName().isBlank() ? element.getName() : element.getId();
        errors.add(new ModelValidationError(model, element.getId(),
            ValidationMessages.get("validation.customScreenElementHeight.zero", name), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
