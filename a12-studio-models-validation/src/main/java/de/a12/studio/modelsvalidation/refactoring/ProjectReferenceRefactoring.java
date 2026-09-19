package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.models.mappingmodel.SortField;
import de.a12.studio.models.printmodel.PrintFieldElement;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.ql.QLLexer;
import de.a12.studio.models.querymodel.operator.AndOperator;
import de.a12.studio.models.querymodel.operator.DateFragmentRangeOperator;
import de.a12.studio.models.querymodel.operator.DateRangeOperator;
import de.a12.studio.models.querymodel.operator.DoubleRangeOperator;
import de.a12.studio.models.querymodel.operator.ExactMatchOperator;
import de.a12.studio.models.querymodel.operator.NotOperator;
import de.a12.studio.models.querymodel.operator.Operator;
import de.a12.studio.models.querymodel.operator.OrOperator;
import de.a12.studio.models.querymodel.operator.SimpleSearchOperator;
import de.a12.studio.models.querymodel.operator.UndefinedMatchOperator;
import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionCategory;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.models.structuralmappingmodel.FieldMapping;
import de.a12.studio.models.structuralmappingmodel.GroupToClearOnFirstFill;
import de.a12.studio.models.structuralmappingmodel.MappingBlock;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategy;
import de.a12.studio.models.structuralmappingmodel.Slice;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.IncludedModelChange;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.PathRewriter;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Keeps the references <em>other project models</em> hold on a Document Model valid while one of its elements is
 * renamed or moved - the cross-model half of {@link DocumentModelRefactoring}, which only looks inside the changed
 * model. Given the {@link DocumentModelRefactoring.Plan} of the change (taken before it, asked after it), it finds
 * every such reference in the project's other models and returns the edits that bring it in line, per model, so the
 * caller can apply them in the same undo step, save the models and tell the editors that have them open.
 *
 * <p>Nothing is guessed: a reference is only touched when the model it lives in demonstrably points at the changed
 * Document Model, and it is left alone (and logged) when that isn't clear. What is covered:
 * <ul>
 *   <li><b>Document Models that include the changed one</b> - rules and computations whose paths run through the
 *       Include group into the changed model (SME: {@code calculateIncludedNameChanges}/{@code
 *       calculateIncludedPathChanges}); see {@link DocumentModelRefactoring.Plan#computeEdits(IncludedModelChange)}.</li>
 *   <li><b>Print Model</b> - a Field element's {@code FieldRef.path} whose {@code model} is the changed one. (An
 *       {@code OverridableValue.path} is a path into the print model's own content, not a Document Model path, so it
 *       is not a reference to this model.)</li>
 *   <li><b>Query Model</b> whose {@code targetDocumentModel} is the changed one - {@code fields}, {@code sort}
 *       entries without a relationship, the field paths of the top-level {@code constraint} operators, and the
 *       {@code [/Path]} field references of {@code filterDefinition}.</li>
 *   <li><b>Mapping Model</b> - {@code SortField.sortFieldFullName} of every {@code Source} whose {@code dmId} is the
 *       changed one.</li>
 *   <li><b>Structural Mapping Model</b> - its {@code *FullName} paths. It has no Document Model reference of its own,
 *       the Mapping Models that use it name the source and target models, so a side is only rewritten when every
 *       Mapping Model using it agrees that the changed model is that side's one model.</li>
 *   <li><b>Selection Model</b> - {@code Selected}/{@code Unselected} paths. Same reason: rewritten only when every
 *       Combination Model that uses it has the changed model as its base.</li>
 * </ul>
 *
 * <p><b>Not covered</b>: a Query's relationship links ({@code links[].fields}, and constraints below a {@code has}),
 * whose Document Model is that of the linked role, not the query's; Print calculation steps; Form {@code
 * hostDocumentModelPath} (transclusion provenance, not a live reference); paths that reach an element through a chain
 * of Includes or an Additive base model. References by element id (Form, Overview, Tree, ...) can't break.
 */
public final class ProjectReferenceRefactoring {

  private static final Logger log = LoggerFactory.getLogger(ProjectReferenceRefactoring.class);

  private ProjectReferenceRefactoring() {
  }

  /** The edits to one model of the project; {@code model} is the instance the caller passed in. */
  public record ModelEdits(A12Model<?> model, List<Edit> edits) {
  }

