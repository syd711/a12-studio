package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.DocumentModelConsistencyValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.MissingReferenceValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.TimeZoneValidator;

import java.util.List;

/** Validates a {@link DocumentModel} (and its {@code TypeDefinitionModel} subclass): structural reference
 * checks, the ported kernel consistency rules, and the generic header checks (locale, time zone). */
public final class DocumentModelValidationService {

  private static final List<ModelValidator> VALIDATORS = List.of(
      new MissingReferenceValidator(),
      new DocumentModelConsistencyValidator(),
      new MissingLocaleValidator(),
      new TimeZoneValidator());

  public List<ModelValidationError> validate(DocumentModel model, ValidationContext context) {
    return ValidatorRunner.runAll(VALIDATORS, model, context);
  }
}
