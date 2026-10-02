package de.a12.studio.kernel;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IEntity;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.ado.IRule;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.error.IProblem;
import com.mgmtp.a12.kernel.core.tool.a12internal.api.services.IMVK_Service;
import com.mgmtp.a12.kernel.md.model.a12internal.Computation;
import com.mgmtp.a12.kernel.md.model.a12internal.DocumentModel;
import com.mgmtp.a12.kernel.md.model.a12internal.Element;
import com.mgmtp.a12.kernel.md.model.a12internal.Rule;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelSearchService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.DocumentModelService;
import com.mgmtp.a12.kernel.md.model.a12internal.services.OriginInComputationFragment;
import com.mgmtp.a12.kernel.md.serializer.model.a12internal.services.DocumentModelSerializer;

/**
 * Facade over the kernel's per-rule and per-computation checks, the ones SME's rule and computation editors call
 * while typing: syntax and semantic problems of one condition with line and column, and the kernel's canonical
 * formatting of a condition. This is the {@code a12internal} API ({@code DocumentModelService}, {@code IMVK_Service}),
 * hence only here.
 *
 * <p>The model must be <em>expanded</em> (no unexpanded includes): the kernel refuses to build its validation
 * service otherwise, which surfaces as a {@link KernelException}. Use {@link KernelDocumentModelExpander} first.
 * Instances are cheap to keep per model; the kernel's validation service is built on first use and reused.
 */
public final class KernelRuleValidator {

  private final DocumentModel model;
  private IMVK_Service mvkService;

  /**
   * @param expandedDocumentModelJson the content of an expanded Document Model JSON (header + content)
   * @throws KernelException if the kernel cannot read it as a Document Model
   */
  public KernelRuleValidator(String expandedDocumentModelJson) {
    try {
      this.model = new DocumentModelSerializer().deserialize(new StringReader(expandedDocumentModelJson));
    }
    catch (RuntimeException e) {
      throw new KernelException("The kernel cannot read the Document Model: " + e.getMessage(), e);
    }
  }

  /**
   * Checks a rule's {@code errorCondition}.
   *
   * @param ruleId id of the rule element
   * @return the problems with position; empty if the condition is valid, or if the rule has no error field yet
   *         (nothing to check against, same as SME)
   * @throws KernelException if there is no such rule or the kernel cannot work on the model
   */
  public List<KernelProblem> validateCondition(String ruleId) {
    Rule rule = element(ruleId, Rule.class, "rule");
    if (rule.getErrorEntity().getDocumentModelObject().isEmpty()) {
      return List.of();
    }
    ProblemCollector problems = new ProblemCollector();
    try {
      new DocumentModelService().hasValidConditionText(mvk(), rule, problems);
    }
    catch (KernelException e) {
      throw e;
    }
    catch (RuntimeException e) {
      throw new KernelException("The kernel cannot check rule " + ruleId + ": " + e.getMessage(), e);
    }
    return problems.problems.stream()
        .map(p -> new KernelProblem(p.getLine(), p.getSourceStart(), p.getSourceEnd(), p.getMessage()))
        .toList();
  }

  /**
   * Checks a computation's preconditions and operations.
   *
   * @param computationId id of the computation element
   * @return the problems, split like SME does: positioned ones and position-less ones; valid and empty if the
   *         computation has no computed field yet
   * @throws KernelException if there is no such computation or the kernel cannot work on the model
   */
  public KernelComputationResult validateComputation(String computationId) {
    Computation computation = element(computationId, Computation.class, "computation");
    if (computation.getComputedField().getDocumentModelObject().isEmpty()) {
      return new KernelComputationResult(List.of(), List.of());
    }
    ProblemCollector problems = new ProblemCollector();
    try {
      DocumentModelService service = new DocumentModelService();
      service.isValidComputation(service.getPath(computation), model, problems);
    }
    catch (RuntimeException e) {
      throw new KernelException("The kernel cannot check computation " + computationId + ": " + e.getMessage(), e);
    }
    List<String> semantic = new ArrayList<>();
    List<KernelComputationProblem> parser = new ArrayList<>();
    for (IProblem problem : problems.problems) {
      var origin = OriginInComputationFragment.of(problem);
      if (origin.isEmpty()) {
        semantic.add(problem.getMessage());
        continue;
      }
      OriginInComputationFragment fragment = origin.get();
      KernelComputationProblem.Part part = fragment.isCommonPrecondition() ? KernelComputationProblem.Part.COMMON_PRECONDITION
          : fragment.isPrecondition() ? KernelComputationProblem.Part.PRECONDITION : KernelComputationProblem.Part.OPERATION;
      var start = fragment.getStartInFragment().getLineAndColumn();
      var end = fragment.getEndInFragment().getLineAndColumn();
      parser.add(new KernelComputationProblem(part, fragment.getComputationAlternativeIndex(), start.getLeft(),
          start.getRight(), end.getRight(), problem.getMessage()));
    }
    return new KernelComputationResult(semantic, parser);
  }

  /**
   * The kernel's formatting of a rule's {@code errorCondition}.
   *
   * @return the formatted text; the original condition if the kernel returns nothing (e.g. it does not parse)
   * @throws KernelException if there is no such rule or it has no error field
   */
  public String formatCondition(String ruleId) {
    Rule rule = element(ruleId, Rule.class, "rule");
    var errorEntity = rule.getErrorEntity().getDocumentModelObject();
    if (errorEntity.isEmpty()) {
      throw new KernelException("Rule " + ruleId + " has no error field, its condition cannot be formatted", null);
    }
    try {
      IMVK_Service service = mvk();
      DocumentModelService modelService = new DocumentModelService();
      IEntity errorField = service.getIEC().get(modelService.getPath((Element) errorEntity.get()));
      IRule iRule = new FormattableRule(modelService.getPath(rule), errorField, rule.getErrorCondition());
      String formatted = service.formatConditionText(iRule, new ProblemCollector());
      return formatted == null || formatted.isBlank() ? rule.getErrorCondition() : formatted;
    }
    catch (KernelException e) {
      throw e;
    }
    catch (RuntimeException e) {
      throw new KernelException("The kernel cannot format rule " + ruleId + ": " + e.getMessage(), e);
    }
  }

  private IMVK_Service mvk() {
    if (mvkService == null) {
      try {
        mvkService = new DocumentModelService().getMvkServiceForModel(model);
      }
      catch (RuntimeException e) {
        throw new KernelException("The kernel cannot build its validation service (is the model expanded?): " + e.getMessage(), e);
      }
    }
    return mvkService;
  }

  private <T extends Element> T element(String id, Class<T> type, String what) {
    Element found = new DocumentModelSearchService(model).getById(id)
        .orElseThrow(() -> new KernelException("No " + what + " with id " + id + " in the model", null));
    if (!type.isInstance(found)) {
      throw new KernelException("Element " + id + " is not a " + what, null);
    }
    return type.cast(found);
  }
}
