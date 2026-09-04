package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.overviewmodel.OverviewModel;
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
import de.a12.studio.modelsvalidation.validators.overview.OverviewColumnsNotEmptyValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewDocumentModelRequiredValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewFilterCustomFieldsValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewFilterGroupsValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewFilterModeRequiredValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewFilterSectionsValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewInitialSortingReferenceValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewMultiSelectionElementValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewPagingSizeValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewStylesValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates an {@link OverviewModel}: generic header checks plus the overview-specific rules ported from SME. */
public final class OverviewModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new HeaderModelReferenceValidator(),
      new OverviewColumnsNotEmptyValidator(),
      new OverviewFieldReferenceValidator(),
      new OverviewDocumentModelRequiredValidator(),
      new OverviewFilterModeRequiredValidator(),
      new OverviewFilterCustomFieldsValidator(),
      new OverviewFilterSectionsValidator(),
      new OverviewFilterGroupsValidator(),
      new OverviewMultiSelectionElementValidator(),
      new OverviewPagingSizeValidator(),
      new OverviewInitialSortingReferenceValidator(),
      new OverviewStylesValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(OverviewModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
