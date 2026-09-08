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

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * Port of the kernel's NumberFieldValueLimitRule (decompiled from kernel-md-model, EUPL-1.2 dual-licensed):
 * a number field's configured min/max may not exceed what the kernel's fixed-precision decimal storage can
 * represent (15 significant digits).
 */
public final class NumberFieldValueLimitValidator implements ModelValidator {

  private static final int MAX_DIGITS = 15;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel, context.otherDocumentModels());
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

    // A model's own type definitions aren't reachable through allElements() (they live in
    // content.typeDefinitions, not the modelRoot tree) - a standalone Type Definition Model has no
    // FieldElement anywhere, so without this second pass its own Number type definitions would never be
    // checked at all until some other model's field happens to reference them.
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
    int maxDecimalPlaces = numberType.getMaxFractionalDigits() == null ? 0 : numberType.getMaxFractionalDigits();
    double maxAllowedValue = Math.pow(10.0, MAX_DIGITS - maxDecimalPlaces) - Math.pow(10.0, -maxDecimalPlaces);
    if (numberType.getMaxValue() != null && numberType.getMaxValue() > maxAllowedValue) {
      errors.add(new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.numberFieldValueLimit.maxExceeded", elementId, numberType.getMaxValue(),
              printLimit(maxAllowedValue, maxDecimalPlaces)),
          Severity.ERROR.name()));
    }
    if (numberType.getMinValue() != null && Math.abs(numberType.getMinValue()) > maxAllowedValue) {
      errors.add(new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.numberFieldValueLimit.minExceeded", elementId, numberType.getMinValue(),
              printLimit(maxAllowedValue, maxDecimalPlaces)),
          Severity.ERROR.name()));
    }
  }

  private static String printLimit(double value, int maxDecimalPlaces) {
    DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
    df.setMaximumFractionDigits(maxDecimalPlaces);
    df.setMaximumIntegerDigits(MAX_DIGITS - maxDecimalPlaces);
    df.setRoundingMode(RoundingMode.DOWN);
    df.setGroupingUsed(false);
    DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance();
    symbols.setDecimalSeparator('.');
    df.setDecimalFormatSymbols(symbols);
    return df.format(value);
  }
}
