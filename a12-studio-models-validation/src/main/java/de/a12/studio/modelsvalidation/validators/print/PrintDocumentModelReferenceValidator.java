package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Document models referenced by a print model must not contain dots in their name — the print engine
 * cannot address them otherwise (print modeling docs: "The referenced Document Model must not contain
 * dots in its name"). Existence of the reference itself is covered by HeaderModelReferenceValidator.
 */
public final class PrintDocumentModelReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/modelReferences";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof PrintModel) || model.getModelReferences() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (ModelReference reference : model.getModelReferences()) {
      if (reference.getModelType() == ModelType.DOCUMENT
          && reference.getReference() != null && reference.getReference().contains(".")) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.printDocumentModelReference.dotsInName", reference.getReference()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
