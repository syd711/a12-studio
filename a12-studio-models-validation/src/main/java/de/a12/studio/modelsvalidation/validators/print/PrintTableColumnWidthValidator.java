package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.printmodel.GenericPrintElement;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Table and Listing column widths must be integers between 1 and 100 and must not sum to more than 100
 * (print modeling docs). Table/Listing elements have no typed DTO, so the columns are read defensively
 * from the generic element's extras.
 */
public final class PrintTableColumnWidthValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/elementDefinitions/columns";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof PrintModel printModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (PrintElementDefinition definition : printModel.getContent().getElementDefinitions()) {
      if (!(definition instanceof GenericPrintElement generic)) {
        continue;
      }
      String type = generic.getType();
      if (!"Table".equalsIgnoreCase(type) && !"Listing".equalsIgnoreCase(type)) {
        continue;
      }
      if (!(generic.getExtras().get("columns") instanceof List<?> columns)) {
        continue;
      }
      int sum = 0;
      boolean anyWidth = false;
      for (Object column : columns) {
        if (!(column instanceof Map<?, ?> columnMap) || !(columnMap.get("width") instanceof Number width)) {
          continue;
        }
        anyWidth = true;
        double value = width.doubleValue();
        if (value != Math.floor(value) || value < 1 || value > 100) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "validation.the" + type + " element \"" + definition.getId()
                  + "\" has a column width of " + width + "; widths must be integers between 1 and 100.",
              Severity.ERROR.name()));
        }
        sum += (int) value;
      }
      if (anyWidth && sum > 100) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.the_column_widths_of_the" + type + " element \"" + definition.getId()
                + "\" sum to " + sum + "; the sum must not exceed 100.", Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
