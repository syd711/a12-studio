package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.MultiColumnSection;
import de.a12.studio.models.formmodel.Screen;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.formmodel.Section;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Screen names and sibling screen-element names must be unique (SME: element names must be unique among siblings). */
public final class FormSiblingNameUniquenessValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/screens";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    Set<String> screenNames = new HashSet<>();
    for (Screen screen : formModel.getContent().getScreens()) {
      if (screen.getName() != null && !screenNames.add(screen.getName())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.formSiblingNameUniqueness.duplicateScreen", screen.getName()),
            Severity.ERROR.name()));
      }
      checkSiblings(model, screen.getScreenElements(), errors);
    }
    return errors;
  }

  private void checkSiblings(A12Model<?> model, List<ScreenElement> siblings, List<ModelValidationError> errors) {
    if (siblings == null) {
      return;
    }
    Set<String> names = new HashSet<>();
    for (ScreenElement element : siblings) {
      if (element.getName() != null && !element.getName().isBlank() && !names.add(element.getName())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.formSiblingNameUniqueness.duplicateSibling", element.getName()),
            Severity.ERROR.name()));
      }
      if (element instanceof MultiColumnSection section) {
        checkSiblings(model, section.getScreenElements(), errors);
      }
      else if (element instanceof Section section) {
        checkSiblings(model, section.getScreenElements(), errors);
      }
    }
  }
}
