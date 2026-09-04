package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.masterdetail.MasterDetailReferenceValidator;
import de.a12.studio.modelsvalidation.validators.masterdetail.MasterDetailTypeConsistencyValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates a {@link MasterDetailModel}: generic header checks plus reference/type consistency. */
public final class MasterDetailModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new MasterDetailReferenceValidator(),
      new MasterDetailTypeConsistencyValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(MasterDetailModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
