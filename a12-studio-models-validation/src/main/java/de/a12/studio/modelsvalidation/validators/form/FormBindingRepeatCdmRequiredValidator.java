package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.formmodel.BindingRepeat;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link BindingRepeat} only makes sense when the Form Model's bound Document Model is a {@link
 * ComposedDocumentModel} - a plain to-many relationship without CDM's precomputed schema has no repetition
 * count for the repeat to render against. Mirrors the *reason* SME's frontend custom conditions {@code
 * DescendantOfHeterogeneousRelationship}/{@code DescendantOfHeterogeneousToManyRelationship}/{@code
 * InvalidBindingRepeatRepetitionAndMultiplicity} gate on {@code ComposedDocumentModelApi.isComposedDocumentModel(...)}.
 * The repetition-vs-multiplicity check lives in {@link FormBindingRepeatMultiplicityValidator}; this validator only
 * checks the structural precondition the conditions share.
 */
public final class FormBindingRepeatCdmRequiredValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    boolean boundToComposedDocumentModel = context.findOtherDocumentModel(dataBindingDocumentModelId(formModel)) instanceof ComposedDocumentModel;
    if (boundToComposedDocumentModel) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (BindingRepeat bindingRepeat : FormModelWalker.find(formModel.getContent(), BindingRepeat.class)) {
      errors.add(new ModelValidationError(model, bindingRepeat.getId(),
          ValidationMessages.get("validation.formBindingRepeatCdmRequired.notComposedDocumentModel"), Severity.ERROR.name()));
    }
    return errors;
  }

  // Mirrors de.a12.studio.ui.editors.formmodel.FormModelEditorController#currentDocumentModelId - a12-studio-ui
  // isn't a dependency here, so the same header ModelReference lookup is duplicated rather than shared.
  static String dataBindingDocumentModelId(FormModel formModel) {
    if (formModel.getModelReferences() == null) {
      return null;
    }
    return formModel.getModelReferences().stream()
        .filter(reference -> reference.getModelType() == ModelType.DOCUMENT && ModelReference.PURPOSE_DATA_BINDING.equals(reference.getPurpose()))
        .map(ModelReference::getReference)
        .findFirst()
        .orElse(null);
  }
}
