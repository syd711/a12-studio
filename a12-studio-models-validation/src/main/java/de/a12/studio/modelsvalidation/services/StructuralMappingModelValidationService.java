package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.kernel.StructuralMappingKernelValidator;
import de.a12.studio.modelsvalidation.validators.AnnotationDuplicateValidator;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.HeaderRolesValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a {@link StructuralMappingModel}: the generic header checks plus the kernel's check of the mappings
 * themselves (see {@link StructuralMappingKernelValidator}), which is all SME validates this model type with.
 */
public final class StructuralMappingModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new HeaderRolesValidator(),
      new AnnotationDuplicateValidator(),
      new StructuralMappingKernelValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(StructuralMappingModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
