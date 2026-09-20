package de.a12.studio.modelsvalidation.services;

import de.a12.studio.models.formmodel.FormModel;
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
import de.a12.studio.modelsvalidation.validators.form.ControlGridLayoutValidator;
import de.a12.studio.modelsvalidation.validators.form.DependentControlOptionsMustExistValidator;
import de.a12.studio.modelsvalidation.validators.form.DependentControlsAtLeastOneOptionValidator;
import de.a12.studio.modelsvalidation.validators.form.DependentEnumerationMasterRequiredValidator;
import de.a12.studio.modelsvalidation.validators.form.DependentFieldAtLeastOneActionValidator;
import de.a12.studio.modelsvalidation.validators.form.DependentFieldMasterRequiredValidator;
import de.a12.studio.modelsvalidation.validators.form.DependentGroupMasterRequiredValidator;
import de.a12.studio.modelsvalidation.validators.form.ExternalEnumerationSourceRequiredValidator;
import de.a12.studio.modelsvalidation.validators.form.FormControlIndexRequiredValidator;
import de.a12.studio.modelsvalidation.validators.form.FormBindingRelationshipReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormBindingTargetRoleValidator;
import de.a12.studio.modelsvalidation.validators.form.FormButtonScreenReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormColumnWidthValidator;
import de.a12.studio.modelsvalidation.validators.form.FormCustomScreenElementHeightValidator;
import de.a12.studio.modelsvalidation.validators.form.FormDatePickerConfigValidator;
import de.a12.studio.modelsvalidation.validators.form.FormDefaultRowActionValidator;
import de.a12.studio.modelsvalidation.validators.form.FormDependencyDriftValidator;
import de.a12.studio.modelsvalidation.validators.form.FormDependentControlContextValidator;
import de.a12.studio.modelsvalidation.validators.form.FormDocumentModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormFieldReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormIncludeProvenanceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormGroupReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormInitiallyFocusedElementValidator;
import de.a12.studio.modelsvalidation.validators.form.FormLayoutColumnSumValidator;
import de.a12.studio.modelsvalidation.validators.form.FormReferenceTypeDriftValidator;
import de.a12.studio.modelsvalidation.validators.form.FormSiblingNameUniquenessValidator;
import de.a12.studio.modelsvalidation.validators.form.FormStyleReferenceValidator;
import de.a12.studio.modelsvalidation.validators.form.FormUnusedConfigEntryValidator;
import de.a12.studio.modelsvalidation.validators.form.HideConditionAtLeastOneCaseValidator;
import de.a12.studio.modelsvalidation.validators.form.HideConditionSupportedValuesValidator;

import java.util.ArrayList;
import java.util.List;

/** Validates a {@link FormModel}: generic header checks plus the form-specific rules ported from SME. */
public final class FormModelValidationService {

  private final List<ModelValidator> validators = new ArrayList<>(List.of(
      new MissingLocaleValidator(),
      new LocaleCodeValidator(),
      new ModelIdFilenameValidator(),
      new ModelSuffixValidator(),
      new UniqueModelIdValidator(),
      new NameConventionValidator(),
      new FormDocumentModelReferenceValidator(),
      new FormFieldReferenceValidator(),
      new FormGroupReferenceValidator(),
      new FormUnusedConfigEntryValidator(),
      new FormButtonScreenReferenceValidator(),
      new FormLayoutColumnSumValidator(),
      new FormSiblingNameUniquenessValidator(),
      new ControlGridLayoutValidator(),
      new HideConditionAtLeastOneCaseValidator(),
      new HideConditionSupportedValuesValidator(),
      new DependentFieldMasterRequiredValidator(),
      new DependentGroupMasterRequiredValidator(),
      new DependentEnumerationMasterRequiredValidator(),
      new DependentFieldAtLeastOneActionValidator(),
      new DependentControlOptionsMustExistValidator(),
      new DependentControlsAtLeastOneOptionValidator(),
      new ExternalEnumerationSourceRequiredValidator(),
      new FormBindingRelationshipReferenceValidator(),
      new FormBindingTargetRoleValidator(),
      new FormDefaultRowActionValidator(),
      new FormStyleReferenceValidator(),
      new FormDatePickerConfigValidator(),
      new FormColumnWidthValidator(),
      new FormInitiallyFocusedElementValidator(),
      new FormCustomScreenElementHeightValidator(),
      new FormDependencyDriftValidator(),
      new FormDependentControlContextValidator(),
      new FormReferenceTypeDriftValidator(),
      new FormControlIndexRequiredValidator(),
      new FormIncludeProvenanceValidator()));

  public void addValidator(ModelValidator validator) {
    validators.add(validator);
  }

  public void removeValidator(ModelValidator validator) {
    validators.remove(validator);
  }

  public List<ModelValidationError> validate(FormModel model, ValidationContext context) {
    return ValidatorRunner.runAll(validators, model, context);
  }
}
