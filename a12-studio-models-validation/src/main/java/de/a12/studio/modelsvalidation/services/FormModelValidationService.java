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
import de.a12.studio.modelsvalidation.validators.form.ControlGridLayoutValidator;
import de.a12.studio.modelsvalidation.validators.form.FormButtonScreenReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormDocumentModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormLayoutColumnSumValidator;
import de.a12.studio.modelsvalidation.validators.form.FormSiblingNameUniquenessValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates a {@link FormModel}: generic header checks plus the form-specific rules ported from SME. */
public final class FormModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new FormDocumentModelReferenceValidator(),
      new FormFieldReferenceValidator(),
      new FormButtonScreenReferenceValidator(),
      new FormLayoutColumnSumValidator(),
      new FormSiblingNameUniquenessValidator(),
      new ControlGridLayoutValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(FormModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
