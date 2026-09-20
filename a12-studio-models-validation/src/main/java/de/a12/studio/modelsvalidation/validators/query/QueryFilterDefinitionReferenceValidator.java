package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Semantic counterpart of {@link QueryFilterDefinitionSyntaxValidator}: the field paths ({@code [/Path/To/Field]})
 * and relationship/role references inside {@code content.filterDefinition} - resolved against the query's target
 * Document Model - and inside every relationship hop's own {@code filterDefinition} (recursively), resolved
 * against the Document Model that hop's role plays, must exist. The rules and their SME origin are described on
 * {@link QueryFilterReferenceChecker}. A filter that is not syntactically valid is left to the syntax validator.
 */
public final class QueryFilterDefinitionReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = QueryFilterDefinitionSyntaxValidator.ELEMENT_ID;
  public static final String LINK_ELEMENT_ID = QueryFilterDefinitionSyntaxValidator.LINK_ELEMENT_ID;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null) {
      return List.of();
    }
    QueryFilterReferenceChecker.Models models = QueryFilterReferenceChecker.Models.of(context);
    QueryFilterReferenceChecker checker = new QueryFilterReferenceChecker(models);
    List<ModelValidationError> errors = new ArrayList<>();

    report(model, ELEMENT_ID, checker.check(queryModel.getContent().getFilterDefinition(),
        QueryElementResolution.targetDocumentModel(queryModel, context)), errors);
    for (QueryLink link : queryModel.getContent().getLinks()) {
      validateLink(model, link, models, checker, errors);
    }
    return errors;
  }

  private void validateLink(A12Model<?> model, QueryLink link, QueryFilterReferenceChecker.Models models,
      QueryFilterReferenceChecker checker, List<ModelValidationError> errors) {
    DocumentModel linkedDocumentModel = models.roleDocumentModel(link.getRelationshipModel(), link.getTargetRole());
    report(model, LINK_ELEMENT_ID, checker.check(link.getFilterDefinition(), linkedDocumentModel), errors);
    for (QueryLink nested : link.getLinks()) {
      validateLink(model, nested, models, checker, errors);
    }
  }

  private static void report(A12Model<?> model, String elementId, List<String> messages, List<ModelValidationError> errors) {
    for (String message : messages) {
      errors.add(new ModelValidationError(model, elementId, message, Severity.ERROR.name()));
    }
  }
}