  /**
   * The edits that update the references other models of {@code projectModels} hold on {@code changed}, after the
   * structural change that {@code plan} was prepared for has been made. Models without any affected reference yield
   * nothing. Never throws for a single model's sake: one that can't be analysed is logged and skipped.
   */
  public static List<ModelEdits> computeEdits(DocumentModel changed, DocumentModelRefactoring.Plan plan,
      Collection<? extends A12Model<?>> projectModels) {
    String changedId = changed.getId();
    if (changedId == null) {
      return List.of();
    }
    PathRewriter rewriter = plan.pathRewriter();
    List<ModelEdits> result = new ArrayList<>();
    for (A12Model<?> other : projectModels) {
      if (other == null || other == changed) {
        continue;
      }
      try {
        List<Edit> edits = editsFor(other, changedId, rewriter, projectModels);
        if (!edits.isEmpty()) {
          result.add(new ModelEdits(other, edits));
        }
      }
      catch (RuntimeException e) {
        log.warn("Could not update the references {} holds on {}: {}", other.getId(), changedId, e.getMessage(), e);
      }
    }
    return result;
  }

  private static List<Edit> editsFor(A12Model<?> other, String changedId, PathRewriter rewriter,
      Collection<? extends A12Model<?>> projectModels) {
    List<Edit> edits = new ArrayList<>();
    if (other instanceof DocumentModel documentModel) {
      if (includes(documentModel, changedId)) {
        edits.addAll(DocumentModelRefactoring.prepare(documentModel)
            .computeEdits(new IncludedModelChange(changedId, rewriter)));
      }
    }
    else if (other instanceof PrintModel print) {
      printEdits(print, changedId, rewriter, edits);
    }
    else if (other instanceof QueryModel query) {
      queryEdits(query, changedId, rewriter, edits);
    }
    else if (other instanceof MappingModel mapping) {
      mappingEdits(mapping, changedId, rewriter, edits);
    }
    else if (other instanceof StructuralMappingModel structural) {
      structuralMappingEdits(structural, changedId, rewriter, projectModels, edits);
    }
    else if (other instanceof SelectionModel selection) {
      selectionEdits(selection, changedId, rewriter, projectModels, edits);
    }
    return edits;
  }

  // ---- Document Models --------------------------------------------------------------------------------------

  private static boolean includes(DocumentModel model, String includedId) {
    if (model.getContent() == null || model.getContent().getModelRoot() == null
        || model.getContent().getModelRoot().getRootGroups() == null) {
      return false;
    }
    return model.getContent().getModelRoot().getRootGroups().stream().anyMatch(root -> includes(root, includedId));
  }

  private static boolean includes(Element element, String includedId) {
    if (!(element instanceof GroupElement group) || group.getGroup() == null) {
      return false;
    }
    if (group.getGroup().getIncludeConfig() != null
        && includedId.equals(group.getGroup().getIncludeConfig().getReference())) {
      return true;
    }
    return group.getGroup().getElements() != null
        && group.getGroup().getElements().stream().anyMatch(child -> includes(child, includedId));
  }

  // ---- Print Model ------------------------------------------------------------------------------------------

  private static void printEdits(PrintModel print, String changedId, PathRewriter rewriter, List<Edit> edits) {
    if (print.getContent() == null || print.getContent().getElementDefinitions() == null) {
      return;
    }
    for (PrintElementDefinition definition : print.getContent().getElementDefinitions()) {
      if (definition instanceof PrintFieldElement field && field.getField() != null
          && changedId.equals(field.getField().getModel())) {
        pathSite(edits, rewriter, field.getField()::getPath, field.getField()::setPath);
      }
    }
  }

  // ---- Query Model ------------------------------------------------------------------------------------------

  private static void queryEdits(QueryModel query, String changedId, PathRewriter rewriter, List<Edit> edits) {
    QueryModelContent content = query.getContent();
    if (content == null || !changedId.equals(content.getTargetDocumentModel())) {
      return;
    }
    listSites(edits, rewriter, content.getFields());
    for (QuerySort sort : content.getSort()) {
      if (sort.getRelationshipModel() == null && sort.getSortBy() != null) {
        pathSite(edits, rewriter, sort.getSortBy()::getField, sort.getSortBy()::setField);
      }
    }
    operatorSites(edits, rewriter, content.getConstraint());
    if (content.getFilterDefinition() != null) {
      String text = content.getFilterDefinition();
      String rewritten = rewriteQueryLanguage(text, rewriter);
      if (!rewritten.equals(text)) {
        edits.add(new Edit(content::setFilterDefinition, text, rewritten));
      }
    }
  }

