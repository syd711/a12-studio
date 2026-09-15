package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.ql.QueryLanguageEmitter;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * If set, {@code content.filterDefinition} and every relationship-traversal hop's own {@code
 * filterDefinition} (recursively, including nested hops - see {@link QueryLink#getLinks()}, mirroring how
 * {@link QueryLinkValidator} recurses for {@code fields}/target-role) must be syntactically valid Query
 * Language (see {@link QueryLanguageEmitter} and docs/sme-reference-comparison.md "Query Model" section) -
 * previously unchecked, since a12-studio had no grammar/parser for it at all until this pass. The Studio's own
 * per-node "Filter Definition" panel ({@code QueryDocumentNodePanelController}) already surfaces this live via
 * {@code RuleEditorController}'s validator hook; this defends hand-edited JSON the same way the other
 * validators here do.
 */
public final class QueryFilterDefinitionSyntaxValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/filterDefinition";
  public static final String LINK_ELEMENT_ID = "content/links";

  private final QueryLanguageEmitter emitter = new QueryLanguageEmitter();

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    checkSyntax(model, queryModel.getContent().getFilterDefinition(), ELEMENT_ID, errors);
    for (QueryLink link : queryModel.getContent().getLinks()) {
      validateLink(model, link, errors);
    }
    return errors;
  }

  private void validateLink(A12Model<?> model, QueryLink link, List<ModelValidationError> errors) {
    checkSyntax(model, link.getFilterDefinition(), LINK_ELEMENT_ID, errors);
    for (QueryLink nested : link.getLinks()) {
      validateLink(model, nested, errors);
    }
  }

  private void checkSyntax(A12Model<?> model, String filterDefinition, String elementId, List<ModelValidationError> errors) {
    if (filterDefinition == null || filterDefinition.isBlank()) {
      return;
    }
    try {
      emitter.emit(filterDefinition);
    } catch (QueryLanguageException e) {
      errors.add(new ModelValidationError(model, elementId,
          ValidationMessages.get("validation.queryFilterDefinition.invalidSyntax", e.getMessage()), Severity.ERROR.name()));
    }
  }
}
