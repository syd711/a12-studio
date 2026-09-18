package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.selection.SelectionDefaultMissingValidator;
import de.a12.studio.modelsvalidation.validators.selection.SelectionDuplicatePathValidator;
import de.a12.studio.modelsvalidation.validators.selection.SelectionPathInBothListsValidator;
import de.a12.studio.modelsvalidation.validators.selection.SelectionPathPatternValidator;
import de.a12.studio.modelsvalidation.validators.selection.SelectionRedundantDefaultValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates a {@link SelectionModel}: generic header checks plus the selection-specific rules ported from SME. */
public final class SelectionModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new SelectionDefaultMissingValidator(),
      new SelectionPathPatternValidator(),
      new SelectionDuplicatePathValidator(),
      new SelectionPathInBothListsValidator(),
      new SelectionRedundantDefaultValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(SelectionModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
