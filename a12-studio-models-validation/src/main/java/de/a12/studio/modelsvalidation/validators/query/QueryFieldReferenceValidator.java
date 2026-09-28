package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewElementResolution;

import java.util.ArrayList;
import java.util.List;

/** Every path in {@code content.fields[]} (the "in result" projection) must resolve against the target Document
 * Model's element tree - a field renamed/removed elsewhere would otherwise silently drop out of the result instead
 * of surfacing as an error - and must resolve to a field with {@code indexed != false}, matching the same
 * indexed-only rule the filter/aggregation validators enforce for their own field references
 * ({@link QueryFilterReferenceChecker}, {@link QueryAggregationSupport}). */
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
      if (QueryElementResolution.isMetaPath(path)) {
        continue;
      }
      Element element = QueryElementResolution.resolveByPath(index, path);
      if (element == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.common.fieldReferenceMissing", path), Severity.ERROR.name()));
      } else if (OverviewElementResolution.isIndexedFalse(element)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.common.indexedAnnotationFalse", element.getName()), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
