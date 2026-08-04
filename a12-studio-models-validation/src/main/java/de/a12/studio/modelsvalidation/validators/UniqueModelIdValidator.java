package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.List;

/**
 * Model names must be unique within the workspace (case-insensitively, matching SME's
 * ModelHasUniqueId custom condition). Applies to every model type.
 */
public final class UniqueModelIdValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/id";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (model.getId() == null || context.otherModels() == null) {
      return List.of();
    }
    boolean duplicate = context.otherModels().stream()
        .anyMatch(other -> other.getId() != null && other.getId().equalsIgnoreCase(model.getId()));
    if (!duplicate) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        "validation.the_chosen_name_already_exists_in_the_workspace_pl",
        Severity.ERROR.name()));
  }
}
