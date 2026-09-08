package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.CustomFieldFieldType;
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
 * A Custom field type must name the underlying custom field it wraps, ported from SME/kernel's {@code
 * DomainField.json} rule {@code CUSTOM_FIELD_TYPE_CODENAME_MISSING}. Runs both against fields and, since a
 * Type Definition Model's own type definitions aren't reachable through {@link ElementIndex#allElements()},
 * against a model's own {@code typeDefinitions} directly.
 */
public final class CustomFieldTypeConfigValidator implements ModelValidator {

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
      if (effectiveType instanceof CustomFieldFieldType customFieldType) {
        checkCodename(model, field.getId(), customFieldType, errors);
      }
    }

    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        if (typeDefinition.getFieldType() instanceof CustomFieldFieldType customFieldType) {
          checkCodename(model, typeDefinition.getId(), customFieldType, errors);
        }
      }
    }
    return errors;
  }

  private static void checkCodename(A12Model<?> model, String elementId, CustomFieldFieldType customFieldType,
      List<ModelValidationError> errors) {
    String name = customFieldType.getCustomFieldType() == null ? null : customFieldType.getCustomFieldType().getName();
    if (name == null || name.isBlank()) {
      errors.add(new ModelValidationError(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.customFieldTypeConfig.codenameMissing", elementId), Severity.ERROR.name()));
    }
  }
}
