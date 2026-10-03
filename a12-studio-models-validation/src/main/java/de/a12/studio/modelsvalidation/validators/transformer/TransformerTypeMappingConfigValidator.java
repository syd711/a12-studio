package de.a12.studio.modelsvalidation.validators.transformer;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.transformermodel.A12DataType;
import de.a12.studio.models.transformermodel.NumberTypeConfig;
import de.a12.studio.models.transformermodel.StringTypeConfig;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.models.transformermodel.TypeMappingEntry;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * The rules inside a Type Mapping's {@code StringType}/{@code NumberType} configuration, ported from SME's
 * {@code TransformerConfigModel} (29.4.0). String rules apply to {@code a12Type == "StringType"}, Number rules to
 * every type whose super type is Number, exactly as the rules' conditions say:
 * <ul>
 *   <li>String: {@code lineBreakAndMaxLength1Invalid}, {@code a12LineBreakAndAlphSortingInvalid},
 *       {@code minLengthBiggerMaxLength}</li>
 *   <li>Number: {@code fractDigitsInvalid}, {@code minValueBiggerThanMaxValue}, {@code minLengthBiggerMaxLength},
 *       {@code maxLengthTooSmall} (the length cannot hold the minimum decimal places: sign, point and one integer
 *       place need {@code minFractionalDigits + 2}), {@code a12AmountAndInvalidFractDigits}</li>
 *   <li>The value ranges of the fields' type definitions: lengths 1..99999, decimal places 0..14, integer places
 *       at least 1, a pattern of at most 1000 characters, min/max value with at most 14 decimal places</li>
 * </ul>
 * Rule errors are reported on the field SME's rule points at ({@code ../lineBreaksPermitted}, {@code ../minLength} ...).
 */
public final class TransformerTypeMappingConfigValidator implements ModelValidator {

