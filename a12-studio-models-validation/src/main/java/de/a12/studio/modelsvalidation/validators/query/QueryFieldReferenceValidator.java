package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/** Every path in {@code content.fields[]} (the "in result" projection) must resolve against the target Document
 * Model's element tree - currently unchecked, so a field renamed/removed elsewhere silently drops out of the
 * result instead of surfacing as an error. */
public final class QueryFieldReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/fields";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null
        || queryModel.getContent().getFields() == null) {
      return List.of();
    }
    DocumentModel targetDocumentModel = QueryElementResolution.targetDocumentModel(queryModel, context);
    if (targetDocumentModel == null || targetDocumentModel.getContent() == null
        || targetDocumentModel.getContent().getModelRoot() == null) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(targetDocumentModel, context.otherDocumentModels());
    List<ModelValidationError> errors = new ArrayList<>();
    for (String path : queryModel.getContent().getFields()) {
      if (QueryElementResolution.resolveByPath(index, path) == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.common.fieldReferenceMissing", path), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
