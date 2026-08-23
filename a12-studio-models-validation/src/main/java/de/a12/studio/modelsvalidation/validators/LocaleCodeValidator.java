package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Locale;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Locale codes must be ISO 639 alpha-2/alpha-3 codes with an optional _REGION suffix, e.g. "en" or
 * "en_US" (mirroring SME's header rule "$code$ is not a valid locale code"). Applies to every model type.
 */
public final class LocaleCodeValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/locales/code";

  private static final Pattern LOCALE_PATTERN = Pattern.compile("^[a-z]{2,3}(_[A-Z]{2})?$");

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (model.getLocales() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Locale locale : model.getLocales()) {
      String code = locale.getCode();
      if (code == null || !LOCALE_PATTERN.matcher(code).matches()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.localeCode.invalid", code),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
