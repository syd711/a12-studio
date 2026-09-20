package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.querymodel.QueryAggregation;
import de.a12.studio.models.querymodel.QueryAggregationEntry;
import de.a12.studio.models.querymodel.QueryAggregationGroup;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.ql.QLLexer;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.FieldReference;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.HasCall;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.Reference;
import de.a12.studio.models.querymodel.ql.QueryLanguageReferences.Replacement;
import de.a12.studio.models.querymodel.operator.AndOperator;
import de.a12.studio.models.querymodel.operator.DateFragmentRangeOperator;
import de.a12.studio.models.querymodel.operator.DateRangeOperator;
import de.a12.studio.models.querymodel.operator.DoubleRangeOperator;
import de.a12.studio.models.querymodel.operator.ExactMatchOperator;
import de.a12.studio.models.querymodel.operator.HasOperator;
import de.a12.studio.models.querymodel.operator.NotOperator;
import de.a12.studio.models.querymodel.operator.Operator;
import de.a12.studio.models.querymodel.operator.OrOperator;
import de.a12.studio.models.querymodel.operator.SimpleSearchOperator;
import de.a12.studio.models.querymodel.operator.UndefinedMatchOperator;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.PathRewriter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * The edits one Query Model needs when an element of the Document Model {@code changedId} is renamed or moved. A
 * query holds field paths against <em>several</em> Document Models, and only those that belong to the changed one
 * may be rewritten - the scoping is SME's ({@code binder.ts}/{@code resolver.ts}), the same one {@code
 * QueryFilterReferenceChecker} resolves against:
 * <ul>
 *   <li>the root's {@code fields}, {@code aggregation} (group and aggregated fields), {@code constraint} and
 *       {@code filterDefinition} belong to the query's {@code targetDocumentModel};</li>
 *   <li>a relationship hop's ({@link QueryLink}) {@code fields}, {@code constraint}, {@code filterDefinition} and a
 *       {@code sort} entry that goes through a relationship belong to the Document Model the hop's {@code
 *       targetRole} plays in its Relationship Model; its {@code linkDocumentFields} to that relationship's link
 *       Document Model. Hops nest, each with its own scope;</li>
 *   <li>a {@code has} operator / {@code Has(...)} call switches scope for its nested constraints: {@code
 *       constraint} to the role's Document Model, {@code linkDocumentConstraint}/the link constraint to the link
 *       Document Model.</li>
 * </ul>
 * A scope that cannot be determined (no such relationship, role, or Document Model) is left alone - nothing is
 * guessed. A filter that isn't valid Query Language has no parse tree to scope by: its field references follow
 * only when it contains no {@code Has(...)} call (see {@link #filter}).
 */
final class QueryReferenceRefactoring {

  private final String changedId;
  private final PathRewriter rewriter;
  private final Map<String, RelationshipModel> relationshipModels = new HashMap<>();
  private final List<Edit> edits;

  private QueryReferenceRefactoring(String changedId, PathRewriter rewriter,
      Collection<? extends A12Model<?>> projectModels, List<Edit> edits) {
    this.changedId = changedId;
    this.rewriter = rewriter;
    this.edits = edits;
    for (A12Model<?> model : projectModels) {
      if (model instanceof RelationshipModel relationshipModel && relationshipModel.getId() != null) {
        relationshipModels.put(relationshipModel.getId(), relationshipModel);
      }
    }
  }

  /** Appends the edits {@code query} needs to {@code edits}; {@code projectModels} supplies the relationship models. */
  static void collect(QueryModel query, String changedId, PathRewriter rewriter,
      Collection<? extends A12Model<?>> projectModels, List<Edit> edits) {
    if (query.getContent() != null) {
      new QueryReferenceRefactoring(changedId, rewriter, projectModels, edits).root(query.getContent());
    }
  }

  private void root(QueryModelContent content) {
    String targetId = content.getTargetDocumentModel();
    if (changedId.equals(targetId)) {
      listSites(content.getFields());
    }
    for (QuerySort sort : content.getSort()) {
      if (sort.getSortBy() != null) {
        String scope = sort.getRelationshipModel() == null
            ? targetId : roleModelId(sort.getRelationshipModel(), sort.getTargetRole());
        if (changedId.equals(scope)) {
          pathSite(sort.getSortBy()::getField, sort.getSortBy()::setField);
        }
      }
    }
    aggregation(content.getAggregation(), targetId);
    operator(content.getConstraint(), targetId);
    filter(content.getFilterDefinition(), targetId, content::setFilterDefinition);
    links(content.getLinks());
  }

  /** {@code aggregation.group[].field} and {@code aggregation.aggregations[].field} are always fields of the target
   * Document Model - Data Services does not aggregate over links. */
  private void aggregation(QueryAggregation aggregation, String targetId) {
    if (aggregation == null || !changedId.equals(targetId)) {
      return;
    }
    for (QueryAggregationGroup group : aggregation.getGroup()) {
      pathSite(group::getField, group::setField);
    }
    for (QueryAggregationEntry entry : aggregation.getAggregations()) {
      pathSite(entry::getField, entry::setField);
    }
  }

  private void links(List<QueryLink> links) {
    for (QueryLink link : links) {
      String scope = roleModelId(link.getRelationshipModel(), link.getTargetRole());
      if (changedId.equals(scope)) {
        listSites(link.getFields());
      }
      if (changedId.equals(linkModelId(link.getRelationshipModel()))) {
        listSites(link.getLinkDocumentFields());
      }
      operator(link.getConstraint(), scope);
      filter(link.getFilterDefinition(), scope, link::setFilterDefinition);
      links(link.getLinks());
    }
  }

  // ---- the structured constraint ----------------------------------------------------------------------------

  /** Field paths of {@code operator} and everything below it that are evaluated in {@code scopeId}. */
  private void operator(Operator operator, String scopeId) {
    boolean inScope = changedId.equals(scopeId);
    if (operator instanceof AndOperator and) {
      if (and.getOperands() != null) {
        and.getOperands().forEach(operand -> operator(operand, scopeId));
      }
    }
    else if (operator instanceof OrOperator or) {
      if (or.getOperands() != null) {
        or.getOperands().forEach(operand -> operator(operand, scopeId));
      }
    }
    else if (operator instanceof NotOperator not) {
      operator(not.getOperand(), scopeId);
    }
    else if (operator instanceof HasOperator has) {
      operator(has.getConstraint(), roleModelId(has.getRelationshipModel(), has.getTargetRole()));
      operator(has.getLinkDocumentConstraint(), linkModelId(has.getRelationshipModel()));
    }
    else if (!inScope) {
      return;
    }
    else if (operator instanceof ExactMatchOperator exact) {
      pathSite(exact::getField, exact::setField);
    }
    else if (operator instanceof UndefinedMatchOperator undefined) {
      pathSite(undefined::getField, undefined::setField);
    }
    else if (operator instanceof DoubleRangeOperator range) {
      pathSite(range::getField, range::setField);
    }
    else if (operator instanceof DateRangeOperator range) {
      pathSite(range::getField, range::setField);
    }
    else if (operator instanceof DateFragmentRangeOperator range) {
      pathSite(range::getField, range::setField);
    }
    else if (operator instanceof SimpleSearchOperator search) {
      listSites(search.getFields());
    }
  }

  // ---- the Query Language text ------------------------------------------------------------------------------

  /**
   * Every {@code [/Path/To/Field]} of {@code text} that is evaluated in {@code scopeId}, rewritten. Found through
   * the parse tree, so a path-looking string literal is never touched and a {@code Has} constraint is attributed to
   * the role's (or link) Document Model rather than the enclosing scope's.
   */
  private void filter(String text, String scopeId, Consumer<String> setter) {
    if (text == null || text.isBlank()) {
      return;
    }
    List<Reference> references;
    try {
      references = QueryLanguageReferences.extract(text);
    }
    catch (QueryLanguageException e) {
      // Half-typed or otherwise invalid text has no parse tree to scope by; fall back to the tokens, but only where
      // that can't misattribute a path (no Has(...), so the whole text is one scope).
      List<Replacement> lexical = changedId.equals(scopeId) ? lexicalReplacements(text) : List.of();
      if (!lexical.isEmpty()) {
        edits.add(new Edit(setter, text, QueryLanguageReferences.replace(text, lexical)));
      }
      return;
    }
    List<Replacement> replacements = new ArrayList<>();
    replacements(references, scopeId, replacements);
    if (!replacements.isEmpty()) {
      edits.add(new Edit(setter, text, QueryLanguageReferences.replace(text, replacements)));
    }
  }

  /** The field references of {@code text}, found by the lexer alone; none if it contains a {@code Has} call. */
  private List<Replacement> lexicalReplacements(String text) {
    List<? extends Token> tokens = new QLLexer(CharStreams.fromString(text)).getAllTokens();
    List<Replacement> replacements = new ArrayList<>();
    for (Token token : tokens) {
      if (token.getType() == QLLexer.I_CALLEE && "Has".equals(token.getText())) {
        return List.of();
      }
      if (token.getType() == QLLexer.I_FIELD) {
        String reference = token.getText();
        String path = reference.substring(1, reference.length() - 1);
        String rewritten = rewriter.rewriteAbsolute(path);
        if (!rewritten.equals(path)) {
          replacements.add(new Replacement(token.getStartIndex(), token.getStopIndex(), "[" + rewritten + "]"));
        }
      }
    }
    return replacements;
  }

  private void replacements(List<Reference> references, String scopeId, List<Replacement> out) {
    if (references == null) {
      return;
    }
    for (Reference reference : references) {
      if (reference instanceof FieldReference field) {
        if (changedId.equals(scopeId)) {
          String rewritten = rewriter.rewriteAbsolute(field.path());
          if (!rewritten.equals(field.path())) {
            out.add(new Replacement(field.start(), field.stop(), "[" + rewritten + "]"));
          }
        }
      }
      else if (reference instanceof HasCall has) {
        replacements(has.constraint(), roleModelId(has.relationshipModel(), has.targetRole()), out);
        replacements(has.linkConstraint(), linkModelId(has.relationshipModel()), out);
      }
    }
  }

  // ---- scopes -----------------------------------------------------------------------------------------------

  /** The id of the Document Model {@code role} plays in {@code relationshipModelId}, or null if unknown. */
  private String roleModelId(String relationshipModelId, String role) {
    RelationshipModel relationshipModel = relationshipModelId == null ? null : relationshipModels.get(relationshipModelId);
    if (relationshipModel == null || relationshipModel.getContent() == null || role == null) {
      return null;
    }
    return relationshipModel.getContent().getEntityCharacteristics().stream()
        .filter(characteristic -> role.equals(characteristic.getRole()))
        .map(EntityCharacteristic::getDocumentModel)
        .findFirst().orElse(null);
  }

  /** The id of {@code relationshipModelId}'s link Document Model, or null if unknown or unset. */
  private String linkModelId(String relationshipModelId) {
    RelationshipModel relationshipModel = relationshipModelId == null ? null : relationshipModels.get(relationshipModelId);
    return relationshipModel == null || relationshipModel.getContent() == null
        ? null : relationshipModel.getContent().getLinkDocumentModelValue();
  }

  // ---- helpers ----------------------------------------------------------------------------------------------

  private void pathSite(Supplier<String> getter, Consumer<String> setter) {
    String old = getter.get();
    if (old == null || old.isBlank()) {
      return;
    }
    String rewritten = rewriter.rewriteAbsolute(old);
    if (!rewritten.equals(old)) {
      edits.add(new Edit(setter, old, rewritten));
    }
  }

  private void listSites(List<String> paths) {
    if (paths == null) {
      return;
    }
    for (int i = 0; i < paths.size(); i++) {
      int index = i;
      pathSite(() -> paths.get(index), value -> paths.set(index, value));
    }
  }
}
