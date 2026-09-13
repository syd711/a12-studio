package de.a12.studio.modelsvalidation.validators.selection;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ports SME's {@code SELECTED_DUPLICATE}/{@code UNSELECTED_DUPLICATE} rules ({@code
 * DomainSelectionSpecification.json}): within one Data/Computation/Validation section, the same path
 * specification must not be repeated in either its {@code Selected} or its {@code Unselected} list.
 * Reports every occurrence after the first, matching SME's {@code RepetitionNotUnique} semantics.
 */
public final class SelectionDuplicatePathValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof SelectionModel selectionModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (SelectionCategories.Entry category : SelectionCategories.ALL) {
      checkList(model, category, "Selected", "validation.selectionDuplicatePath",
          category.get(selectionModel).getSelected(), errors);
      checkList(model, category, "Unselected", "validation.selectionDuplicatePath",
          category.get(selectionModel).getUnselected(), errors);
    }
    return errors;
  }

  private static void checkList(A12Model<?> model, SelectionCategories.Entry category, String listName,
      String messageKey, List<PathSpecification> paths, List<ModelValidationError> errors) {
    if (paths == null) {
      return;
    }
    Set<String> seen = new HashSet<>();
    for (int index = 0; index < paths.size(); index++) {
      String path = paths.get(index).getPath();
      if (path == null || path.isBlank()) {
        continue;
      }
      if (!seen.add(path)) {
        errors.add(new ModelValidationError(model, category.elementIdPrefix() + "/" + listName + "/" + index,
            ValidationMessages.get(messageKey, path), Severity.ERROR.name()));
      }
    }
  }
}
