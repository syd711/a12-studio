package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * A sort entry with no relationship hop ({@code relationshipModel == null}, i.e. sorting directly on the target
 * Document Model - see {@link QuerySort}) must reference a real field path on that model. A sort entry that does
 * traverse a relationship first is skipped here: resolving the hop's own target Document Model (via the
 * Relationship Model's role, see {@link QueryRelationshipTraversalValidator}) to validate the field against
 * would need more infrastructure than this pass adds - the traversal itself is still validated, just not yet the
 * field beyond it.
 */
public final class QuerySortFieldReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/sort/sortBy/field";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null
        || queryModel.getContent().getSort() == null) {
      return List.of();
    }
    DocumentModel targetDocumentModel = QueryElementResolution.targetDocumentModel(queryModel, context);
    if (targetDocumentModel == null || targetDocumentModel.getContent() == null
        || targetDocumentModel.getContent().getModelRoot() == null) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(targetDocumentModel, context.otherDocumentModels());
    List<ModelValidationError> errors = new ArrayList<>();
    for (QuerySort sort : queryModel.getContent().getSort()) {
      if (sort.getRelationshipModel() != null || sort.getSortBy() == null) {
        continue;
      }
      String field = sort.getSortBy().getField();
      if (field != null && QueryElementResolution.resolveByPath(index, field) == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.common.fieldReferenceMissing", field), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
