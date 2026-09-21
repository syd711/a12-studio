package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModelResolver;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.models.mappingmodel.SortField;
import de.a12.studio.models.printmodel.ComputationStep;
import de.a12.studio.models.printmodel.PrintCalculationElement;
import de.a12.studio.models.printmodel.PrintFieldElement;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.selectionmodel.PathSpecification;
import de.a12.studio.models.selectionmodel.SelectionCategory;
import de.a12.studio.models.selectionmodel.SelectionModel;
import de.a12.studio.models.structuralmappingmodel.FieldMapping;
import de.a12.studio.models.structuralmappingmodel.GroupToClearOnFirstFill;
import de.a12.studio.models.structuralmappingmodel.MappingBlock;
import de.a12.studio.models.structuralmappingmodel.ResolutionStrategy;
import de.a12.studio.models.structuralmappingmodel.Slice;
import de.a12.studio.models.structuralmappingmodel.StructuralMappingModel;
import de.a12.studio.modelsvalidation.formincludes.FormIncludeExpander;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.Edit;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.IncludedModelChange;
import de.a12.studio.modelsvalidation.refactoring.DocumentModelRefactoring.PathRewriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
 *       calculateIncludedPathChanges}); see {@link DocumentModelRefactoring.Plan#computeEdits(IncludedModelChange)}.
 *       That includes models that reach the changed one through a <em>chain</em> of Includes (A includes B includes the
 *       changed C), and an <b>Additive Document Model</b> whose base model (see {@link AdditiveDocumentModelResolver})
 *       is the changed one: its paths into the base, which its own file does not hold, follow as well.</li>
 *   <li><b>Print Model</b> - a Field element's {@code FieldRef.path} whose {@code model} is the changed one, and the
 *       {@code [<model id>/<path>]} field references in the operations of its calculation steps. (An {@code
 *       OverridableValue.path} is a path into the print model's own content, not a Document Model path, so it is not
 *       a reference to this model.)</li>
 *   <li><b>Query Model</b> - every field path evaluated against the changed model, wherever in the query it sits:
 *       the root's {@code fields}/{@code sort}/{@code constraint}/{@code filterDefinition} when it is the {@code
 *       targetDocumentModel}, and those of a relationship hop, a sort entry through a relationship and a {@code
 *       has} constraint when the changed model is the one the role plays (see {@link QueryReferenceRefactoring}).</li>
 *   <li><b>Mapping Model</b> - {@code SortField.sortFieldFullName} of every {@code Source} whose {@code dmId} is the
 *       changed one.</li>
 *   <li><b>Structural Mapping Model</b> - its {@code *FullName} paths. It has no Document Model reference of its own,
 *       the Mapping Models that use it name the source and target models, so a side is only rewritten when every
 *       Mapping Model using it agrees that the changed model is that side's one model.</li>
 *   <li><b>Selection Model</b> - {@code Selected}/{@code Unselected} paths. Same reason: rewritten only when every
 *       Combination Model that uses it has the changed model as its base.</li>
 *   <li><b>Form Model</b> bound to the changed model - the {@code hostDocumentModelPath} of its includes (the path of
 *       the Include group the included elements were bound to).</li>
 * </ul>
 *
 * <p><b>Not covered</b>: Form {@code
 * hostDocumentModelPath} of a form bound to a Document Model that merely <em>includes</em> the changed one (a path
 * into the included model); the mirrored groups of an Additive Document Model that its base renames or moves.
 * References by element id (Form, Overview, Tree, ...) can't break.
 */
public final class ProjectReferenceRefactoring {

  private static final Logger log = LoggerFactory.getLogger(ProjectReferenceRefactoring.class);

