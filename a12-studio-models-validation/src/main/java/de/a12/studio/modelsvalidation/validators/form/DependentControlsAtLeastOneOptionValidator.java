package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Control;
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
 * A Control that has a {@code dependentControls} block must list at least one screen element in it (SME's
 * {@code DependentControlsAtLeastOneOptionMustBeSelected}: "An empty control dependency is set up for this
 * control. At least one option must be selected."). Entries whose {@code idref} is blank or dangling are
 * {@link DependentControlOptionsMustExistValidator}'s business.
 */
public final class DependentControlsAtLeastOneOptionValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Control control : FormModelWalker.find(formModel.getContent(), Control.class)) {
      if (control.getDependentControls() != null && control.getDependentControls().getScreenElement().isEmpty()) {
        errors.add(new ModelValidationError(model, control.getId(),
            ValidationMessages.get("validation.dependentControls.atLeastOneOption",
                DependentControlOptionsMustExistValidator.controlLabel(control)),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
