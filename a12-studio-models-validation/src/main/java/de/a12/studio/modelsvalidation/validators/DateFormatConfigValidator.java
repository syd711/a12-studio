package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DateFieldType;
import de.a12.studio.models.documentmodel.DateFragmentFieldType;
import de.a12.studio.models.documentmodel.DateRangeFieldType;
import de.a12.studio.models.documentmodel.DateTimeFieldType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.TimeFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * Every date-family field type must specify a format, ported from SME/kernel's {@code DomainField.json}
 * rules {@code FORMAT_MISSING}/{@code FORMAT_DATE_RANGE_MISSING}. The Java data classes default {@code
 * DateTypeOptions.format} to {@code "yyyy-MM-dd"} and the property panels auto-select the first preset for
 * every other date-family type as soon as it's chosen, so this is defense-in-depth for a blank format
 * reaching disk some other way (hand-edited JSON, an import path) rather than something the UI normally
 * allows. Runs both against fields and, since a Type Definition Model's own type definitions aren't
 * reachable through {@link ElementIndex#allElements()}, against a model's own {@code typeDefinitions}
 * directly.
 */
public final class DateFormatConfigValidator implements ModelValidator {

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
      checkFormat(model, field.getId(), index.effectiveFieldType(field.getField().getFieldType()), errors);
    }

    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        checkFormat(model, typeDefinition.getId(), typeDefinition.getFieldType(), errors);
      }
    }
    return errors;
  }

  private static void checkFormat(A12Model<?> model, String elementId, FieldType fieldType, List<ModelValidationError> errors) {
    String format = null;
    if (fieldType instanceof DateFieldType dateFieldType && dateFieldType.getDateType() != null) {
      format = dateFieldType.getDateType().getFormat();
    } else if (fieldType instanceof DateTimeFieldType dateTimeFieldType && dateTimeFieldType.getDateTimeType() != null) {
      format = dateTimeFieldType.getDateTimeType().getFormat();
    } else if (fieldType instanceof TimeFieldType timeFieldType && timeFieldType.getTimeType() != null) {
      format = timeFieldType.getTimeType().getFormat();
    } else if (fieldType instanceof DateFragmentFieldType dateFragmentFieldType && dateFragmentFieldType.getDateFragmentType() != null) {
      format = dateFragmentFieldType.getDateFragmentType().getFormatOfFragment();
    } else if (fieldType instanceof DateRangeFieldType dateRangeFieldType && dateRangeFieldType.getDateRangeType() != null) {
      format = dateRangeFieldType.getDateRangeType().getFormat();
    } else {
      return;
    }
    if (format == null || format.isBlank()) {
      errors.add(new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.dateFormatConfig.missing", elementId), Severity.ERROR.name()));
    }
  }
}
