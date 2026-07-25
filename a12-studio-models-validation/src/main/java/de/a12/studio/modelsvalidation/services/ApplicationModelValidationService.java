package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/** Validates an {@link ApplicationModel}: currently just the generic header checks (locale). */
public final class ApplicationModelValidationService {

  private static final List<ModelValidator> VALIDATORS = List.of(new MissingLocaleValidator());

  public List<ModelValidationError> validate(ApplicationModel model, ValidationContext context) {
    return ValidatorRunner.runAll(VALIDATORS, model, context);
  }
}
