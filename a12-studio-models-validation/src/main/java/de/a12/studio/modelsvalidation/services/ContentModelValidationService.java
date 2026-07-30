package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.contentmodel.ContentModel;
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
import de.a12.studio.modelsvalidation.validators.content.ContentElementIdUniqueValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentRootElementValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates a {@link ContentModel}: generic header checks plus content-tree structure rules. */
public final class ContentModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new ContentRootElementValidator(),
      new ContentElementIdUniqueValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(ContentModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
