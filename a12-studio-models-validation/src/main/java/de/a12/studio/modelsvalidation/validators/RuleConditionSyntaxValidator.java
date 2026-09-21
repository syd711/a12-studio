package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.models.documentmodel.ComputationConfig;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.RuleConfig;
import de.a12.studio.models.documentmodel.RuleElement;
import de.a12.studio.models.documentmodel.rulelang.RuleLanguageSyntaxChecker;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * The condition text of every Rule ({@code errorCondition}) and Computation ({@code commonPrecondition} and each
 * alternative's {@code precondition}/{@code operation}) must be syntactically valid Rule/Computation condition
 * language. The property editors already show {@link RuleLanguageSyntaxChecker}'s message while typing; this
 * makes the same check part of the model's validation, so a broken condition also shows up in the validation
 * list and on the element's tree row, and for a model that was edited elsewhere. Syntax only - whether the
 * paths and functions in the condition resolve against the Document Model stays with the kernel (see
 * {@code docs/sme-reference-comparison.md}, "Kernel dependency spike"). Blank text is not this rule's business,
 * exactly as in the editors.
 */
public final class RuleConditionSyntaxValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : context.elementIndex().allElements()) {
      if (element instanceof RuleElement rule && rule.getRule() != null) {
        checkRule(model, rule, errors);
      }
      else if (element instanceof ComputationElement computation && computation.getComputation() != null) {
        checkComputation(model, computation, errors);
      }
    }
    return errors;
  }

  private static void checkRule(A12Model<?> model, RuleElement element, List<ModelValidationError> errors) {
    RuleConfig rule = element.getRule();
    String problem = problemOf(rule.getErrorCondition());
    if (problem != null) {
      errors.add(error(model, element, ElementProperty.RULE_PROPERTIES,
          ValidationMessages.get("validation.ruleConditionSyntax.errorCondition", problem)));
    }
  }

  private static void checkComputation(A12Model<?> model, ComputationElement element, List<ModelValidationError> errors) {
    ComputationConfig computation = element.getComputation();
    String problem = problemOf(computation.getCommonPrecondition());
    if (problem != null) {
      errors.add(error(model, element, ElementProperty.COMPUTATION_PROPERTIES,
          ValidationMessages.get("validation.ruleConditionSyntax.commonPrecondition", problem)));
    }
    List<ComputationAlternative> alternatives = computation.getComputationAlternatives();
    for (int i = 0; i < alternatives.size(); i++) {
      String precondition = problemOf(alternatives.get(i).getPrecondition());
      if (precondition != null) {
        errors.add(error(model, element, ElementProperty.COMPUTATION_PROPERTIES,
            ValidationMessages.get("validation.ruleConditionSyntax.precondition", i + 1, precondition)));
      }
      String operation = problemOf(alternatives.get(i).getOperation());
      if (operation != null) {
        errors.add(error(model, element, ElementProperty.COMPUTATION_PROPERTIES,
            ValidationMessages.get("validation.ruleConditionSyntax.operation", i + 1, operation)));
      }
    }
  }

  private static String problemOf(String text) {
    return text == null || text.isBlank() ? null : RuleLanguageSyntaxChecker.validate(text);
  }

  private static ModelValidationError error(A12Model<?> model, Element element, String property, String message) {
    return new ModelValidationError(model, element.getId(), property, message, Severity.ERROR.name());
  }
}
