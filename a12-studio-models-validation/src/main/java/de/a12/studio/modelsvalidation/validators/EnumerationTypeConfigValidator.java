package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.Category;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationTypeOptions;
import de.a12.studio.models.documentmodel.EnumerationValue;
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
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Structural sanity checks for an Enumeration field type's configuration, beyond the "at least one value"
 * check in {@link EnumerationValuesValidator} and the duplicate-value/duplicate-category-name checks in
 * {@link BasicConsistencyValidator}: ported from SME/kernel's {@code DomainField.json} Enumeration-section
 * rules {@code VALUE_MISSING}, {@code LABEL_INVALID}/{@code A12_LABEL_FOR_LANGUAGE_MISSING}, {@code
 * CATEGORY_NAME_MISSING}, {@code CATEGORY_VALUE_MISSING}. Runs both against fields and, since a Type
 * Definition Model's own type definitions aren't reachable through {@link ElementIndex#allElements()},
 * against a model's own {@code typeDefinitions} directly.
 */
public final class EnumerationTypeConfigValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    Set<String> locales = model.getLocales() == null ? Set.of()
        : model.getLocales().stream()
            .map(de.a12.studio.models.Locale::getCode)
            .filter(code -> code != null && !code.isBlank())
            .collect(Collectors.toSet());

    ElementIndex index = new ElementIndex(documentModel, context.otherDocumentModels());
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (!(element instanceof FieldElement field) || field.getField() == null) {
        continue;
      }
      FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
      if (effectiveType instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
        checkEnumerationType(model, field.getId(), enumType.getEnumerationType(), locales, errors);
      }
    }

    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        if (typeDefinition.getFieldType() instanceof EnumerationFieldType enumType && enumType.getEnumerationType() != null) {
          checkEnumerationType(model, typeDefinition.getId(), enumType.getEnumerationType(), locales, errors);
        }
      }
    }
    return errors;
  }

  private static void checkEnumerationType(A12Model<?> model, String elementId, EnumerationTypeOptions enumerationType,
      Set<String> locales, List<ModelValidationError> errors) {
    if (enumerationType.getValues() != null) {
      for (EnumerationValue value : enumerationType.getValues()) {
        if (value.getValue() == null || value.getValue().isBlank()) {
          errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
              ValidationMessages.get("validation.enumerationTypeConfig.valueMissing", elementId)));
          continue;
        }
        checkLabels(model, elementId, value, locales, errors);
      }
    }
    if (enumerationType.getCategories() != null) {
      for (Category category : enumerationType.getCategories()) {
        if (category.getName() == null || category.getName().isBlank()) {
          errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
              ValidationMessages.get("validation.enumerationTypeConfig.categoryNameMissing", elementId)));
        }
        if (category.getValues() != null && category.getValues().stream().anyMatch(v -> v == null || v.isBlank())) {
          errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
              ValidationMessages.get("validation.enumerationTypeConfig.categoryValueMissing", elementId,
                  category.getName())));
        }
      }
    }
    checkErrorMessage(model, elementId, enumerationType, locales, errors);
  }

  /** Mirrors {@link StringPatternErrorMessageValidator}: a custom error message, once opted into, must cover
   * every locale declared on the model. */
  private static void checkErrorMessage(A12Model<?> model, String elementId, EnumerationTypeOptions enumerationType,
      Set<String> locales, List<ModelValidationError> errors) {
    if (!Boolean.FALSE.equals(enumerationType.getUseDefaultErrorMessages()) || locales.isEmpty()) {
      return;
    }
    Set<String> coveredLocales = enumerationType.getErrorMessage() == null ? Set.of()
        : enumerationType.getErrorMessage().stream()
            .filter(label -> label.getLocale() != null && label.getText() != null && !label.getText().isBlank())
            .map(Label::getLocale)
            .collect(Collectors.toSet());
    for (String locale : locales) {
      if (!coveredLocales.contains(locale)) {
        errors.add(error(model, elementId, ElementProperty.ERROR_MESSAGE,
            ValidationMessages.get("validation.enumerationTypeConfig.errorMessageMissingLocale", elementId, locale)));
      }
    }
  }

  private static void checkLabels(A12Model<?> model, String elementId, EnumerationValue value, Set<String> locales,
      List<ModelValidationError> errors) {
    if (locales.isEmpty()) {
      return;
    }
    Set<String> coveredLocales = value.getLabel() == null ? Set.of()
        : value.getLabel().stream()
            .filter(label -> label.getLocale() != null && label.getText() != null && !label.getText().isBlank())
            .map(Label::getLocale)
            .collect(Collectors.toSet());
    for (String locale : locales) {
      if (!coveredLocales.contains(locale)) {
        errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
            ValidationMessages.get("validation.enumerationTypeConfig.labelMissing", elementId, value.getValue(), locale)));
      }
    }
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String property, String message) {
    return new ModelValidationError(model, elementId, property, message, Severity.ERROR.name());
  }
}
