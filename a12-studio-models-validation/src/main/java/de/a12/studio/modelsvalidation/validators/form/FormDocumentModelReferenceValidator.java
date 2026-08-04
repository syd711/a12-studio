package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A form model must reference at least one Document Model, and every referenced Document Model must
 * exist in the workspace (SME: "validation.the_document_model_reference_is_required_select_a_" /
 * "No valid referenced document model.").
 */
public final class FormDocumentModelReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/modelReferences";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel)) {
      return List.of();
    }
    List<ModelReference> documentReferences = model.getModelReferences() == null ? List.of()
        : model.getModelReferences().stream()
            .filter(reference -> reference.getModelType() == ModelType.DOCUMENT)
            .toList();
    if (documentReferences.isEmpty()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          "validation.the_document_model_reference_is_required_select_a_", Severity.ERROR.name()));
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (ModelReference reference : documentReferences) {
      if (context.findOtherDocumentModel(reference.getReference()) == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.no_valid_referenced_document_model"" + reference.getReference() + "\" does not exist in the workspace.",
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
