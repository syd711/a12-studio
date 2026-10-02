package de.a12.studio.modelsvalidation.kernel;

import java.util.Optional;

import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.a12.studio.kernel.KernelComputationProblem;
import de.a12.studio.kernel.KernelComputationProblem.Part;
import de.a12.studio.kernel.KernelComputationResult;
import de.a12.studio.kernel.KernelException;
import de.a12.studio.kernel.KernelRuleValidator;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Semantic check of one computation's texts (common precondition, an alternative's precondition or operation)
 * against the A12 kernel while the user types them, the counterpart of {@link RuleConditionKernelCheck} for
 * computations. The model is expanded once, from the in-memory {@link ProjectItem} tree, and each check swaps the
 * typed text into that copy. {@link #check} returns {@code null} for "no kernel opinion" (kernel cannot expand or
 * read the model, nothing positioned for the edited part).
 */
public final class ComputationKernelCheck {

  private static final Logger log = LoggerFactory.getLogger(ComputationKernelCheck.class);

  /** The part of the computation a text belongs to. */
  public enum TextPart {
    COMMON_PRECONDITION, PRECONDITION, OPERATION
  }

  private final ProjectItem contextItem;
  private final String documentModelId;
  private final String computationId;

  private boolean expanded;
  private @Nullable ObjectNode model;

  public ComputationKernelCheck(ProjectItem contextItem, String documentModelId, String computationId) {
    this.contextItem = contextItem;
    this.documentModelId = documentModelId;
    this.computationId = computationId;
  }

  /**
   * @param part             which text is being edited
   * @param alternativeIndex index of the alternative the text belongs to (ignored for the common precondition);
   *                         an index past the end stands for an alternative that is not stored yet
   * @param text             the text as currently typed
   * @return the kernel's first problem for that text with its position (1-based line and column), or {@code null}
   */
  public @Nullable String check(TextPart part, int alternativeIndex, String text) {
    ObjectNode root = expandedModel();
    if (root == null) {
      return null;
    }
    JsonNode element = RuleConditionKernelCheck.find(root.path("content").path("modelRoot").path("rootGroups"), computationId);
    if (element == null || !(element.get("Computation") instanceof ObjectNode body)) {
      log.debug("Computation {} is not in the expanded model of {}", computationId, documentModelId);
      return null;
    }
    ArrayNode alternatives = body.has("computationAlternatives") && body.get("computationAlternatives") instanceof ArrayNode a
        ? a : body.putArray("computationAlternatives");
    if (part == TextPart.COMMON_PRECONDITION) {
      body.put("commonPrecondition", text);
    }
    else {
      while (alternatives.size() <= alternativeIndex) {
        alternatives.addObject().put("operation", "").put("precondition", "");
      }
      ((ObjectNode) alternatives.get(alternativeIndex)).put(part == TextPart.PRECONDITION ? "precondition" : "operation", text);
    }
    try {
      KernelComputationResult result = new KernelRuleValidator(JsonSettings.objectMapper.writeValueAsString(root))
          .validateComputation(computationId);
      Part kernelPart = Part.valueOf(part.name());
      Optional<KernelComputationProblem> problem = result.parserErrors().stream()
          .filter(p -> p.part() == kernelPart && (part == TextPart.COMMON_PRECONDITION || p.alternativeIndex() == alternativeIndex))
          .findFirst();
      return problem.map(p -> p.message() + " (line " + p.line() + ", column " + (p.startColumn() + 1) + ")").orElse(null);
    }
    catch (KernelException e) {
      log.debug("Kernel could not check computation {}: {}", computationId, e.getMessage());
      return null;
    }
    catch (Exception e) {
      log.warn("Kernel check of computation {} failed unexpectedly: {}", computationId, e.getMessage());
      return null;
    }
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
}
