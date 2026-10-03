package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.AnnotationDuplicateValidator;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerConfigurationValidator;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerCustomTextsValidator;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerElementSelectionValidator;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerSourceValidator;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerTypeMappingConfigValidator;
import de.a12.studio.modelsvalidation.validators.transformer.TransformerTypeMappingValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a {@link TransformerModel}: the generic header checks SME's header rules map to (at least one locale,
 * locale codes, a unique name that follows the naming convention and the filename, roles, unique annotation names)
 * plus the rules of the {@code TransformerConfigModel} meta model for the Transformation, Element Selection and
 * Custom Texts tabs. What only the transformation itself can tell (the XSD does not have that root element, a type
 * mapping names an unknown XSD type) is not validated here: it comes back as the transformation's issues in the editor.
 */
public final class TransformerModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderRolesValidator(),
      new AnnotationDuplicateValidator(),
      new TransformerSourceValidator(),
      new TransformerTypeMappingValidator(),
      new TransformerTypeMappingConfigValidator(),
      new TransformerElementSelectionValidator(),
      new TransformerCustomTextsValidator(),
      new TransformerConfigurationValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(TransformerModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
