package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.List;

/**
 * One independent validation check, run against a model by the model type's own validation service (e.g.
 * {@code DocumentModelValidationService}). Implementations return an empty list for models they don't apply
 * to, so a single validator list can be shared by every model type's service without per-type filtering.
 */
public interface ModelValidator {

  List<ModelValidationError> validate(A12Model<?> model, ValidationContext context);
}
