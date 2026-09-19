package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A screen's initially focused element ({@code initiallyFocusedElementId}) may only be set on the first screen
 * (SME's {@code InitialFocusedElementOnlyOnFirstScreen}) and must name a Control that can take the focus there
 * (SME's {@code InvalidReference} over {@code focusableElementsEnum}, see {@link InitiallyFocusedElementSupport}).
 * An empty value is fine.
 */
public final class FormInitiallyFocusedElementValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    FormModelContent content = formModel.getContent();
    List<ModelValidationError> errors = new ArrayList<>();
    for (Screen screen : content.getScreens()) {
      String focusedId = screen.getInitiallyFocusedElementId();
      if (focusedId == null || focusedId.isBlank()) {
        continue;
      }
      String screenName = screen.getName() != null && !screen.getName().isBlank() ? screen.getName() : screen.getId();
      if (!InitiallyFocusedElementSupport.isFirstScreen(content, screen)) {
        errors.add(new ModelValidationError(model, screen.getId(),
            ValidationMessages.get("validation.initiallyFocusedElement.notFirstScreen", screenName), Severity.ERROR.name()));
      }
      else if (InitiallyFocusedElementSupport.focusableControls(screen).stream().map(Control::getId).noneMatch(focusedId::equals)) {
        errors.add(new ModelValidationError(model, screen.getId(),
            ValidationMessages.get("validation.initiallyFocusedElement.notFocusable", focusedId, screenName), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
