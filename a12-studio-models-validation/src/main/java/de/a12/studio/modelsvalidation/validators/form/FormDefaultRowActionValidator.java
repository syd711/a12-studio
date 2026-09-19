package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.RowAction;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A Repeat's default row action must be one the editor could have offered (see {@link
 * DefaultRowActionSupport#candidates}): "edit", "download" only with multi file upload, or one of the repeat's
 * own row actions - which must exist and must not define a confirmation, since clicking a row can't open a
 * dialog first. Catches what a hand-edit, or a change the editor didn't reconcile, leaves behind.
 */
public final class FormDefaultRowActionValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (AbstractRepeat repeat : FormRepeats.collect(formModel.getContent())) {
      String event = repeat.getDefaultRowAction() == null ? null : repeat.getDefaultRowAction().getEvent();
      String technicalEvent = DefaultRowActionSupport.technicalEvent(repeat.getDefaultRowAction());
      if (technicalEvent == null || DefaultRowActionSupport.candidates(repeat).contains(technicalEvent)) {
        continue;
      }
      String repeatName = repeat.getName() != null && !repeat.getName().isBlank() ? repeat.getName() : repeat.getId();
      errors.add(new ModelValidationError(model, repeat.getId(), message(repeat, repeatName, event, technicalEvent),
          Severity.ERROR.name()));
    }
    return errors;
  }

  private static String message(AbstractRepeat repeat, String repeatName, String event, String technicalEvent) {
    if (DefaultRowActionSupport.DOWNLOAD.equals(technicalEvent)) {
      return ValidationMessages.get("validation.defaultRowAction.downloadNeedsMultiFileUpload", repeatName);
    }
    if (technicalEvent.startsWith(DefaultRowActionSupport.BUILT_IN_PREFIX)) {
      return ValidationMessages.get("validation.defaultRowAction.unknownBuiltIn", repeatName, event);
    }
    boolean exists = repeat.getRowActionGroup() != null
        && repeat.getRowActionGroup().getAction().stream().map(RowAction::getEvent).anyMatch(event::equals);
    return ValidationMessages.get(exists
        ? "validation.defaultRowAction.customWithConfirmation"
        : "validation.defaultRowAction.customMissing", repeatName, event);
  }
}
