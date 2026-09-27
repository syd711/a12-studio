package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.TextContainer;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link FieldConfigEntry#getPlaceholder()} is meaningless (and rejected by the Form Engine) once {@code
 * exposition} is {@code "FULL"} or {@code "INLINE"} - SME's {@code
 * graph_mustNotHavePlaceholderWhenFullOrInlineExposition}.
 */
public final class FormPlaceholderExpositionConflictValidator implements ModelValidator {

  private static final String EXPOSITION_FULL = "FULL";
  private static final String EXPOSITION_INLINE = "INLINE";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null
        || formModel.getContent().getFieldConfiguration() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
      boolean conflictingExposition = EXPOSITION_FULL.equals(entry.getExposition()) || EXPOSITION_INLINE.equals(entry.getExposition());
      if (conflictingExposition && hasText(entry.getPlaceholder())) {
        errors.add(new ModelValidationError(model, FormFieldReferenceValidator.ELEMENT_ID,
            ValidationMessages.get("validation.formPlaceholderExposition.conflict", entry.getElementRef(), entry.getExposition()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static boolean hasText(TextContainer container) {
    if (container == null || container.getText() == null) {
      return false;
    }
    for (Label label : container.getText()) {
      if (label.getText() != null && !label.getText().isBlank()) {
        return true;
      }
    }
    return false;
  }
}
