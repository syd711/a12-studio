package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * An overview model must reference exactly one Document Model (SME's "General Settings" - Document
 * Model is implicitly required). Whether the reference actually resolves in the workspace is checked
 * separately by the generic {@link de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator}.
 */
public final class OverviewDocumentModelRequiredValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/modelReferences";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    boolean hasDocumentModel = overviewModel.getModelReferences() != null
        && overviewModel.getModelReferences().stream()
            .anyMatch(reference -> ModelReference.PURPOSE_DOCUMENT_MODEL_FOR_OVERVIEW.equals(reference.getPurpose())
                && reference.getReference() != null && !reference.getReference().isBlank());
    if (hasDocumentModel) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID, "A Document Model is required.", Severity.ERROR.name()));
  }
}
