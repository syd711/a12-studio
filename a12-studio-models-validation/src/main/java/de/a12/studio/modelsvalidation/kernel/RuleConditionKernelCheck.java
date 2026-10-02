package de.a12.studio.modelsvalidation.kernel;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.a12.studio.kernel.KernelDocumentModelExpander;
import de.a12.studio.kernel.KernelException;
import de.a12.studio.kernel.KernelProblem;
import de.a12.studio.kernel.KernelRuleValidator;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Semantic check of one rule's {@code errorCondition} against the A12 kernel while the user types it: unknown
 * fields, wrong function use and the like, which a grammar-only check cannot see. The model is expanded once on the
 * first check (from the in-memory {@link ProjectItem} tree, so unsaved edits elsewhere count) and reused; each
 * check only swaps the rule's text in that copy and asks the kernel.
 *
 * <p>Meant to be created per selected rule and discarded when the selection changes. It never breaks an editor:
 * if the kernel cannot expand or read the model (a half-edited model, a missing include), {@link #check} returns
 * {@code null}, i.e. "no kernel opinion", and the caller keeps whatever other validation it has.
 */
public final class RuleConditionKernelCheck {

  private static final Logger log = LoggerFactory.getLogger(RuleConditionKernelCheck.class);

  /**
   * A one-rule model for {@link #warmUp()}: the first kernel call of a session spends about 0.8 s on class loading
   * and JIT (measured 2026-10-02), later checks 3-10 ms.
   */
  private static final String WARM_UP_MODEL = """
      {"header":{"id":"Warmup_DM","modelType":"document","modelVersion":"29.4.0","locales":[{"code":"en"}],"labels":[],
      "annotations":[],"modelReferences":[]},"content":{"modelInfo":{"name":"Warmup_DM","immutable":false},
      "modelConfig":{"timeZone":"UTC","decimalSeparator":".","conditionLanguage":{"code":"en_US"}},
      "modelRoot":{"rootGroups":[{"type":"Group","id":"g1","name":"Root","Group":{"repeatability":1,"elements":[
      {"type":"Field","id":"f1","name":"A","Field":{"fieldType":{"type":"StringType"}}},
      {"type":"Rule","id":"r1","name":"R","Rule":{"errorEntityRelPath":"../A","errorCode":"E",
      "errorCondition":"FieldNotFilled(A)","severity":"ERROR","errorMessage":[{"locale":"en","text":"x"}]}}]}}]}}}
      """;

  private static final AtomicBoolean warmUpStarted = new AtomicBoolean();

  private final ProjectItem contextItem;
  private final String documentModelId;
  private final String ruleId;

  private boolean expanded;
  private @Nullable ObjectNode model;

  /**
   * @param contextItem     any item of the project, used to find the models the kernel needs
   * @param documentModelId id of the Document Model the rule belongs to
   * @param ruleId          id of the rule element within that model
   */
  public RuleConditionKernelCheck(ProjectItem contextItem, String documentModelId, String ruleId) {
    this.contextItem = contextItem;
    this.documentModelId = documentModelId;
    this.ruleId = ruleId;
  }

  /**
   * @param conditionText the condition as currently typed
   * @return the kernel's first problem with its position (1-based line and column), or {@code null} if the kernel
   *         finds none or could not check
   */
  public @Nullable String check(String conditionText) {
    ObjectNode root = expandedModel();
    if (root == null) {
      return null;
    }
    JsonNode rule = find(root.path("content").path("modelRoot").path("rootGroups"), ruleId);
    if (rule == null || !(rule.get("Rule") instanceof ObjectNode ruleBody)) {
      log.debug("Rule {} is not in the expanded model of {}", ruleId, documentModelId);
      return null;
    }
    ruleBody.put("errorCondition", conditionText);
    try {
      List<KernelProblem> problems = new KernelRuleValidator(JsonSettings.objectMapper.writeValueAsString(root))
          .validateCondition(ruleId);
      return problems.isEmpty() ? null : describe(problems.get(0));
    }
    catch (KernelException e) {
      log.debug("Kernel could not check rule {}: {}", ruleId, e.getMessage());
      return null;
    }
    catch (Exception e) {
      log.warn("Kernel check of rule {} failed unexpectedly: {}", ruleId, e.getMessage());
      return null;
    }
  }

  /**
   * Starts loading the kernel on a background thread (once per JVM), so that the first {@link #check} the user
   * triggers does not stall the UI thread. Call it as soon as a rule editor exists.
   */
  public static void warmUpAsync() {
    if (warmUpStarted.compareAndSet(false, true)) {
      Thread thread = new Thread(RuleConditionKernelCheck::warmUp, "kernel-warm-up");
      thread.setDaemon(true);
      thread.start();
    }
  }

  /** Runs one expansion and one condition check on a tiny model; returns whether the kernel answered. */
  static boolean warmUp() {
    long start = System.nanoTime();
    log.info("Kernel warm-up started");
    try {
      String expanded = new KernelDocumentModelExpander().expand("Warmup_DM", id -> WARM_UP_MODEL).expandedJson();
      boolean answered = new KernelRuleValidator(expanded).validateCondition("r1").isEmpty();
      log.info("Kernel warm-up finished in {} ms (kernel answered: {})", (System.nanoTime() - start) / 1_000_000, answered);
      return answered;
    }
    catch (RuntimeException e) {
      log.warn("Kernel warm-up failed after {} ms: {}", (System.nanoTime() - start) / 1_000_000, e.getMessage());
      return false;
    }
  }

  /** The kernel counts lines from 1 and columns from 0; users read both from 1. */
  static String describe(KernelProblem problem) {
    return problem.message() + " (line " + problem.line() + ", column " + (problem.startColumn() + 1) + ")";
  }

  private @Nullable ObjectNode expandedModel() {
    if (!expanded) {
      expanded = true;
      Optional<String> json = ProjectKernelModels.expandToJson(contextItem, documentModelId);
      if (json.isPresent()) {
        try {
          model = (ObjectNode) JsonSettings.objectMapper.readTree(json.get());
        }
        catch (Exception e) {
          log.warn("Expanded model of {} could not be read: {}", documentModelId, e.getMessage());
        }
      }
    }
    return model;
  }

  static @Nullable JsonNode find(JsonNode elements, String id) {
    for (JsonNode element : elements) {
      if (id.equals(element.path("id").asString())) {
        return element;
      }
      JsonNode found = find(element.path(element.path("type").asString()).path("elements"), id);
      if (found != null) {
        return found;
      }
    }
    return null;
  }
}
