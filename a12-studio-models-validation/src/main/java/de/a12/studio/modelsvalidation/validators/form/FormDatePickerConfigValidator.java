package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.DatePickerConfig;
import de.a12.studio.models.formmodel.FieldBasedRepeatOverviewColumn;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The year range of a Control's or column's date picker must make sense (SME {@code I_DatePickerConfig.json}):
 * the minimal year not above the maximal year, the preselection year within that range, and no negative year
 * when the years are absolute. See {@link DatePickerSupport#problems}.
 */
public final class FormDatePickerConfigValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Control control : FormModelWalker.find(formModel.getContent(), Control.class)) {
      report(model, control.getId(), control.getDatePickerConfig(), errors);
    }
    for (FieldBasedRepeatOverviewColumn column : FormModelWalker.find(formModel.getContent(), FieldBasedRepeatOverviewColumn.class)) {
      report(model, column.getId(), column.getDatePickerConfig(), errors);
    }
    return errors;
  }

  private static void report(A12Model<?> model, String elementId, DatePickerConfig config, List<ModelValidationError> errors) {
    for (DatePickerSupport.Problem problem : DatePickerSupport.problems(config)) {
      errors.add(new ModelValidationError(model, elementId, ValidationMessages.get(problem.messageKey()), Severity.ERROR.name()));
    }
  }
}
