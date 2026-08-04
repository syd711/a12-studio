package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Model naming conventions enforced by the SME: at most 100 characters, only letters, digits,
 * hyphens, underscores and periods, starting with a letter or underscore, and never starting with
 * "xml" in any casing. Applies to every model type.
 */
public final class NameConventionValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/id";

  private static final Pattern NAME_PATTERN = Pattern.compile("^[_a-zA-Z][-_.a-zA-Z0-9]{0,99}$");

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    String id = model.getId();
    if (id == null || id.isBlank()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID, "validation.validation_the_model_name_must_not_be_empty", Severity.ERROR.name()));
    }

    List<ModelValidationError> errors = new ArrayList<>();
    if (!NAME_PATTERN.matcher(id).matches()) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          "validation.model_names_must_only_consist_of_letters_digits_hy"
              + "with an underscore or letter and must be at most 100 characters long.",
          Severity.ERROR.name()));
    }
    if (id.toLowerCase().startsWith("xml")) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          "validation.model_names_must_not_start_with"xml\".", Severity.ERROR.name()));
    }
    return errors;
  }
}
