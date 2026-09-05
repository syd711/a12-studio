package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.ql.QueryLanguageEmitter;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * If set, {@code content.filterDefinition} must be syntactically valid Query Language (see
 * {@link QueryLanguageEmitter} and docs/sme-reference-comparison.md "Query Model" section) - previously
 * unchecked, since a12-studio had no grammar/parser for it at all until this pass. The Studio's own filter
 * dialog already surfaces this live via {@code RichtextEditorController}'s validator hook; this defends
 * hand-edited JSON the same way the other validators here do.
 */
public final class QueryFilterDefinitionSyntaxValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/filterDefinition";

  private final QueryLanguageEmitter emitter = new QueryLanguageEmitter();

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null) {
      return List.of();
    }
    String filterDefinition = queryModel.getContent().getFilterDefinition();
    if (filterDefinition == null || filterDefinition.isBlank()) {
      return List.of();
    }
    try {
      emitter.emit(filterDefinition);
      return List.of();
    } catch (QueryLanguageException e) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.queryFilterDefinition.invalidSyntax", e.getMessage()), Severity.ERROR.name()));
    }
  }
}
