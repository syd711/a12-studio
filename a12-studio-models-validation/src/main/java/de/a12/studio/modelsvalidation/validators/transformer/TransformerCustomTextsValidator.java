package de.a12.studio.modelsvalidation.validators.transformer;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.Locale;
import de.a12.studio.models.transformermodel.EnumLabel;
import de.a12.studio.models.transformermodel.PatternError;
import de.a12.studio.models.transformermodel.PatternErrorAction;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.IntFunction;

/**
 * The Custom Texts tab, ported from SME's {@code TransformerConfigModel} (29.4.0):
 * <ul>
 *   <li>String Pattern Error Messages: {@code pattern} is required and {@code patternNotUnique};
 *       {@code replaceRequiresReplacement} (action {@code REPLACE} needs a {@code replacement}); every message's locale
 *       is required and must be one of the model's locales ({@code localeIsNotSupported}, only checked while the
 *       model has at least one locale)</li>
 *   <li>Enumeration Display Texts: {@code value} is required; {@code onlyOneEnumerationTypeSpecifier} (an enumeration
 *       type id and a field path exclude each other); {@code enumLabelNotUnique} (the value, type id and field path
 *       combination occurs once); every display text's locale is required and must be a locale of the model</li>
 * </ul>
 */
public final class TransformerCustomTextsValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TransformerModel transformerModel) || transformerModel.getContent() == null) {
      return List.of();
    }
    Set<String> supportedLocales = new HashSet<>();
    for (Locale locale : model.getLocales()) {
      if (locale.getCode() != null) {
        supportedLocales.add(locale.getCode());
      }
    }
    List<ModelValidationError> errors = new ArrayList<>();
    checkPatternErrors(model, transformerModel.getContent().getPatternErrorsOrEmpty(), supportedLocales, errors);
    checkEnumLabels(model, transformerModel.getContent().getEnumLabelsOrEmpty(), supportedLocales, errors);
    return errors;
  }

  private static void checkPatternErrors(A12Model<?> model, List<PatternError> patternErrors, Set<String> supportedLocales,
      List<ModelValidationError> errors) {
    Set<String> seenPatterns = new HashSet<>();
    for (int index = 0; index < patternErrors.size(); index++) {
      PatternError patternError = patternErrors.get(index);

      String pattern = patternError.getPattern();
      if (pattern == null || pattern.isBlank()) {
        errors.add(error(model, TransformerElementIds.patternError(index, "pattern"),
            ValidationMessages.get("validation.transformer.patternError.patternMissing")));
      }
      else if (!seenPatterns.add(pattern)) {
        errors.add(error(model, TransformerElementIds.patternError(index, "pattern"),
            ValidationMessages.get("validation.transformer.patternError.patternDuplicate", pattern)));
      }

      if (patternError.getEffectiveAction() == PatternErrorAction.REPLACE
          && (patternError.getReplacement() == null || patternError.getReplacement().isEmpty())) {
        errors.add(error(model, TransformerElementIds.patternError(index, "replacement"),
            ValidationMessages.get("validation.transformer.patternError.replacementMissing")));
      }

      int patternErrorIndex = index;
      checkLocales(model, patternError.getErrorsOrEmpty(), supportedLocales, errors,
          errorIndex -> TransformerElementIds.patternErrorLocale(patternErrorIndex, errorIndex),
          "validation.transformer.patternError.localeMissing");
    }
  }

  private static void checkEnumLabels(A12Model<?> model, List<EnumLabel> enumLabels, Set<String> supportedLocales,
      List<ModelValidationError> errors) {
    Set<List<String>> seenCombinations = new HashSet<>();
    for (int index = 0; index < enumLabels.size(); index++) {
      EnumLabel enumLabel = enumLabels.get(index);

      String value = enumLabel.getValue();
      if (value == null || value.isBlank()) {
        errors.add(error(model, TransformerElementIds.enumLabel(index, "value"),
            ValidationMessages.get("validation.transformer.enumLabel.valueMissing")));
      }
      else {
        // SME: RepetitionNotUnique('EnumLabels'/'value', 'EnumLabels'/'typeDefinitionId', 'EnumLabels'/'enumFieldPath')
        List<String> combination = List.of(value, Objects.toString(enumLabel.getTypeDefinitionId(), ""),
            Objects.toString(enumLabel.getEnumFieldPath(), ""));
        if (!seenCombinations.add(combination)) {
          errors.add(error(model, TransformerElementIds.enumLabel(index, "value"),
              ValidationMessages.get("validation.transformer.enumLabel.notUnique")));
        }
      }

      if (isFilled(enumLabel.getTypeDefinitionId()) && isFilled(enumLabel.getEnumFieldPath())) {
        errors.add(error(model, TransformerElementIds.enumLabel(index, "typeDefinitionId"),
            ValidationMessages.get("validation.transformer.enumLabel.typeAndPathExclusive")));
      }

      int enumLabelIndex = index;
      checkLocales(model, enumLabel.getReplacementsOrEmpty(), supportedLocales, errors,
          replacementIndex -> TransformerElementIds.enumLabelLocale(enumLabelIndex, replacementIndex),
          "validation.transformer.enumLabel.localeMissing");
    }
  }

  private static void checkLocales(A12Model<?> model, List<Label> labels, Set<String> supportedLocales,
      List<ModelValidationError> errors, IntFunction<String> elementId, String missingKey) {
    for (int index = 0; index < labels.size(); index++) {
      String locale = labels.get(index).getLocale();
      if (locale == null || locale.isBlank()) {
        errors.add(error(model, elementId.apply(index), ValidationMessages.get(missingKey)));
      }
      else if (!supportedLocales.isEmpty() && !supportedLocales.contains(locale)) {
        errors.add(error(model, elementId.apply(index), ValidationMessages.get("validation.transformer.localeNotSupported", locale)));
      }
    }
  }

  private static boolean isFilled(String value) {
    return value != null && !value.isBlank();
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
