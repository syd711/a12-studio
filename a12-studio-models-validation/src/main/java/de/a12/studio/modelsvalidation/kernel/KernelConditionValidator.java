package de.a12.studio.modelsvalidation.kernel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.a12.studio.kernel.KernelComputationProblem;
import de.a12.studio.kernel.KernelException;
import de.a12.studio.kernel.KernelProblem;
import de.a12.studio.kernel.KernelRuleValidator;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.rulelang.RuleLanguageSyntaxChecker;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

/**
 * The semantic half of the condition checks: what the kernel finds wrong with a rule's {@code errorCondition} and a
 * computation's preconditions/operations once the paths and functions are resolved against the (expanded) model -
 * unknown fields, wrong function use, and the like. The grammar is {@link
 * de.a12.studio.modelsvalidation.validators.RuleConditionSyntaxValidator}'s job; a text it already flags is not
 * reported twice. Only elements of the validated model itself are reported (rules that come in through an include
 * are read-only and belong to the included model).
 *
 * <p>{@code validateElement} runs the whole validation for every panel on every selection, so the kernel verdict is
 * cached per model: the key is the serialized model plus the serialized models its header references (includes,
 * type definitions), i.e. everything the kernel expansion reads, so a verdict is recomputed only after an edit that
 * can change it. Models the kernel cannot expand (half-edited, a missing include) yield no kernel findings.
 */
public final class KernelConditionValidator implements ModelValidator {

  private static final Logger log = LoggerFactory.getLogger(KernelConditionValidator.class);

  private record Finding(String elementId, String property, String key, Object[] arguments) {
  }

  private record Cached(String fingerprint, List<Finding> findings) {
  }

  private static final Map<A12Model<?>, Cached> cache = Collections.synchronizedMap(new WeakHashMap<>());

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    ProjectItem item = context.projectItem();
    if (!(model instanceof DocumentModel documentModel) || item == null) {
      return List.of();
    }
    String fingerprint = fingerprint(documentModel, item);
    if (fingerprint == null) {
      return List.of();
    }
    Cached cached = cache.get(model);
    if (cached == null || !cached.fingerprint().equals(fingerprint)) {
      cached = new Cached(fingerprint, compute(documentModel, item, context));
      cache.put(model, cached);
    }
    return cached.findings().stream()
        .map(f -> new ModelValidationError(model, f.elementId(), f.property(), ValidationMessages.get(f.key(), f.arguments()),
            Severity.ERROR.name()))
        .toList();
  }

  private static String fingerprint(DocumentModel model, ProjectItem item) {
    try {
      StringBuilder key = new StringBuilder(JsonSettings.objectMapper.writeValueAsString(model));
      for (var reference : model.getModelReferences()) {
        ProjectItem referenced = reference.getReference() == null ? null : item.findByModelId(reference.getReference());
        if (referenced != null && referenced.getModel() != null && referenced.getModel() != model) {
          key.append('\n').append(JsonSettings.objectMapper.writeValueAsString(referenced.getModel()));
        }
      }
      return key.toString();
    }
    catch (Exception e) {
      return null;
    }
  }

  private static List<Finding> compute(DocumentModel model, ProjectItem item, ValidationContext context) {
    Optional<String> expanded = ProjectKernelModels.expandToJson(item, model.getId());
    if (expanded.isEmpty()) {
      return List.of();
    }
    List<Finding> findings = new ArrayList<>();
    try {
      KernelRuleValidator validator = new KernelRuleValidator(expanded.get());
      for (Element element : context.elementIndex().allElements()) {
        if (element instanceof RuleElement rule && rule.getRule() != null && hasText(rule.getRule().getErrorCondition())
            && RuleLanguageSyntaxChecker.validate(rule.getRule().getErrorCondition()) == null) {
          checkRule(validator, rule, findings);
        }
        else if (element instanceof ComputationElement computation && computation.getComputation() != null) {
          checkComputation(validator, computation, findings);
        }
      }
    }
    catch (KernelException e) {
      log.debug("Kernel could not validate the conditions of {}: {}", model.getId(), e.getMessage());
      return List.of();
    }
    catch (RuntimeException e) {
      log.warn("Kernel validation of the conditions of {} failed unexpectedly: {}", model.getId(), e.getMessage());
      return List.of();
    }
    return findings;
  }

  private static void checkRule(KernelRuleValidator validator, RuleElement rule, List<Finding> findings) {
    try {
      List<KernelProblem> problems = validator.validateCondition(rule.getId());
      if (!problems.isEmpty()) {
        findings.add(new Finding(rule.getId(), ElementProperty.RULE_PROPERTIES, "validation.kernelCondition.errorCondition",
            new Object[] {RuleConditionKernelCheck.describe(problems.get(0))}));
      }
    }
    catch (KernelException e) {
      log.debug("Kernel could not check rule {}: {}", rule.getId(), e.getMessage());
    }
  }

  private static void checkComputation(KernelRuleValidator validator, ComputationElement computation, List<Finding> findings) {
    try {
      var result = validator.validateComputation(computation.getId());
      for (String semantic : result.semanticErrors()) {
        findings.add(new Finding(computation.getId(), ElementProperty.COMPUTATION_PROPERTIES,
            "validation.kernelCondition.computation", new Object[] {semantic}));
      }
      for (KernelComputationProblem problem : result.parserErrors()) {
        String where = problem.message() + " (line " + problem.line() + ", column " + (problem.startColumn() + 1) + ")";
        if (problem.part() == KernelComputationProblem.Part.COMMON_PRECONDITION) {
          findings.add(new Finding(computation.getId(), ElementProperty.COMPUTATION_PROPERTIES,
              "validation.kernelCondition.commonPrecondition", new Object[] {where}));
        }
        else {
          String key = problem.part() == KernelComputationProblem.Part.PRECONDITION
              ? "validation.kernelCondition.precondition" : "validation.kernelCondition.operation";
          findings.add(new Finding(computation.getId(), ElementProperty.COMPUTATION_PROPERTIES, key,
              new Object[] {problem.alternativeIndex() + 1, where}));
        }
      }
    }
    catch (KernelException e) {
      log.debug("Kernel could not check computation {}: {}", computation.getId(), e.getMessage());
    }
  }

  private static boolean hasText(String text) {
    return text != null && !text.isBlank();
  }
}
