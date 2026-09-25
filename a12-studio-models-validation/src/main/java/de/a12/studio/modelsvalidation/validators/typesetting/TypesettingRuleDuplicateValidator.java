package de.a12.studio.modelsvalidation.validators.typesetting;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ports {@code PreventLineBreakRuleValidator.checkDuplication} of the {@code print-typesetting} editor: every
 * rule whose stored pattern occurs more than once is reported - all its occurrences, not just the later ones,
 * as SME does - so two rows the user is looking at are both marked. Compares the stored pattern, across all
 * three rule kinds (they cannot collide in practice, but two still-empty rows of one kind do).
 */
public final class TypesettingRuleDuplicateValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TypesettingModel typesettingModel)) {
      return List.of();
    }
    List<TypesettingRuleRow> rules = TypesettingRuleRow.of(typesettingModel);
    Map<String, Integer> occurrences = new HashMap<>();
    for (TypesettingRuleRow rule : rules) {
      occurrences.merge(String.valueOf(rule.pattern()), 1, Integer::sum);
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (TypesettingRuleRow rule : rules) {
      if (occurrences.get(String.valueOf(rule.pattern())) > 1) {
        errors.add(new ModelValidationError(model, TypesettingElementIds.rule(rule.index()),
            ValidationMessages.get(messageKey(rule), rule.row()), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static String messageKey(TypesettingRuleRow rule) {
    return switch (rule.type()) {
      case CHARACTER_SEQUENCE -> "validation.typesetting.duplicateCharacter";
      case NUMBER_UNIT -> "validation.typesetting.duplicateUnit";
      case SPECIAL_PATTERN -> "validation.typesetting.duplicateSpecial";
    };
  }
}
