package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.GroupConfigEntry;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/** A {@code dependentGroup} block requires a master field to be selected. */
public final class DependentGroupMasterRequiredValidator implements ModelValidator {

  static final String ELEMENT_ID = "content/groupConfiguration";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null
        || formModel.getContent().getGroupConfiguration() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (GroupConfigEntry entry : formModel.getContent().getGroupConfiguration().getGroup()) {
      if (entry.getDependentGroup() != null
          && (entry.getDependentGroup().getMasterField() == null || entry.getDependentGroup().getMasterField().isBlank())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.dependentGroup.masterRequired", entry.getGroupRef()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
