package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.AbstractRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The column a repeat is initially sorted by ({@link AbstractRepeat#getInitialSorting()}) must be sortable (SME's
 * {@code columnMustBeSortableWhenSetAsInitialSorting} rule with its {@code SortableColumnCustomCondition}, which
 * fails when the column's {@code sortable} is not set). Reported on the column, like SME does on its
 * {@code sortable} field. A reference to a column that does not exist is not this rule's business.
 */
public final class FormInitialSortingColumnSortableValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (AbstractRepeat repeat : FormRepeats.collect(formModel.getContent())) {
      String initialSorting = repeat.getInitialSorting();
      if (initialSorting == null || initialSorting.isBlank()) {
        continue;
      }
      for (RepeatOverviewColumn column : repeat.getRepeatOverviewColumn()) {
        if (initialSorting.equals(column.getId()) && !Boolean.TRUE.equals(column.getSortable())) {
          errors.add(new ModelValidationError(model, column.getId(),
              ValidationMessages.get("validation.initialSortingColumn.notSortable", column.getId(),
                  repeat.getName() != null && !repeat.getName().isBlank() ? repeat.getName() : repeat.getId()),
              Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }
}
