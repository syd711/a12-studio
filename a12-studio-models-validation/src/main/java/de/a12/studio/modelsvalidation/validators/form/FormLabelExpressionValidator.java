package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.expressionlang.ExpressionLanguageSyntaxChecker;
import de.a12.studio.models.formmodel.ExpressionText;
import de.a12.studio.models.formmodel.FormLabelExpressions;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A label whose Label Type is switched to "Expression" ({@link ExpressionText}) must carry a non-blank,
 * syntactically valid Expression - SME's {@code I_Label.json} mixin rules {@code expressionMustNotBeEmpty}/
 * {@code expressionMustBeValid} ({@code ExpressionValidationCustomCondition}), which apply to every labeled
 * element (Screen/Row/Section title, Control/ExpressionCell/RepeatOverviewColumn/FieldConfigEntry/
 * GroupConfigEntry label, ButtonStyling label, the model's own Subtitle - see {@link FormLabelExpressions}).
 * Reuses the same "Expression" text-templating grammar/checker already built for an Overview Model's
 * expression columns ({@code ExpressionLanguageSyntaxChecker}); only syntax is checked, not field references.
 */
public final class FormLabelExpressionValidator implements ModelValidator {

  // Fallback elementId for a label with no reachable owner id (a model-level field, or a field-/group-
  // configuration entry override, neither of which has an id of its own).
  public static final String ELEMENT_ID = "content/label";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (FormLabelExpressions.Usage usage : FormLabelExpressions.usages(formModel.getContent())) {
      if (!(usage.text() instanceof ExpressionText expressionText)) {
        continue;
      }
      String elementId = usage.ownerId() != null ? usage.ownerId() : ELEMENT_ID;
      String owner = usage.ownerName() != null && !usage.ownerName().isBlank() ? usage.ownerName() : usage.ownerId();
      String expression = expressionText.getExpressionText();
      if (expression == null || expression.isBlank()) {
        errors.add(new ModelValidationError(model, elementId,
            ValidationMessages.get("validation.formLabelExpression.blank", owner), Severity.ERROR.name()));
        continue;
      }
      String syntaxError = ExpressionLanguageSyntaxChecker.validate(expression);
      if (syntaxError != null) {
        errors.add(new ModelValidationError(model, elementId,
            ValidationMessages.get("validation.formLabelExpression.invalid", owner, syntaxError), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
