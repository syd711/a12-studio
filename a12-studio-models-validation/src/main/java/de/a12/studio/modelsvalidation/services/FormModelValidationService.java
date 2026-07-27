package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.form.FormDocumentModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormLayoutColumnSumValidator;
import de.a12.studio.modelsvalidation.validators.form.FormSiblingNameUniquenessValidator;

import java.util.List;

/** Validates a {@link FormModel}: generic header checks plus the form-specific rules ported from SME. */
public final class FormModelValidationService {

  private static final List<ModelValidator> VALIDATORS = List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new FormDocumentModelReferenceValidator(),
      new FormFieldReferenceValidator(),
      new FormLayoutColumnSumValidator(),
      new FormSiblingNameUniquenessValidator());

  public List<ModelValidationError> validate(FormModel model, ValidationContext context) {
    return ValidatorRunner.runAll(VALIDATORS, model, context);
  }
}
