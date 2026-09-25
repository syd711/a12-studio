package de.a12.studio.modelsvalidation.validators.typesetting;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.typesettingmodel.TypesettingModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Ports the per-rule checks of the {@code print-typesetting} editor's {@code PreventLineBreakRuleValidator}
 * ({@code custom-validator.ts}); it reports at most one problem per rule, the first that applies:
 * <ul>
 *   <li>Character sequence: required, letters and hyphens only, at most 20 characters.</li>
 *   <li>Number unit: required, no digits/whitespace/special characters, at most 20 characters.</li>
 *   <li>Special pattern: required (one of the curated patterns must be chosen).</li>
 * </ul>
 * The checks apply to the value the editor shows (the word or unit, not the regex it is stored as), see
 * {@link TypesettingRuleRow#value()}. Uniqueness is a separate rule, see {@link TypesettingRuleDuplicateValidator}.
 */
public final class TypesettingRuleValueValidator implements ModelValidator {

  /** SME's {@code VALIDATOR_VALUE.maxLength}. */
  public static final int MAX_LENGTH = 20;

  private static final Pattern LETTERS_AND_HYPHENS = Pattern.compile("^[\\p{L}-]+$");

  // Everything except digits, whitespace and the symbols below: letters and unit symbols such as % $ are fine.
  private static final Pattern UNIT_CHARACTERS =
      Pattern.compile("^[^0-9\\s._\\-<>,;:'\"`=^*+?()\\[\\]{}\\\\|/:#!@&~]+$", Pattern.UNICODE_CHARACTER_CLASS);

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TypesettingModel typesettingModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (TypesettingRuleRow rule : TypesettingRuleRow.of(typesettingModel)) {
      String messageKey = findProblem(rule);
      if (messageKey != null) {
        errors.add(new ModelValidationError(model, TypesettingElementIds.rule(rule.index()),
            ValidationMessages.get(messageKey, rule.row(), MAX_LENGTH), Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static String findProblem(TypesettingRuleRow rule) {
    String value = rule.value();
    return switch (rule.type()) {
      case CHARACTER_SEQUENCE -> {
        if (value.isEmpty()) {
          yield "validation.typesetting.characterRequired";
        }
        if (!LETTERS_AND_HYPHENS.matcher(value).matches()) {
          yield "validation.typesetting.characterInvalid";
        }
        yield value.length() > MAX_LENGTH ? "validation.typesetting.characterTooLong" : null;
      }
      case NUMBER_UNIT -> {
        if (value.isEmpty()) {
          yield "validation.typesetting.unitRequired";
        }
        if (!UNIT_CHARACTERS.matcher(value).matches()) {
          yield "validation.typesetting.unitInvalid";
        }
        yield value.length() > MAX_LENGTH ? "validation.typesetting.unitTooLong" : null;
      }
      case SPECIAL_PATTERN -> value.isEmpty() ? "validation.typesetting.specialRequired" : null;
    };
  }
}
