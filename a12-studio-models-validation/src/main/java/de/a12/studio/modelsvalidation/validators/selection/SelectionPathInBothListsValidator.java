package de.a12.studio.modelsvalidation.validators.selection;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionCategory;
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
 * Ports SME's {@code SELECTED_IN_UNSELECTED} rule ({@code DomainSelectionSpecification.json}): within one
 * Data/Computation/Validation section, the same path specification must not appear in both its
 * {@code Selected} and its {@code Unselected} list - the section's {@code Default} already makes one of the
 * two lists redundant for that path.
 */
public final class SelectionPathInBothListsValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof SelectionModel selectionModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (SelectionCategories.Entry category : SelectionCategories.ALL) {
      checkCategory(model, selectionModel, category, errors);
    }
    return errors;
  }

  private static void checkCategory(A12Model<?> model, SelectionModel selectionModel, SelectionCategories.Entry category,
      List<ModelValidationError> errors) {
    SelectionCategory selectionCategory = category.get(selectionModel);
    List<PathSpecification> selected = selectionCategory.getSelected();
    List<PathSpecification> unselected = selectionCategory.getUnselected();
    if (selected == null || unselected == null) {
      return;
    }
    Set<String> unselectedPaths = new HashSet<>();
    for (PathSpecification path : unselected) {
      if (path.getPath() != null && !path.getPath().isBlank()) {
        unselectedPaths.add(path.getPath());
      }
    }
    for (int index = 0; index < selected.size(); index++) {
      String path = selected.get(index).getPath();
      if (path != null && unselectedPaths.contains(path)) {
        errors.add(new ModelValidationError(model, category.elementIdPrefix() + "/Selected/" + index,
            ValidationMessages.get("validation.selectionPathInBothLists", path), Severity.ERROR.name()));
      }
    }
  }
}
