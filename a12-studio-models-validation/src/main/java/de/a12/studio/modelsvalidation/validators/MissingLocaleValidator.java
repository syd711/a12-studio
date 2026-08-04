package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.List;

/**
 * At least one locale is required, for the Locales settings panel. Applies to every model type: locales live
 * on the {@link A12Model} header, not on any type-specific content.
 */
public final class MissingLocaleValidator implements ModelValidator {

  // Not a real element id: A12Model's locales live on the header, not in an element tree, but every other
  // ModelValidationError is keyed by a (possibly null) element id, so header-level checks like this one need
  // a stable, non-null placeholder to be distinguishable from each other by callers that filter on it (see
  // de.a12.studio.modelsvalidation.ValidationService#getMissingLocaleError).
  public static final String ELEMENT_ID = "header/locales";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    boolean missing = model.getLocales() == null || model.getLocales().isEmpty();
    if (!missing) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID, "Please add at least one locale.", Severity.ERROR.name()));
  }
}