  /** Field paths of {@code operator} and everything below it, except below a {@code has} (a linked model's fields). */
  private static void operatorSites(List<Edit> edits, PathRewriter rewriter, Operator operator) {
    if (operator instanceof AndOperator and) {
      if (and.getOperands() != null) {
        and.getOperands().forEach(operand -> operatorSites(edits, rewriter, operand));
      }
    }
    else if (operator instanceof OrOperator or) {
      if (or.getOperands() != null) {
        or.getOperands().forEach(operand -> operatorSites(edits, rewriter, operand));
      }
    }
    else if (operator instanceof NotOperator not) {
      operatorSites(edits, rewriter, not.getOperand());
    }
    else if (operator instanceof ExactMatchOperator exact) {
      pathSite(edits, rewriter, exact::getField, exact::setField);
    }
    else if (operator instanceof UndefinedMatchOperator undefined) {
      pathSite(edits, rewriter, undefined::getField, undefined::setField);
    }
    else if (operator instanceof DoubleRangeOperator range) {
      pathSite(edits, rewriter, range::getField, range::setField);
    }
    else if (operator instanceof DateRangeOperator range) {
      pathSite(edits, rewriter, range::getField, range::setField);
    }
    else if (operator instanceof DateFragmentRangeOperator range) {
      pathSite(edits, rewriter, range::getField, range::setField);
    }
    else if (operator instanceof SimpleSearchOperator search) {
      listSites(edits, rewriter, search.getFields());
    }
  }

  /**
   * {@code text} with every {@code [/Path/To/Field]} field reference of the query language rewritten. Found through
   * the lexer, so a path-looking string literal is never touched; text that doesn't lex cleanly is left alone.
   */
  static String rewriteQueryLanguage(String text, PathRewriter rewriter) {
    QLLexer lexer = new QLLexer(CharStreams.fromString(text));
    lexer.removeErrorListeners();
    boolean[] failed = {false};
    lexer.addErrorListener(new org.antlr.v4.runtime.BaseErrorListener() {
      @Override
      public void syntaxError(org.antlr.v4.runtime.Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
          int charPositionInLine, String msg, org.antlr.v4.runtime.RecognitionException e) {
        failed[0] = true;
      }
    });
    List<? extends Token> tokens = lexer.getAllTokens();
    if (failed[0]) {
      return text;
    }
    List<Token> fields = new ArrayList<>();
    for (Token token : tokens) {
      if (token.getType() == QLLexer.I_FIELD) {
        fields.add(token);
      }
    }
    fields.sort(Comparator.comparingInt(Token::getStartIndex).reversed());
    StringBuilder result = new StringBuilder(text);
    for (Token token : fields) {
      String reference = token.getText();
      String path = reference.substring(1, reference.length() - 1);
      String rewritten = rewriter.rewriteAbsolute(path);
      if (!rewritten.equals(path)) {
        // ANTLR's CharStream indexes by code point, String by UTF-16 unit (see PathLocator).
        int start = text.offsetByCodePoints(0, token.getStartIndex());
        int end = text.offsetByCodePoints(0, token.getStopIndex() + 1);
        result.replace(start, end, "[" + rewritten + "]");
      }
    }
    return result.toString();
  }

  // ---- Mapping Model / Structural Mapping Model -------------------------------------------------------------

  private static void mappingEdits(MappingModel mapping, String changedId, PathRewriter rewriter, List<Edit> edits) {
    if (mapping.getContent() == null || mapping.getContent().getSource() == null) {
      return;
    }
    for (MappingSource source : mapping.getContent().getSource()) {
      if (!changedId.equals(source.getDmId()) || source.getSortInfo() == null) {
        continue;
      }
      for (SortField sortField : source.getSortInfo().getSortFields()) {
        pathSite(edits, rewriter, sortField::getSortFieldFullName, sortField::setSortFieldFullName);
      }
    }
  }

