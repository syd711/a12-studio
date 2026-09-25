package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.typesetting.TypesettingLineLimitValidator;
import de.a12.studio.modelsvalidation.validators.typesetting.TypesettingRuleDuplicateValidator;
import de.a12.studio.modelsvalidation.validators.typesetting.TypesettingRuleValueValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a {@link TypesettingModel}: the generic header checks, the {@code roles} rules and the rules of the
 * {@code print-typesetting} editor (rule values, uniqueness, orphan/widow limits). No locale validators: a
 * Typesetting Model has no locales, so "at least one locale is required" would always fire.
 */
public final class TypesettingModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderRolesValidator(),
      new TypesettingRuleValueValidator(),
      new TypesettingRuleDuplicateValidator(),
      new TypesettingLineLimitValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(TypesettingModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
