package de.a12.studio.modelsvalidation.validators.composeddocument;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModel;
import de.a12.studio.models.composeddocumentmodel.ComposedDocumentModelResolver;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;
import java.util.Optional;

/**
 * A {@link ComposedDocumentModel}'s {@code cdm.queryRoot} annotation must be set and must resolve to an
 * existing {@link de.a12.studio.models.documentmodel.DocumentModel} in the workspace - see the BA docs' "The
 * required Annotation has the name 'cdm.queryRoot' and its value is the name of the root Document Model."
 */
public final class CdmQueryRootReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/cdm.queryRoot";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ComposedDocumentModel composedDocumentModel)) {
      return List.of();
    }
    Optional<String> queryRootId = ComposedDocumentModelResolver.getQueryRootId(composedDocumentModel);
    if (queryRootId.isEmpty() || queryRootId.get().isBlank()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.cdmQueryRootReference.missing"), Severity.ERROR.name()));
    }
    if (context.findOtherDocumentModel(queryRootId.get()) == null) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.cdmQueryRootReference.notFound", queryRootId.get()), Severity.ERROR.name()));
    }
    return List.of();
  }
}