  private static void structuralMappingEdits(StructuralMappingModel structural, String changedId,
      PathRewriter rewriter, Collection<? extends A12Model<?>> projectModels, List<Edit> edits) {
    if (structural.getContent() == null) {
      return;
    }
    Set<String> sourceModels = new HashSet<>();
    Set<String> targetModels = new HashSet<>();
    for (A12Model<?> candidate : projectModels) {
      if (candidate instanceof MappingModel mapping && mapping.getContent() != null
          && mapping.getContent().getStructuralMappingModel() != null
          && structural.getId() != null
          && structural.getId().equals(mapping.getContent().getStructuralMappingModel().getId())) {
        if (mapping.getContent().getSource() != null) {
          mapping.getContent().getSource().forEach(source -> sourceModels.add(source.getDmId()));
        }
        if (mapping.getContent().getTarget() != null) {
          targetModels.add(mapping.getContent().getTarget().getDmId());
        }
      }
    }
    boolean rewriteSource = sourceModels.equals(Set.of(changedId));
    boolean rewriteTarget = targetModels.equals(Set.of(changedId));
    if (!rewriteSource && !rewriteTarget) {
      if (sourceModels.contains(changedId) || targetModels.contains(changedId)) {
        log.warn("{} maps {} together with other models, so its paths into {} are not updated automatically",
            structural.getId(), changedId, changedId);
      }
      return;
    }

    for (GroupToClearOnFirstFill group : structural.getContent().getGroupsToClearOnFirstFill()) {
      if (rewriteTarget) {
        pathSite(edits, rewriter, group::getFullName, group::setFullName);
      }
    }
    for (MappingBlock block : structural.getContent().getMappingBlocks()) {
      for (ResolutionStrategy strategy : block.getResolutionStrategies()) {
        if (rewriteSource) {
          pathSite(edits, rewriter, strategy::getSourceGroupFullName, strategy::setSourceGroupFullName);
        }
        if (rewriteTarget) {
          pathSite(edits, rewriter, strategy::getTargetGroupFullName, strategy::setTargetGroupFullName);
        }
        Slice slice = strategy.getSlice();
        if (slice != null) {
          if (rewriteSource) {
            pathSite(edits, rewriter, slice::getSourceFieldFullName, slice::setSourceFieldFullName);
          }
          if (rewriteTarget) {
            pathSite(edits, rewriter, slice::getTargetFieldFullName, slice::setTargetFieldFullName);
          }
        }
      }
      for (FieldMapping mapping : block.getFieldMappings()) {
        if (rewriteSource) {
          pathSite(edits, rewriter, mapping::getSourceFieldFullName, mapping::setSourceFieldFullName);
        }
        if (rewriteTarget) {
          pathSite(edits, rewriter, mapping::getTargetFieldFullName, mapping::setTargetFieldFullName);
        }
      }
    }
  }

  // ---- Selection Model --------------------------------------------------------------------------------------

  private static void selectionEdits(SelectionModel selection, String changedId, PathRewriter rewriter,
      Collection<? extends A12Model<?>> projectModels, List<Edit> edits) {
    if (selection.getContent() == null || selection.getId() == null) {
      return;
    }
    Set<String> bases = new HashSet<>();
    for (A12Model<?> candidate : projectModels) {
      if (candidate instanceof CombinedDocumentModel combined && combined.getContent() != null
          && combined.getContent().getCombinationSteps() != null
          && combined.getContent().getCombinationSteps().stream().anyMatch(step -> uses(step, selection.getId()))) {
        bases.add(combined.getContent().getBaseModelId());
      }
    }
    if (!bases.equals(Set.of(changedId))) {
      if (bases.contains(changedId)) {
        log.warn("{} is used by Combination Models with different base models, so its paths into {} are not "
            + "updated automatically", selection.getId(), changedId);
      }
      return;
    }
    for (SelectionCategory category : List.of(selection.getContent().getData(), selection.getContent().getComputation(),
        selection.getContent().getValidation())) {
      if (category == null) {
        continue;
      }
      for (List<PathSpecification> specifications : java.util.Arrays.asList(category.getSelected(),
          category.getUnselected())) {
        if (specifications != null) {
          specifications.forEach(specification ->
              pathSite(edits, rewriter, specification::getPath, specification::setPath));
        }
      }
    }
  }

  private static boolean uses(CombinationStep step, String selectionId) {
    return step.getSelectionModel() != null && selectionId.equals(step.getSelectionModel().getSmId());
  }

  // ---- helpers ----------------------------------------------------------------------------------------------

  private static void pathSite(List<Edit> edits, PathRewriter rewriter, Supplier<String> getter,
      Consumer<String> setter) {
    String old = getter.get();
    if (old == null || old.isBlank()) {
      return;
    }
    String rewritten = rewriter.rewriteAbsolute(old);
    if (!rewritten.equals(old)) {
      edits.add(new Edit(setter, old, rewritten));
    }
  }

  /** The paths held in {@code paths}, rewritten in place (the list itself is what a model holds on to). */
  private static void listSites(List<Edit> edits, PathRewriter rewriter, List<String> paths) {
    if (paths == null) {
      return;
    }
    for (int i = 0; i < paths.size(); i++) {
      int index = i;
      pathSite(edits, rewriter, () -> paths.get(index), value -> paths.set(index, value));
    }
  }
}
