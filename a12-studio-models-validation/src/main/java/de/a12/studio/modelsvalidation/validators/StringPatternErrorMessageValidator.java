package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.StringTypeOptions;
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
 * A string field with a {@code pattern} must also specify the error text for <em>every locale defined
 * in the model</em>, otherwise the field fails value validation with no explanation for the user in
 * some locales. Mirrors SME's {@code PATTERN_AND_NO_ERROR_MESSAGE} rule on {@code DomainField}.
 */
public final class StringPatternErrorMessageValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    Set<String> requiredLocales = model.getLocales() == null ? Set.of()
        : model.getLocales().stream()
            .map(de.a12.studio.models.Locale::getCode)
            .filter(c -> c != null && !c.isBlank())
            .collect(Collectors.toSet());

    ElementIndex index = context.elementIndex();
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (!(element instanceof FieldElement field) || field.getField() == null) {
        continue;
      }
      FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
      if (effectiveType instanceof StringFieldType stringFieldType && stringFieldType.getStringType() != null) {
        checkStringType(model, field.getId(), stringFieldType.getStringType(), requiredLocales, errors);
      }
    }

    // See the equivalent comment in NumberFieldValueLimitValidator: a model's own type definitions aren't
    // reachable through allElements(), so without this second pass a String type definition's pattern/error
    // message consistency would never be checked until some other model's field references it.
    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        if (typeDefinition.getFieldType() instanceof StringFieldType stringFieldType && stringFieldType.getStringType() != null) {
          checkStringType(model, typeDefinition.getId(), stringFieldType.getStringType(), requiredLocales, errors);
        }
      }
    }
    return errors;
  }

  private static void checkStringType(A12Model<?> model, String elementId, StringTypeOptions stringType,
      Set<String> requiredLocales, List<ModelValidationError> errors) {
    String pattern = stringType.getPattern();
    if (pattern == null || pattern.isEmpty()) {
      return;
    }

    List<Label> errorMessage = stringType.getErrorMessage();
    Set<String> coveredLocales = coveredLocales(errorMessage);

    if (requiredLocales.isEmpty()) {
      // No locales declared on the model — fall back to: at least one entry must exist.
      if (coveredLocales.isEmpty()) {
        errors.add(new ModelValidationError(model, elementId, ElementProperty.ERROR_MESSAGE,
            ValidationMessages.get("validation.stringPatternErrorMessage.missing", pattern), Severity.ERROR.name()));
      }
    } else {
      // Report each locale that is missing a non-blank error message entry.
      for (String locale : requiredLocales) {
        if (!coveredLocales.contains(locale)) {
          errors.add(new ModelValidationError(model, elementId, ElementProperty.ERROR_MESSAGE,
              ValidationMessages.get("validation.stringPatternErrorMessage.missingLocale", pattern, locale),
              Severity.ERROR.name()));
        }
      }
    }
  }

  private static Set<String> coveredLocales(List<Label> errorMessage) {
    if (errorMessage == null) {
      return Set.of();
    }
    return errorMessage.stream()
        .filter(label -> label.getLocale() != null && label.getText() != null && !label.getText().isBlank())
        .map(Label::getLocale)
        .collect(Collectors.toSet());
  }
}
