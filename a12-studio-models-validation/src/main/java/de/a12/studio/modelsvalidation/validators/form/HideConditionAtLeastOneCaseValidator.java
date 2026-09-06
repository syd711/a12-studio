package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Every hide condition with a master field selected must have at least one trigger value checked (SME:
 * "At least one hide condition value needs to be selected when a condition field is selected.").
 */
public final class HideConditionAtLeastOneCaseValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (HideConditionElements.Entry entry : HideConditionElements.collect(formModel.getContent())) {
      String masterField = entry.hideCondition().getMasterField();
      if (masterField != null && !masterField.isBlank() && entry.hideCondition().getCases().isEmpty()) {
        errors.add(new ModelValidationError(model, entry.nodeId(),
            ValidationMessages.get("validation.hideCondition.atLeastOneCase"), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
