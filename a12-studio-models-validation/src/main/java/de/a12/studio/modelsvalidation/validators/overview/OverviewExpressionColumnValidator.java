package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.expressionlang.ExpressionLanguageSyntaxChecker;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * An expression column - identified, in the absence of a persisted {@code type} discriminant (SME itself
 * never writes one either, see {@code exportTransformations.ts}; it derives {@code type} on import from
 * whichever of {@code elementRef}/{@code expression} is present), as any column with a blank {@code
 * elementRef} - needs a Name and an Expression (SME: {@code expressionNameIsRequired}, {@code
 * expressionIsRequired}, "This field is required."), and the Expression must be syntactically valid Expression
 * language text (SME: {@code expressionMustBeValid}, the module's one kernel-backed check, {@code
 * fetchExpressionProblems} - only the syntax half is checked here, see Open Decision #1 in {@code TODO.md}).
 */
public final class OverviewExpressionColumnValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/columns/expression";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Column column : overviewModel.getContent().getColumns()) {
      if (column.getElementRef() != null && !column.getElementRef().isBlank()) {
        continue;
      }
      String columnName = column.getId();
      if (column.getName() == null || column.getName().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewExpressionColumn.nameRequired", columnName), Severity.ERROR.name()));
      }
      String expression = column.getExpression();
      if (expression == null || expression.isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewExpressionColumn.expressionRequired", columnName), Severity.ERROR.name()));
        continue;
      }
      String syntaxError = ExpressionLanguageSyntaxChecker.validate(expression);
      if (syntaxError != null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewExpressionColumn.invalidExpression", columnName, syntaxError), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
