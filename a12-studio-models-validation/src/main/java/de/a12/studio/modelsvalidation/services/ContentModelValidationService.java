package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentBaseGroupValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentDocumentModelTypeValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentElementIdUniqueValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentEventNodeValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentFormElementValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentGroupReferenceValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentNodeShapeValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentRootElementValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentSettingsValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentStructureValidator;
import de.a12.studio.modelsvalidation.validators.content.ContentWarningsValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a {@link ContentModel}: generic header checks, the Document Model binding and base group, the element tree
 * (shape, structure rules, settings) and everything the elements reference in the Document Model.
 */
public final class ContentModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new HeaderRolesValidator(),
      new ContentDocumentModelTypeValidator(),
      new ContentRootElementValidator(),
      new ContentElementIdUniqueValidator(),
      new ContentNodeShapeValidator(),
      new ContentStructureValidator(),
      new ContentBaseGroupValidator(),
      new ContentGroupReferenceValidator(),
      new ContentFieldReferenceValidator(),
      new ContentFormElementValidator(),
      new ContentEventNodeValidator(),
      new ContentSettingsValidator(),
      new ContentWarningsValidator()));

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
