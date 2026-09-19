package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormStyleReferences;
import de.a12.studio.models.formmodel.Style;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Every style an element uses ({@code style}/{@code headerStyle}) must be defined in the model-level styles
 * of the Model Settings, and every entry needs a name - SME picks style names from that list, so a dangling
 * one is a broken reference. Reports one error per use, on the element that holds it, naming the style.
 */
public final class FormStyleReferenceValidator implements ModelValidator {

  // Fallback elementId for a nameless entry in the model-level styles, which no screen element owns.
  public static final String ELEMENT_ID = "content/styles";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Style style : formModel.getContent().getStyles()) {
      if (style.getName() == null || style.getName().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.formStyle.definitionWithoutName"), Severity.ERROR.name()));
      }
    }

    Set<String> defined = FormStyleReferences.definedNames(formModel.getContent());
    for (FormStyleReferences.Usage usage : FormStyleReferences.usages(formModel.getContent())) {
      String owner = usage.ownerName() != null && !usage.ownerName().isBlank() ? usage.ownerName() : usage.ownerId();
      for (Style style : usage.styles()) {
        if (style.getName() == null || style.getName().isBlank()) {
          errors.add(new ModelValidationError(model, usage.ownerId(),
              ValidationMessages.get("validation.formStyle.withoutName", owner), Severity.ERROR.name()));
        }
        else if (!defined.contains(style.getName())) {
          errors.add(new ModelValidationError(model, usage.ownerId(),
              ValidationMessages.get("validation.formStyle.undefined", style.getName(), owner), Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }
}
