package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Every model referenced in the header's modelReferences must exist in the workspace (mirroring SME's
 * "The referenced ... is missing in the workspace" check on e.g. print models). Applies to every model
 * type that carries header references.
 */
public final class HeaderModelReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/modelReferences";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (model.getModelReferences() == null || context.otherModels() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (ModelReference reference : model.getModelReferences()) {
      if (reference.getReference() == null || reference.getReference().isBlank()) {
        continue;
      }
      if (context.findOtherModel(reference.getReference()) == null) {
        String type = reference.getModelType() != null ? reference.getModelType().getDisplayName() : "model";
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.the_referenced" + type + " \"" + reference.getReference() + "\" is missing in the workspace.",
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
