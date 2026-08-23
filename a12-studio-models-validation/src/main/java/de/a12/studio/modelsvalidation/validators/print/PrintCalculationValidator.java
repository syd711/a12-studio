package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.printmodel.Calculation;
import de.a12.studio.models.printmodel.ComputationStep;
import de.a12.studio.models.printmodel.PrintCalculationElement;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Calculation elements need a name and at least one non-empty computation operation, and their
 * referenced Document Model must exist (print engine "Internal Validation": required fields).
 */
public final class PrintCalculationValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/elementDefinitions/calculation";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof PrintModel printModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (PrintElementDefinition definition : printModel.getContent().getElementDefinitions()) {
      if (!(definition instanceof PrintCalculationElement element) || element.getCalculation() == null) {
        continue;
      }
      Calculation calculation = element.getCalculation();
      if (calculation.getName() == null || calculation.getName().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.printCalculation.missingName", definition.getId()), Severity.ERROR.name()));
      }
      boolean anyOperation = calculation.getComputationAlternatives().stream()
          .map(ComputationStep::getOperation)
          .anyMatch(operation -> operation != null && !operation.isBlank());
      if (!anyOperation) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.printCalculation.missingOperation", calculation.getName()), Severity.ERROR.name()));
      }
      if (calculation.getModel() != null && !calculation.getModel().isBlank()
          && context.findOtherDocumentModel(calculation.getModel()) == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.printCalculation.missingModel", calculation.getModel(), calculation.getName()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
