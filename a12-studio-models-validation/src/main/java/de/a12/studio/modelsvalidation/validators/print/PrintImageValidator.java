package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.printmodel.GenericPrintElement;
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
 * Image elements need an alternative text (accessibility requirement of the print engine, mandatory
 * for PDF/UA compliance). Image elements have no typed DTO, so the property is read defensively from
 * the generic element's extras.
 */
public final class PrintImageValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/elementDefinitions/image";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof PrintModel printModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (PrintElementDefinition definition : printModel.getContent().getElementDefinitions()) {
      if (!(definition instanceof GenericPrintElement generic) || !"Image".equalsIgnoreCase(generic.getType())) {
        continue;
      }
      Object alternativeText = generic.getExtras().get("alternativeText");
      if (alternativeText == null || String.valueOf(alternativeText).isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.printImage.missingAlternativeText", definition.getId()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
