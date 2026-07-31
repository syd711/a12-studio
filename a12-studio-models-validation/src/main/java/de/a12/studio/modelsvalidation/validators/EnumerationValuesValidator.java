package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Wherever the {@code DataTypeEnumerationConfigurationPanelController} editor is shown - a field or type
 * definition whose own field type is directly an {@link EnumerationFieldType} (not resolved through a type
 * definition reference, which the editor doesn't open for) - at least one value must be specified. Mirrors
 * SME/kernel's {@code MULTI_SELECT_FIELD_TOO_FEW_ENUM_VALUES} rule (condition {@code [.../Field/type] ==
 * "Enumeration" And NumberOfFilledFields(.../Values) < 2}, which likewise only looks at the field's own,
 * unresolved type) for the stricter case: the sole field of a multi-select group must have at least two values.
 */
public final class EnumerationValuesValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    ElementIndex index = new ElementIndex(documentModel);
    for (Element element : index.allElements()) {
      if (!(element instanceof FieldElement field) || field.getField() == null
          || !(field.getField().getFieldType() instanceof EnumerationFieldType enumerationFieldType)) {
        continue;
      }
      boolean isMultiSelectValueField = isMultiSelectValueField(index, field);
      checkMinimumValues(model, field.getId(), "field", enumerationFieldType, isMultiSelectValueField, errors);
    }

    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        if (typeDefinition.getFieldType() instanceof EnumerationFieldType enumerationFieldType) {
          checkMinimumValues(model, typeDefinition.getId(), "type definition", enumerationFieldType, false, errors);
        }
      }
    }
    return errors;
  }

  private static void checkMinimumValues(A12Model<?> model, String elementId, String elementKind,
      EnumerationFieldType enumerationFieldType, boolean isMultiSelectValueField, List<ModelValidationError> errors) {
    long valueCount = enumerationFieldType.getEnumerationType() == null ? 0
        : enumerationFieldType.getEnumerationType().getValues().stream()
            .filter(value -> value.getValue() != null && !value.getValue().isEmpty())
            .count();
    int required = isMultiSelectValueField ? 2 : 1;
    if (valueCount >= required) {
      return;
    }
    String message = isMultiSelectValueField
        ? "The enumeration in " + elementKind + " [id: " + elementId + "] is the value field of a multi-select group and must have at least 2 values."
        : "The enumeration in " + elementKind + " [id: " + elementId + "] must have at least 1 value.";
    errors.add(new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE, message, Severity.ERROR.name()));
  }

  /**
   * Mirrors the kernel rule's condition, which only fires for a field whose <em>own</em> type is literally
   * {@code Enumeration} - a multi-select value field that goes through a type definition reference isn't
   * covered by that rule (or, correspondingly, by the raised minimum here).
   */
  private static boolean isMultiSelectValueField(ElementIndex index, FieldElement field) {
    GroupElement parent = index.parentOf(field);
    return parent != null && parent.getGroup() != null && GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(parent.getGroup().getUsageType());
  }
}
