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

/** An {@code externalEnumeration} block requires a source URL. */
public final class ExternalEnumerationSourceRequiredValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null
        || formModel.getContent().getFieldConfiguration() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
      if (entry.getExternalEnumeration() != null
          && (entry.getExternalEnumeration().getSrc() == null || entry.getExternalEnumeration().getSrc().isBlank())) {
        errors.add(new ModelValidationError(model, FormFieldReferenceValidator.ELEMENT_ID,
            ValidationMessages.get("validation.externalEnumeration.sourceRequired", entry.getElementRef()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
