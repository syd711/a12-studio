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
import java.util.Set;

/**
 * Once a {@link FieldConfigEntry#getExternalEnumeration()} source is set, {@code exposition} must be one of
 * {@code "FULL"}/{@code "INLINE"}/{@code "COMPACT"}/{@code "AUTOCOMPLETE"} (absent counts as the default,
 * compact, presentation) - SME's {@code graph_externalEnumerationExpositionMustBeValid}.
 */
public final class FormExternalEnumerationExpositionValidator implements ModelValidator {

  private static final Set<String> VALID_EXPOSITIONS = Set.of("FULL", "INLINE", "COMPACT", "AUTOCOMPLETE");

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null
        || formModel.getContent().getFieldConfiguration() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
      boolean hasExternalSource = entry.getExternalEnumeration() != null
          && entry.getExternalEnumeration().getSrc() != null && !entry.getExternalEnumeration().getSrc().isBlank();
      if (hasExternalSource && entry.getExposition() != null && !VALID_EXPOSITIONS.contains(entry.getExposition())) {
        errors.add(new ModelValidationError(model, FormFieldReferenceValidator.ELEMENT_ID,
            ValidationMessages.get("validation.formExternalEnumerationExposition.invalid", entry.getElementRef(), entry.getExposition()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
