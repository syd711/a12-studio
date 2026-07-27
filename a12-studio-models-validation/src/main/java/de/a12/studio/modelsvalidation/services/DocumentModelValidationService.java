package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.AttachmentGroupValidator;
import de.a12.studio.modelsvalidation.validators.BasicConsistencyValidator;
import de.a12.studio.modelsvalidation.validators.DuplicateIdValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.MissingReferenceValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.MultiSelectGroupValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.NumberFieldValueLimitValidator;
import de.a12.studio.modelsvalidation.validators.SchemaVersionValidator;
import de.a12.studio.modelsvalidation.validators.StringPatternErrorMessageValidator;
import de.a12.studio.modelsvalidation.validators.TimeZoneValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;

import java.util.List;

/** Validates a {@link DocumentModel} (and its {@code TypeDefinitionModel} subclass): structural reference
 * checks, the ported kernel consistency rules, and the generic header checks (locale, time zone). */
public final class DocumentModelValidationService {

  private static final List<ModelValidator> VALIDATORS = List.of(
      new MissingReferenceValidator(),
      new SchemaVersionValidator(),
      new DuplicateIdValidator(),
      new NumberFieldValueLimitValidator(),
      new MultiSelectGroupValidator(),
      new AttachmentGroupValidator(),
      new BasicConsistencyValidator(),
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new TimeZoneValidator(),
      new StringPatternErrorMessageValidator());

  public List<ModelValidationError> validate(DocumentModel model, ValidationContext context) {
    return ValidatorRunner.runAll(VALIDATORS, model, context);
  }
}
