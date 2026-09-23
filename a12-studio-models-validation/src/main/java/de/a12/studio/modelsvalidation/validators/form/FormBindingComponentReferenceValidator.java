package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.formmodel.Binding;
import de.a12.studio.models.formmodel.BindingComponent;
import de.a12.studio.models.formmodel.BindingComponentModelsSme;
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
 * A {@link Binding}/{@link BindingRepeat}'s {@code mainComponent}/{@code editModalComponent} {@code modelsSME}
 * references ({@code availableItemsOverview}, {@code selectedItemsOverview}: Overview Models; {@code
 * additionalFieldsForm}: a Form Model) must resolve to a real model of the right type when set - mirrors SME's
 * {@code availableItemsOverviewMustHaveAValidReference}/{@code selectedItemsOverviewMustHaveAValidReference}/
 * {@code additionalFieldsFormMustHaveAValidReference} rules in {@code I_BindingComponent.json}.
 */
public final class FormBindingComponentReferenceValidator implements ModelValidator {

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
      validateComponent(model, context, holder.elementId(), holder.content().getDetails().getMainComponent(), errors);
      validateComponent(model, context, holder.elementId(), holder.content().getDetails().getEditModalComponent(), errors);
    }
    return errors;
  }

  private void validateComponent(A12Model<?> model, ValidationContext context, String elementId, BindingComponent component,
      List<ModelValidationError> errors) {
    if (component == null || component.getModelsSME() == null) {
      return;
    }
    BindingComponentModelsSme modelsSme = component.getModelsSME();
    validateReference(model, context, elementId, ModelType.OVERVIEW, modelsSme.getAvailableItemsOverview(),
        "validation.formBindingComponentReference.availableItemsOverviewNotFound", errors);
    validateReference(model, context, elementId, ModelType.OVERVIEW, modelsSme.getSelectedItemsOverview(),
        "validation.formBindingComponentReference.selectedItemsOverviewNotFound", errors);
    validateReference(model, context, elementId, ModelType.FORM, modelsSme.getAdditionalFieldsForm(),
        "validation.formBindingComponentReference.additionalFieldsFormNotFound", errors);
  }

  private void validateReference(A12Model<?> model, ValidationContext context, String elementId, ModelType expectedType,
      String referencedId, String messageKey, List<ModelValidationError> errors) {
    if (referencedId == null || referencedId.isBlank()) {
      return;
    }
    A12Model<?> referenced = context.findOtherModel(referencedId);
    if (referenced == null || referenced.getModelType() != expectedType) {
      errors.add(new ModelValidationError(model, elementId, ValidationMessages.get(messageKey, referencedId), Severity.ERROR.name()));
    }
  }
}