  // "[Person_DM/Person/Name]": a field a Print calculation step reads, qualified by the id of its Document Model.
  // Group 1 is that id, group 2 the absolute path (with its leading slash).
  private static final Pattern PRINT_FIELD_REFERENCE = Pattern.compile("\\[([^/\\]\\s]+)(/[^\\]]*)]");

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
      Map<String, DocumentModel> documentModels = documentModelsById(projectModels);
      boolean includesChanged = includes(documentModel, changedId, documentModels, new HashSet<>());
      boolean additiveOverChanged = documentModel instanceof AdditiveDocumentModel additive
          && baseModelIs(additive, changedId, projectModels);
      if (includesChanged || additiveOverChanged) {
        edits.addAll(DocumentModelRefactoring.prepare(documentModel).computeEdits(
            new IncludedModelChange(changedId, rewriter, documentModels::get, additiveOverChanged)));
      }
    }
    else if (other instanceof PrintModel print) {
      printEdits(print, changedId, rewriter, edits);
    }
    else if (other instanceof QueryModel query) {
      queryEdits(query, changedId, rewriter, projectModels, edits);
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
    else if (other instanceof FormModel form) {
      formEdits(form, changedId, rewriter, edits);
    }
    return edits;
  }

  // ---- Document Models --------------------------------------------------------------------------------------

  private static Map<String, DocumentModel> documentModelsById(Collection<? extends A12Model<?>> projectModels) {
    Map<String, DocumentModel> byId = new HashMap<>();
    for (A12Model<?> model : projectModels) {
      if (model instanceof DocumentModel documentModel && documentModel.getId() != null) {
        byId.putIfAbsent(documentModel.getId(), documentModel);
      }
    }
    return byId;
  }

  /** Whether {@code baseId} is the id of the base model of {@code additive} (see {@link AdditiveDocumentModelResolver}). */
  private static boolean baseModelIs(AdditiveDocumentModel additive, String baseId,
      Collection<? extends A12Model<?>> projectModels) {
    List<A12Model<?>> models = new ArrayList<>(projectModels);
    List<DocumentModel> documentModels = models.stream().filter(DocumentModel.class::isInstance)
        .map(DocumentModel.class::cast).toList();
    return AdditiveDocumentModelResolver.findBaseModel(additive, models, documentModels)
        .map(base -> baseId.equals(base.getId())).orElse(false);
  }

  /** Whether {@code model} includes {@code includedId} - directly, or through the Includes of the models it includes. */
  private static boolean includes(DocumentModel model, String includedId, Map<String, DocumentModel> documentModels,
      Set<String> visited) {
    if (model.getContent() == null || model.getContent().getModelRoot() == null
        || model.getContent().getModelRoot().getRootGroups() == null) {
      return false;
    }
    return model.getContent().getModelRoot().getRootGroups().stream()
        .anyMatch(root -> includes(root, includedId, documentModels, visited));
  }

  private static boolean includes(Element element, String includedId, Map<String, DocumentModel> documentModels,
      Set<String> visited) {
    if (!(element instanceof GroupElement group) || group.getGroup() == null) {
      return false;
    }
    String reference = group.getGroup().getIncludeConfig() == null ? null : group.getGroup().getIncludeConfig().getReference();
    if (reference != null) {
      if (includedId.equals(reference)) {
        return true;
      }
      DocumentModel included = documentModels.get(reference);
      if (included != null && visited.add(reference) && includes(included, includedId, documentModels, visited)) {
        return true;
      }
    }
    return group.getGroup().getElements() != null
        && group.getGroup().getElements().stream().anyMatch(child -> includes(child, includedId, documentModels, visited));
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
      else if (definition instanceof PrintCalculationElement element && element.getCalculation() != null) {
        for (ComputationStep step : element.getCalculation().getComputationAlternatives()) {
          operationSite(edits, changedId, rewriter, step);
        }
      }
    }
  }

  /**
   * A calculation step reads fields as {@code [<Document Model id>/<absolute path>]} (e.g. {@code
   * [Person_DM/Person/Name]}); the ones that read the changed model follow it, the rest of the operation - and the
   * references to other models - stay as they are.
   */
  private static void operationSite(List<Edit> edits, String changedId, PathRewriter rewriter, ComputationStep step) {
    String old = step.getOperation();
    if (old == null || old.isBlank()) {
      return;
    }
    Matcher matcher = PRINT_FIELD_REFERENCE.matcher(old);
    StringBuilder rewritten = new StringBuilder();
    int copiedUpTo = 0;
    while (matcher.find()) {
      if (!changedId.equals(matcher.group(1))) {
        continue;
      }
      rewritten.append(old, copiedUpTo, matcher.start(2)).append(rewriter.rewriteAbsolute(matcher.group(2)));
      copiedUpTo = matcher.end(2);
    }
    rewritten.append(old, copiedUpTo, old.length());
    if (!rewritten.toString().equals(old)) {
      edits.add(new Edit(step::setOperation, old, rewritten.toString()));
    }
  }

  // ---- Query Model ------------------------------------------------------------------------------------------

  private static void queryEdits(QueryModel query, String changedId, PathRewriter rewriter,
      Collection<? extends A12Model<?>> projectModels, List<Edit> edits) {
    QueryReferenceRefactoring.collect(query, changedId, rewriter, projectModels, edits);
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

  // ---- Form Model -------------------------------------------------------------------------------------------

  /**
   * The {@code hostDocumentModelPath} of the includes in a Form Model bound to the changed Document Model: the path
   * of the Include group the included elements were bound to, so renaming or moving that group (or a group above it)
   * must carry it along, or {@code FormIncludeProvenanceValidator} reports the include as pointing nowhere and a
   * refresh would fail. The element references of the included elements are ids and cannot break.
   */
  private static void formEdits(FormModel form, String changedId, PathRewriter rewriter, List<Edit> edits) {
    if (form.getContent() == null || !changedId.equals(FormIncludeExpander.documentModelIdOf(form))) {
      return;
    }
    for (ScreenElement element : FormModelWalker.find(form.getContent(), ScreenElement.class)) {
      if (element.getIncludeId() != null && !element.getIncludeId().isEmpty()) {
        pathSite(edits, rewriter, element::getHostDocumentModelPath, element::setHostDocumentModelPath);
      }
    }
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
}
