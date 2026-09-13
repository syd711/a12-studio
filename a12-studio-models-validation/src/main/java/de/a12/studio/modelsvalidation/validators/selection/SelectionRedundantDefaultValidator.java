package de.a12.studio.modelsvalidation.validators.selection;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionCategory;
import de.a12.studio.models.selectionmodel.SelectionDefault;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Ports SME's {@code DEFAULT_SELECTED_BUT_NO_UNSELECTED}/{@code DEFAULT_UNSELECTED_BUT_NO_SELECTED} rules
 * ({@code DomainSelectionSpecification.json}): when a section's {@code Default} is {@code Selected}, listing
 * path specifications in {@code Selected} without ever using {@code Unselected} is redundant (every element
 * is already selected by default) - and symmetrically for a {@code Default} of {@code Unselected}.
 */
public final class SelectionRedundantDefaultValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof SelectionModel selectionModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (SelectionCategories.Entry category : SelectionCategories.ALL) {
      SelectionCategory selectionCategory = category.get(selectionModel);
      boolean selectedFilled = anyFilled(selectionCategory.getSelected());
      boolean unselectedFilled = anyFilled(selectionCategory.getUnselected());
      SelectionDefault defaultValue = selectionCategory.getDefaultValue();

      if (defaultValue == SelectionDefault.SELECTED && selectedFilled && !unselectedFilled) {
        errors.add(new ModelValidationError(model, category.elementIdPrefix() + "/Default",
            ValidationMessages.get("validation.selectionDefaultSelectedButNoUnselected"), Severity.ERROR.name()));
      }
      else if (defaultValue == SelectionDefault.UNSELECTED && unselectedFilled && !selectedFilled) {
        errors.add(new ModelValidationError(model, category.elementIdPrefix() + "/Default",
            ValidationMessages.get("validation.selectionDefaultUnselectedButNoSelected"), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static boolean anyFilled(List<PathSpecification> paths) {
    if (paths == null) {
      return false;
    }
    for (PathSpecification path : paths) {
      if (path.getPath() != null && !path.getPath().isBlank()) {
        return true;
      }
    }
    return false;
  }
}
