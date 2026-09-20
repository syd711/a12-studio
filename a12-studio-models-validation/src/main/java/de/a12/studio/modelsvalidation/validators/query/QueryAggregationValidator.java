package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.querymodel.QueryAggregation;
import de.a12.studio.models.querymodel.QueryAggregationEntry;
import de.a12.studio.models.querymodel.QueryAggregationGroup;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.query.QueryAggregationSupport.Check;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates {@code content.aggregation} (see {@link QueryAggregation}) against what Data Services can execute and
 * against the target Document Model:
 * <ul>
 *   <li>the query must not traverse relationships and must use the {@code document} projection - an aggregation
 *       result holds generated documents, never roots that links could hang on (error);</li>
 *   <li>every group and aggregation entry needs a field that resolves in the target Document Model, is not annotated
 *       {@code indexed = false} and is not repeatable ({@link QueryAggregationSupport#check});</li>
 *   <li>every aggregation entry needs one of the five functions, and that function must be available for the field's
 *       type ({@link QueryAggregationSupport#isFunctionAvailable});</li>
 *   <li>warnings: no aggregation entry at all (the query then only groups), and a sort, which Data Services ignores in
 *       aggregation mode.</li>
 * </ul>
 * A target Document Model that cannot be resolved skips the field checks - {@link
 * QueryTargetDocumentModelRequiredValidator} reports that itself.
 */
public final class QueryAggregationValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/aggregation";
  public static final String GROUP_ELEMENT_ID = ELEMENT_ID + "/group/field";
  public static final String AGGREGATIONS_ELEMENT_ID = ELEMENT_ID + "/aggregations";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null
        || queryModel.getContent().getAggregation() == null) {
      return List.of();
    }
    QueryAggregation aggregation = queryModel.getContent().getAggregation();
    List<ModelValidationError> errors = new ArrayList<>();

    if (!queryModel.getContent().getLinks().isEmpty()) {
      errors.add(error(model, ELEMENT_ID, Severity.ERROR, "validation.queryAggregation.withLinks"));
    }
    String projection = queryModel.getContent().getProjectionName();
    if (projection != null && !projection.isBlank() && !"document".equals(projection)) {
      errors.add(error(model, ELEMENT_ID, Severity.ERROR, "validation.queryAggregation.projectionNotDocument", projection));
    }
    if (!queryModel.getContent().getSort().isEmpty()) {
      errors.add(error(model, ELEMENT_ID, Severity.WARNING, "validation.queryAggregation.sortIgnored"));
    }
    if (aggregation.getAggregations().isEmpty()) {
      errors.add(error(model, ELEMENT_ID, Severity.WARNING, "validation.queryAggregation.noAggregations"));
    }

    DocumentModel target = QueryElementResolution.targetDocumentModel(queryModel, context);
    ElementIndex index = target == null || target.getContent() == null || target.getContent().getModelRoot() == null
        ? null : new ElementIndex(target, context.otherDocumentModels());

    for (QueryAggregationGroup group : aggregation.getGroup()) {
      if (isBlank(group.getField())) {
        errors.add(error(model, GROUP_ELEMENT_ID, Severity.ERROR, "validation.queryAggregation.groupFieldRequired"));
      }
      else {
        checkField(model, GROUP_ELEMENT_ID, group.getField(), index, target, errors);
      }
    }

    for (QueryAggregationEntry entry : aggregation.getAggregations()) {
      String function = entry.getFunction();
      boolean knownFunction = false;
      if (isBlank(function)) {
        errors.add(error(model, AGGREGATIONS_ELEMENT_ID, Severity.ERROR, "validation.queryAggregation.functionRequired",
            isBlank(entry.getField()) ? "" : entry.getField()));
      }
      else if (!QueryAggregationEntry.FUNCTIONS.contains(function)) {
        errors.add(error(model, AGGREGATIONS_ELEMENT_ID, Severity.ERROR, "validation.queryAggregation.functionUnknown", function));
      }
      else {
        knownFunction = true;
      }

      if (isBlank(entry.getField())) {
        errors.add(error(model, AGGREGATIONS_ELEMENT_ID, Severity.ERROR, "validation.queryAggregation.entryFieldRequired",
            isBlank(function) ? "" : function));
        continue;
      }
      Check check = checkField(model, AGGREGATIONS_ELEMENT_ID, entry.getField(), index, target, errors);
      if (knownFunction && check != null && check.ok()) {
        FieldType type = QueryAggregationSupport.effectiveType(index, check.element());
        if (!QueryAggregationSupport.isFunctionAvailable(function, type)) {
          errors.add(error(model, AGGREGATIONS_ELEMENT_ID, Severity.ERROR, "validation.queryAggregation.functionTypeMismatch",
              function, entry.getField(), QueryAggregationSupport.typeLabel(type), QueryAggregationSupport.availableFor(function)));
        }
      }
    }
    return errors;
  }

  /** Reports why {@code path} cannot be used; returns the check, or null when it was not checked (meta path, or no
   * resolvable target Document Model). */
  private static Check checkField(A12Model<?> model, String elementId, String path, ElementIndex index, DocumentModel target,
      List<ModelValidationError> errors) {
    if (index == null || QueryElementResolution.isMetaPath(path)) {
      return null;
    }
    Check check = QueryAggregationSupport.check(index, path);
    if (check.problem() != null) {
      String targetId = target.getId();
      errors.add(switch (check.problem()) {
        case UNKNOWN -> error(model, elementId, Severity.ERROR, "validation.queryAggregation.fieldUnknown", path, targetId);
        case NOT_A_FIELD -> error(model, elementId, Severity.ERROR, "validation.queryAggregation.fieldNotAField", path, targetId);
        case NOT_INDEXED -> error(model, elementId, Severity.ERROR, "validation.queryAggregation.fieldNotIndexed", path, targetId);
        case REPEATABLE -> error(model, elementId, Severity.ERROR, "validation.queryAggregation.fieldRepeatable", path,
            check.repeatableGroup());
      });
    }
    return check;
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, Severity severity, String key, Object... args) {
    return new ModelValidationError(model, elementId, ValidationMessages.get(key, args), severity.name());
  }

  private static boolean isBlank(String value) {
    return value == null || value.isBlank();
  }
}