  private static final int MAX_VALUE_DECIMAL_PLACES = 14;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TransformerModel transformerModel) || transformerModel.getContent() == null) {
      return List.of();
    }
    List<TypeMappingEntry> entries = transformerModel.getContent().getTypeMappingOrEmpty();
    List<ModelValidationError> errors = new ArrayList<>();
    for (int index = 0; index < entries.size(); index++) {
      TypeMappingEntry entry = entries.get(index);
      A12DataType dataType = entry.getDataType();
      if (entry.getStringType() != null) {
        checkStringRanges(model, index, entry.getStringType(), errors);
        if (dataType == A12DataType.STRING) {
          checkStringRules(model, index, entry.getStringType(), errors);
        }
      }
      if (entry.getNumberType() != null) {
        checkNumberRanges(model, index, entry.getNumberType(), errors);
        if (dataType != null && dataType.acceptsNumberConfig()) {
          checkNumberRules(model, index, dataType, entry.getNumberType(), errors);
        }
      }
    }
    return errors;
  }

  private static void checkStringRules(A12Model<?> model, int index, StringTypeConfig config, List<ModelValidationError> errors) {
    boolean lineBreaks = Boolean.TRUE.equals(config.getLineBreaksPermitted());
    if (lineBreaks && config.getMaxLength() != null && config.getMaxLength() == 1) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "StringType/lineBreaksPermitted"),
          ValidationMessages.get("validation.transformer.string.lineBreaksWithMaxLength1")));
    }
    if (lineBreaks && Boolean.TRUE.equals(config.getAlphabeticalSorting())) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "StringType/lineBreaksPermitted"),
          ValidationMessages.get("validation.transformer.string.lineBreaksWithSorting")));
    }
    if (config.getMinLength() != null && config.getMaxLength() != null && config.getMinLength() > config.getMaxLength()) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "StringType/minLength"),
          ValidationMessages.get("validation.transformer.string.minLengthBiggerMaxLength", config.getMinLength(), config.getMaxLength())));
    }
  }

  private static void checkNumberRules(A12Model<?> model, int index, A12DataType dataType, NumberTypeConfig config,
      List<ModelValidationError> errors) {
    Integer minFraction = config.getMinFractionalDigits();
    Integer maxFraction = config.getMaxFractionalDigits();
    if (minFraction != null && maxFraction != null && minFraction > maxFraction) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "NumberType/maxFractionalDigits"),
          ValidationMessages.get("validation.transformer.number.fractionalDigitsInvalid", maxFraction, minFraction)));
    }
    if (config.getMinValue() != null && config.getMaxValue() != null && config.getMinValue().compareTo(config.getMaxValue()) > 0) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "NumberType/minValue"),
          ValidationMessages.get("validation.transformer.number.minValueBiggerThanMaxValue",
              config.getMinValue().toPlainString(), config.getMaxValue().toPlainString())));
    }
    if (config.getMinLength() != null && config.getMaxLength() != null && config.getMinLength() > config.getMaxLength()) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "NumberType/minLength"),
          ValidationMessages.get("validation.transformer.number.minLengthBiggerMaxLength", config.getMinLength(), config.getMaxLength())));
    }
    if (config.getMaxLength() != null && minFraction != null && minFraction > 0 && config.getMaxLength() < minFraction + 2) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "NumberType/maxLength"),
          ValidationMessages.get("validation.transformer.number.maxLengthTooSmall")));
    }
    if (dataType == A12DataType.NUMBER && NumberTypeConfig.TRAIT_AMOUNT.equals(config.getTrait())
        && minFraction != null && maxFraction != null
        && (!minFraction.equals(maxFraction) || (minFraction != 2 && minFraction != 0) || (maxFraction != 2 && maxFraction != 0))) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "NumberType/minFractionalDigits"),
          ValidationMessages.get("validation.transformer.number.amountFractionalDigits")));
    }
  }

  private static void checkStringRanges(A12Model<?> model, int index, StringTypeConfig config, List<ModelValidationError> errors) {
    checkLength(model, index, "StringType/minLength", "validation.transformer.field.minLength", config.getMinLength(), errors);
    checkLength(model, index, "StringType/maxLength", "validation.transformer.field.maxLength", config.getMaxLength(), errors);
    if (config.getPattern() != null && config.getPattern().length() > StringTypeConfig.MAX_PATTERN_LENGTH) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "StringType/pattern"),
          ValidationMessages.get("validation.transformer.string.patternTooLong", StringTypeConfig.MAX_PATTERN_LENGTH)));
    }
  }

  private static void checkNumberRanges(A12Model<?> model, int index, NumberTypeConfig config, List<ModelValidationError> errors) {
    checkLength(model, index, "NumberType/minLength", "validation.transformer.field.minLength", config.getMinLength(), errors);
    checkLength(model, index, "NumberType/maxLength", "validation.transformer.field.maxLength", config.getMaxLength(), errors);
    checkFractionalDigits(model, index, "NumberType/minFractionalDigits", "validation.transformer.field.minFractionalDigits",
        config.getMinFractionalDigits(), errors);
    checkFractionalDigits(model, index, "NumberType/maxFractionalDigits", "validation.transformer.field.maxFractionalDigits",
        config.getMaxFractionalDigits(), errors);
    if (config.getMaxIntegerDigits() != null && config.getMaxIntegerDigits() < 1) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, "NumberType/maxIntegerDigits"),
          ValidationMessages.get("validation.transformer.number.maxIntegerDigitsInvalid")));
    }
    checkValueScale(model, index, "NumberType/minValue", "validation.transformer.field.minValue", config.getMinValue(), errors);
    checkValueScale(model, index, "NumberType/maxValue", "validation.transformer.field.maxValue", config.getMaxValue(), errors);
  }

  private static void checkLength(A12Model<?> model, int index, String field, String labelKey, Integer value,
      List<ModelValidationError> errors) {
    if (value != null && (value < StringTypeConfig.MIN_LENGTH || value > StringTypeConfig.MAX_LENGTH)) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, field), ValidationMessages.get(
          "validation.transformer.range.integer", ValidationMessages.get(labelKey), StringTypeConfig.MIN_LENGTH, StringTypeConfig.MAX_LENGTH)));
    }
  }

  private static void checkFractionalDigits(A12Model<?> model, int index, String field, String labelKey, Integer value,
      List<ModelValidationError> errors) {
    if (value != null && (value < NumberTypeConfig.MIN_FRACTIONAL_DIGITS || value > NumberTypeConfig.MAX_FRACTIONAL_DIGITS)) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, field), ValidationMessages.get("validation.transformer.range.integer",
          ValidationMessages.get(labelKey), NumberTypeConfig.MIN_FRACTIONAL_DIGITS, NumberTypeConfig.MAX_FRACTIONAL_DIGITS)));
    }
  }

  private static void checkValueScale(A12Model<?> model, int index, String field, String labelKey, BigDecimal value,
      List<ModelValidationError> errors) {
    if (value != null && value.stripTrailingZeros().scale() > MAX_VALUE_DECIMAL_PLACES) {
      errors.add(error(model, TransformerElementIds.typeMapping(index, field), ValidationMessages.get(
          "validation.transformer.range.decimalPlaces", ValidationMessages.get(labelKey), MAX_VALUE_DECIMAL_PLACES)));
    }
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
