package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingComponent;
import de.a12.studio.models.formmodel.BindingComponentType;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.form.FormBindingElements.BindingHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link Binding}/{@link BindingRepeat}'s {@code mainComponent}/{@code editModalComponent}, once a {@link
 * BindingComponentType} is set, requires {@code selectedItemsOverview} always, and {@code
 * availableItemsOverview} unless the type is {@link BindingComponentType#TABLE_LIST} (whose rows come from the
 * bound relationship directly) - mirrors SME's {@code availableItemIsRequired}/{@code selectedItemIsRequired}
 * rules in {@code I_BindingComponent.json}.
 */
public final class FormBindingComponentRequiredFieldsValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (BindingHolder holder : FormBindingElements.findBindingContents(formModel)) {
      if (holder.content() == null || holder.content().getDetails() == null) {
        continue;
      }
      validateComponent(model, holder.elementId(), holder.content().getDetails().getMainComponent(), errors);
      validateComponent(model, holder.elementId(), holder.content().getDetails().getEditModalComponent(), errors);
    }
    return errors;
  }

  private void validateComponent(A12Model<?> model, String elementId, BindingComponent component, List<ModelValidationError> errors) {
    if (component == null || component.getName() == null) {
      return;
    }
    boolean availableItemsOverviewSet = component.getModelsSME() != null
        && component.getModelsSME().getAvailableItemsOverview() != null
        && !component.getModelsSME().getAvailableItemsOverview().isBlank();
    boolean selectedItemsOverviewSet = component.getModelsSME() != null
        && component.getModelsSME().getSelectedItemsOverview() != null
        && !component.getModelsSME().getSelectedItemsOverview().isBlank();

    if (component.getName() != BindingComponentType.TABLE_LIST && !availableItemsOverviewSet) {
      errors.add(new ModelValidationError(model, elementId,
          ValidationMessages.get("validation.formBindingComponentRequiredFields.availableItemsOverviewMissing"), Severity.ERROR.name()));
    }
    if (!selectedItemsOverviewSet) {
      errors.add(new ModelValidationError(model, elementId,
          ValidationMessages.get("validation.formBindingComponentRequiredFields.selectedItemsOverviewMissing"), Severity.ERROR.name()));
    }
  }
}
