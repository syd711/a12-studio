package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DateFragmentFieldType;
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
 * {@code youngerThan1900Check} only makes sense when the configured format actually has a year component -
 * ported from SME/kernel's {@code DomainField.json} rule {@code YOUNGER1900_CHECK_INVALID}
 * ({@code FieldFilled(younger1900) And [format->containsYear] == "False"}). Scoped to {@link
 * DateFragmentFieldType}/{@link DateRangeFieldType} only, the two a12-studio field types that actually expose
 * this option ({@code DateFieldType}/{@code DateTimeFieldType}/{@code TimeFieldType} have no such field - see
 * "Document Model: gap review", gap 10, for why SME's broader "any Date-superType field" condition doesn't
 * translate 1:1 onto a12-studio's split field-type hierarchy). SME's {@code optionalDateType}/{@code
 * interpretationOfYear} cross-field rules from the same gap are deliberately not ported here - the former has
 * no equivalent field on {@code DateRangeTypeOptions} and doesn't correspond to a12-studio's own, differently-
 * shaped {@link DateFragmentFieldType} concept, and the latter needs a "model is year-based" input a12-studio's
 * {@code ModelInfo} doesn't carry (SME's {@code ModelInfo.baseYear}, entirely unmodeled here) - both left open,
 * see the doc. Runs against {@link DateRangeFieldType} even though {@code DataTypeDateRangeConfigurationPanelController}
 * itself has no UI for {@code youngerThan1900Check} (an SME "expert" property never exposed by either editor),
 * matching {@link DateFormatConfigValidator}'s own defense-in-depth reasoning for state only reachable by a
 * hand-edited or imported file - and against a model's own {@code typeDefinitions} directly, since a Type
 * Definition Model's type definitions aren't reachable through {@link ElementIndex#allElements()}.
 */
public final class DateYounger1900ConfigValidator implements ModelValidator {

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
      checkYounger1900(model, field.getId(), index.effectiveFieldType(field.getField().getFieldType()), errors);
    }

    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        checkYounger1900(model, typeDefinition.getId(), typeDefinition.getFieldType(), errors);
      }
    }
    return errors;
  }

  private static void checkYounger1900(A12Model<?> model, String elementId, FieldType fieldType, List<ModelValidationError> errors) {
    boolean younger1900;
    String format;
    if (fieldType instanceof DateFragmentFieldType dateFragmentFieldType && dateFragmentFieldType.getDateFragmentType() != null) {
      younger1900 = Boolean.TRUE.equals(dateFragmentFieldType.getDateFragmentType().getYoungerThan1900Check());
      format = dateFragmentFieldType.getDateFragmentType().getFormatOfFragment();
    }
    else if (fieldType instanceof DateRangeFieldType dateRangeFieldType && dateRangeFieldType.getDateRangeType() != null) {
      younger1900 = Boolean.TRUE.equals(dateRangeFieldType.getDateRangeType().getYoungerThan1900Check());
      format = dateRangeFieldType.getDateRangeType().getFormat();
    }
    else {
      return;
    }
    if (younger1900 && !containsYear(format)) {
      errors.add(new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.dateYounger1900Config.noYearInFormat", elementId), Severity.ERROR.name()));
    }
  }

  private static boolean containsYear(String format) {
    return format != null && format.contains("yyyy");
  }
}
