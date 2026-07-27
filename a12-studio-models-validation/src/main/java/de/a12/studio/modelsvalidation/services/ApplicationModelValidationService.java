package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.application.ApplicationSceneGraphValidator;
import de.a12.studio.modelsvalidation.validators.application.ApplicationUniqueNamesValidator;
import de.a12.studio.modelsvalidation.validators.application.ApplicationViewAddValidator;

import java.util.List;

/** Validates an {@link ApplicationModel}: generic header checks plus the app-model rules ported from SME. */
public final class ApplicationModelValidationService {

  private static final List<ModelValidator> VALIDATORS = List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new ApplicationUniqueNamesValidator(),
      new ApplicationSceneGraphValidator(),
      new ApplicationViewAddValidator());

  public List<ModelValidationError> validate(ApplicationModel model, ValidationContext context) {
    return ValidatorRunner.runAll(VALIDATORS, model, context);
  }
}
