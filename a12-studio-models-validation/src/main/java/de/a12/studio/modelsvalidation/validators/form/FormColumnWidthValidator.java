package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A repeat overview column's width is a number of at least {@value #MIN_WIDTH} with at most one decimal place
 * (SME's {@code NumberType} constraints on {@code I_RepeatOverviewColumnBase.json}'s width: 1.0 is about 150px,
 * steps of 0.1). An absent width is fine, the default is 1.0.
 */
public final class FormColumnWidthValidator implements ModelValidator {

  public static final double MIN_WIDTH = 0.3;

  private static final double EPSILON = 1e-9;

  /** Whether {@code width} is a valid column width. */
  public static boolean isValid(double width) {
    return width >= MIN_WIDTH - EPSILON && Math.abs(width * 10 - Math.rint(width * 10)) < EPSILON * 10;
  }

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (RepeatOverviewColumn column : FormModelWalker.find(formModel.getContent(), RepeatOverviewColumn.class)) {
      Double width = column.getWidth();
      if (width != null && !isValid(width)) {
        errors.add(new ModelValidationError(model, column.getId(),
            ValidationMessages.get("validation.columnWidth.invalid", width, MIN_WIDTH), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
