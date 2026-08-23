package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.NumberFieldType;
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
      if (!(effectiveType instanceof NumberFieldType numberFieldType) || numberFieldType.getNumberType() == null) {
        continue;
      }
      var numberType = numberFieldType.getNumberType();
      int maxDecimalPlaces = numberType.getMaxFractionalDigits() == null ? 0 : numberType.getMaxFractionalDigits();
      double maxAllowedValue = Math.pow(10.0, MAX_DIGITS - maxDecimalPlaces) - Math.pow(10.0, -maxDecimalPlaces);
      if (numberType.getMaxValue() != null && numberType.getMaxValue() > maxAllowedValue) {
        errors.add(new ModelValidationError(model, field.getId(), ElementProperty.DATA_TYPE,
            ValidationMessages.get("validation.numberFieldValueLimit.maxExceeded", field.getId(), numberType.getMaxValue(),
                printLimit(maxAllowedValue, maxDecimalPlaces)),
            Severity.ERROR.name()));
      }
      if (numberType.getMinValue() != null && Math.abs(numberType.getMinValue()) > maxAllowedValue) {
        errors.add(new ModelValidationError(model, field.getId(), ElementProperty.DATA_TYPE,
            ValidationMessages.get("validation.numberFieldValueLimit.minExceeded", field.getId(), numberType.getMinValue(),
                printLimit(maxAllowedValue, maxDecimalPlaces)),
            Severity.ERROR.name()));
      }
    }
    return errors;
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
