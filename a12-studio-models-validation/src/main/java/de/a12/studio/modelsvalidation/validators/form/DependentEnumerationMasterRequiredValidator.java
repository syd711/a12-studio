package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/** A {@code dependentEnumeration} block requires a master field to be selected. */
public final class DependentEnumerationMasterRequiredValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null
        || formModel.getContent().getFieldConfiguration() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
      if (entry.getDependentEnumeration() != null
          && (entry.getDependentEnumeration().getMasterField() == null || entry.getDependentEnumeration().getMasterField().isBlank())) {
        errors.add(new ModelValidationError(model, FormFieldReferenceValidator.ELEMENT_ID,
            ValidationMessages.get("validation.dependentEnumeration.masterRequired", entry.getElementRef()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
