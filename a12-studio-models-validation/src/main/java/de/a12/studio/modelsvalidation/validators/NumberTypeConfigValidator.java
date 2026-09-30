package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.NumberFieldType;
import de.a12.studio.models.documentmodel.NumberTypeOptions;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Structural sanity checks for a Number field type's configuration, ported from SME/kernel's
 * {@code DomainField.json} Number-section rules ({@code MIN_VALUE_BIGGER_THAN_MAX_VALUE}, a fraction-digit
 * range check covering {@code MIN_FRACT_DIGITS_MISSING}/{@code MAX_FRACT_DIGITS_MISSING}/{@code
 * FRACT_DIGITS_INVALID}, and {@code A12_AMOUNT_AND_INVALID_FRACT_DIGITS}: an {@code Amount}-trait field's
 * {@code minFractionalDigits}/{@code maxFractionalDigits} must be equal and either both 0 or both 2). Runs both
 * against fields and, since a Type Definition Model's own type definitions aren't reachable through {@link
 * ElementIndex#allElements()}, against a model's own {@code typeDefinitions} directly - see {@link
 * NumberFieldValueLimitValidator} for the same dual-pass shape.
 *
 * <p>Re-verified 2026-09-30 directly against {@code DomainField.json} in {@code C:\workspace\sme} (see
 * "Document Model: gap review", gap 11): the doc's prior "{@code positivesOnly}-vs-negative-{@code minValue}
 * conflict" and "{@code maxIntegerDigits}-vs-{@code maxValue}-digit-count check" items were speculative, not
 * grounded in a real SME rule - no such cross-field rule exists in SME's Number section at all (only the ones
 * ported here); not implemented, and removed from the gap list.
 */
public final class NumberTypeConfigValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = context.elementIndex();
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (!(element instanceof FieldElement field) || field.getField() == null) {
        continue;
      }
      FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
      if (effectiveType instanceof NumberFieldType numberFieldType && numberFieldType.getNumberType() != null) {
        checkNumberType(model, field.getId(), numberFieldType.getNumberType(), errors);
      }
    }

    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        if (typeDefinition.getFieldType() instanceof NumberFieldType numberFieldType && numberFieldType.getNumberType() != null) {
          checkNumberType(model, typeDefinition.getId(), numberFieldType.getNumberType(), errors);
        }
      }
    }
    return errors;
  }

  private static final String TRAIT_AMOUNT = "amount";

  private static void checkNumberType(A12Model<?> model, String elementId, NumberTypeOptions numberType,
      List<ModelValidationError> errors) {
    if (numberType.getMinValue() != null && numberType.getMaxValue() != null
        && numberType.getMinValue() > numberType.getMaxValue()) {
      errors.add(error(model, elementId, ValidationMessages.get("validation.numberTypeConfig.minValueBiggerMaxValue", elementId)));
    }
    if (numberType.getMinFractionalDigits() != null && numberType.getMaxFractionalDigits() != null
        && numberType.getMinFractionalDigits() > numberType.getMaxFractionalDigits()) {
      errors.add(error(model, elementId,
          ValidationMessages.get("validation.numberTypeConfig.minFractionalDigitsBiggerMaxFractionalDigits", elementId)));
    }
    if (TRAIT_AMOUNT.equals(numberType.getTrait())
        && numberType.getMinFractionalDigits() != null && numberType.getMaxFractionalDigits() != null
        && !isValidAmountFractionalDigits(numberType.getMinFractionalDigits(), numberType.getMaxFractionalDigits())) {
      errors.add(error(model, elementId, ValidationMessages.get("validation.numberTypeConfig.amountInvalidFractionalDigits", elementId)));
    }
  }

  private static boolean isValidAmountFractionalDigits(int minFractionalDigits, int maxFractionalDigits) {
    return minFractionalDigits == maxFractionalDigits && (minFractionalDigits == 0 || minFractionalDigits == 2);
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE, message, Severity.ERROR.name());
  }
}
