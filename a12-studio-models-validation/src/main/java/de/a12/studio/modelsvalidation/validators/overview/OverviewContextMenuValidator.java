package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.ActionGroup;
import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.ContextMenu;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A named Context Menu group must have at least one action with an event (SME: {@code groupMustHaveAction},
 * {@code NoGroupFilled(actions{@literal *}/event) and FieldFilled(name)}, "Group $name.value$ in context menu
 * should have at least one action.").
 */
public final class OverviewContextMenuValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/contextMenu";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    ContextMenu contextMenu = overviewModel.getContent().getContextMenu();
    if (contextMenu == null || contextMenu.getGroups().isEmpty()) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (ActionGroup group : contextMenu.getGroups()) {
      if (group.getName() == null || group.getName().isBlank()) {
        continue;
      }
      boolean hasAction = group.getActions().stream().map(Button::getEvent).anyMatch(event -> event != null && !event.isBlank());
      if (!hasAction) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.overviewContextMenu.groupWithoutAction", group.getName()), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
