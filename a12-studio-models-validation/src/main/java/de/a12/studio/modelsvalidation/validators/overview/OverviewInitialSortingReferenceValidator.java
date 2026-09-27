package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.overviewmodel.ColumnRef;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Every {@code content.configuration.initialSorting} entry must reference a column that still exists, so
 * deleting a column used for sorting surfaces as a validation error instead of silently leaving a dangling
 * reference (SME instead prompts a refactoring dialog to remove or reassign it; not reimplemented here). Entries
 * must also be unique (SME: {@code initialSortingIDRefNotUnique}, {@code RepetitionNotUnique(initialSorting/idref)},
 * "The values of the field must be unique."). SME's meta model has no rule requiring a referenced column to be
 * Sortable - that's enforced only by the Initial Sorting picker offering sortable reference columns alone (see
 * {@link de.a12.studio.ui.editors.overviewmodel.OverviewSortingPanelController}), not a validation error.
 */
public final class OverviewInitialSortingReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/initialSorting";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    OverviewConfiguration configuration = overviewModel.getContent().getConfiguration();
    if (configuration == null || configuration.getInitialSorting().isEmpty()) {
      return List.of();
    }

    Set<String> columnIds = overviewModel.getContent().getColumns().stream()
        .map(Column::getId)
        .collect(Collectors.toSet());

    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    for (ColumnRef columnRef : configuration.getInitialSorting()) {
      String idref = columnRef.getIdref();
      if (idref == null || idref.isBlank()) {
        continue;
      }
      if (!columnIds.contains(idref)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewInitialSortingReference.missing", idref), Severity.ERROR.name()));
      }
      else if (!seen.add(idref)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewInitialSortingReference.duplicate", idref), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
