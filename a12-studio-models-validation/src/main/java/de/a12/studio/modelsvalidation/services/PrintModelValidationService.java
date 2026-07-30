package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.printmodel.PrintModel;
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
import de.a12.studio.modelsvalidation.validators.print.PrintCalculationValidator;
import de.a12.studio.modelsvalidation.validators.print.PrintDocumentModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.print.PrintElementReferenceIntegrityValidator;
import de.a12.studio.modelsvalidation.validators.print.PrintFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.print.PrintHeadlineOrderValidator;
import de.a12.studio.modelsvalidation.validators.print.PrintImageValidator;
import de.a12.studio.modelsvalidation.validators.print.PrintTableColumnWidthValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates a {@link PrintModel}: generic header checks plus rules ported from the print engine's validation. */
public final class PrintModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new PrintDocumentModelReferenceValidator(),
      new PrintFieldReferenceValidator(),
      new PrintElementReferenceIntegrityValidator(),
      new PrintCalculationValidator(),
      new PrintTableColumnWidthValidator(),
      new PrintImageValidator(),
      new PrintHeadlineOrderValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(PrintModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
