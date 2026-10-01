package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Ports the kernel's {@code DomainField.json} rule {@code INTERPRETATION_OF_YEAR_INVALID}: a Date Range's
 * {@code interpretationOfYear} may only be something other than "standard" (absent) when the model has a
 * {@link de.a12.studio.models.documentmodel.ModelInfo#getBaseYear() base year}. The companion rule
 * {@code INTERPRETATION_OF_YEAR_MISSING} keys off the legacy format name {@code "DD.MM-DD.MM"}, which neither
 * SME's nor a12-studio's date range formats produce ({@code MM-dd} maps to {@code MM-DD/MM-DD}), so it is not
 * ported.
 */
public final class DateInterpretationOfYearValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel) || documentModel.getContent() == null) {
      return List.of();
    }
    if (documentModel.getContent().getModelInfo() != null && documentModel.getContent().getModelInfo().getBaseYear() != null) {
      return List.of();
    }

    ElementIndex index = context.elementIndex();
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (element instanceof FieldElement field && field.getField() != null) {
        check(model, field.getId(), index.effectiveFieldType(field.getField().getFieldType()), errors);
      }
    }
    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        check(model, typeDefinition.getId(), typeDefinition.getFieldType(), errors);
      }
    }
    return errors;
  }

  private static void check(A12Model<?> model, String elementId, FieldType fieldType, List<ModelValidationError> errors) {
    if (!(fieldType instanceof DateRangeFieldType range) || range.getDateRangeType() == null) {
      return;
    }
    String interpretation = range.getDateRangeType().getInterpretationOfYear();
    if (interpretation != null && !interpretation.isBlank()) {
      errors.add(new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.dateInterpretationOfYear.noBaseYear", elementId), Severity.ERROR.name()));
    }
  }
}
