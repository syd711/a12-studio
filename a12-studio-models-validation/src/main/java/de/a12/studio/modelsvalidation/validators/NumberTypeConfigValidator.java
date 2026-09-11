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
 * FRACT_DIGITS_INVALID}). Runs both against fields and, since a Type Definition Model's own type definitions
 * aren't reachable through {@link ElementIndex#allElements()}, against a model's own {@code typeDefinitions}
 * directly - see {@link NumberFieldValueLimitValidator} for the same dual-pass shape.
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
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE, message, Severity.ERROR.name());
  }
}
