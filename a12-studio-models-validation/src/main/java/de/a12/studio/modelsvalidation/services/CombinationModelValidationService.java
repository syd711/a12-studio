package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidatorRunner;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.LocaleCodeValidator;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.ModelIdFilenameValidator;
import de.a12.studio.modelsvalidation.validators.ModelSuffixValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.NameConventionValidator;
import de.a12.studio.modelsvalidation.validators.UniqueModelIdValidator;
import de.a12.studio.modelsvalidation.validators.combination.CombinationAdditiveModelDuplicateValidator;
import de.a12.studio.modelsvalidation.validators.combination.CombinationAdditiveModelMissingValidator;
import de.a12.studio.modelsvalidation.validators.combination.CombinationAdditiveModelNotAllowedValidator;
import de.a12.studio.modelsvalidation.validators.combination.CombinationDecorationModelMissingValidator;
import de.a12.studio.modelsvalidation.validators.combination.CombinationDecorationModelNotAllowedValidator;
import de.a12.studio.modelsvalidation.validators.combination.CombinationSelectionModelMissingValidator;
import de.a12.studio.modelsvalidation.validators.combination.CombinationSelectionModelNotAllowedValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates a {@link CombinedDocumentModel}: generic header checks plus the structural per-step rules ported
 * from SME's {@code DomainCombination.json}. Real semantic validation (DM expansion, rule-contradiction
 * solving, loop detection) has no backing implementation in a12-studio yet - see
 * {@code docs/sme-reference-comparison.md}.
 */
public final class CombinationModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new CombinationAdditiveModelMissingValidator(),
      new CombinationSelectionModelMissingValidator(),
      new CombinationDecorationModelMissingValidator(),
      new CombinationAdditiveModelNotAllowedValidator(),
      new CombinationSelectionModelNotAllowedValidator(),
      new CombinationDecorationModelNotAllowedValidator(),
      new CombinationAdditiveModelDuplicateValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(CombinedDocumentModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
