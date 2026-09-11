package de.a12.studio.modelsvalidation.validators.relationshipui;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipuimodel.DropDownSelectionComponent;
import de.a12.studio.models.relationshipuimodel.DualPaneSelectionComponent;
import de.a12.studio.models.relationshipuimodel.EditConfiguration;
import de.a12.studio.models.relationshipuimodel.RelationshipUiComponent;
import de.a12.studio.models.relationshipuimodel.RelationshipUiModel;
import de.a12.studio.models.relationshipuimodel.TableListComponent;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A component must be chosen, and each componentType has its own required model references (the fields real
 * fixtures never omit - see e.g. {@code DualPaneSelectionComponent}'s javadoc for which fields that is per
 * variant).
 */
public final class RelationshipUiComponentValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/component";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipUiModel relationshipUiModel)) {
      return List.of();
    }
    RelationshipUiComponent component = relationshipUiModel.getContent().getComponent();
    if (component == null) {
      return List.of(error(model, "validation.relationshipUiComponent.missing"));
    }

    List<ModelValidationError> errors = new ArrayList<>();
    if (component instanceof DualPaneSelectionComponent dualPane) {
      requireField(errors, model, dualPane.getAvailableItemsOverviewModel(), "validation.relationshipUiComponent.missingAvailableItems");
      requireField(errors, model, dualPane.getSelectedItemsOverviewModel(), "validation.relationshipUiComponent.missingSelectedItems");
    }
    else if (component instanceof TableListComponent tableList) {
      requireField(errors, model, tableList.getSelectedItemsOverviewModel(), "validation.relationshipUiComponent.missingSelectedItems");
      EditConfiguration editConfiguration = tableList.getEditConfiguration();
      if (editConfiguration != null) {
        requireField(errors, model, editConfiguration.getAvailableItemsOverviewModel(),
            "validation.relationshipUiComponent.editConfigurationMissingAvailableItems");
        requireField(errors, model, editConfiguration.getSelectedItemsOverviewModel(),
            "validation.relationshipUiComponent.editConfigurationMissingSelectedItems");
      }
    }
    else if (component instanceof DropDownSelectionComponent dropDown) {
      requireField(errors, model, dropDown.getAvailableItemsQueryModel(), "validation.relationshipUiComponent.missingAvailableItemsQuery");
      requireField(errors, model, dropDown.getSelectedItemQueryModel(), "validation.relationshipUiComponent.missingSelectedItemQuery");
      requireField(errors, model, dropDown.getElementRef(), "validation.relationshipUiComponent.missingElementRef");
    }
    return errors;
  }

  private void requireField(List<ModelValidationError> errors, A12Model<?> model, String value, String messageKey) {
    if (value == null || value.isBlank()) {
      errors.add(error(model, messageKey));
    }
  }

  private ModelValidationError error(A12Model<?> model, String messageKey) {
    return new ModelValidationError(model, ELEMENT_ID, ValidationMessages.get(messageKey), Severity.ERROR.name());
  }
}
