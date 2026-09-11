package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
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
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Structural sanity checks for a String field type's configuration, ported from SME/kernel's
 * {@code DomainField.json} String-section rules ({@code PATTERN_INVALID}, {@code MIN_LENGTH_BIGGER_MAX_LENGTH},
 * {@code LINE_BREAK_AND_MAX_LENGTH_1_INVALID}, {@code A12_LINE_BREAK_AND_ALPH_SORTING_INVALID}). Runs both
 * against fields (as before) and, since a Type Definition Model's own type definitions aren't reachable
 * through {@link ElementIndex#allElements()}, against a model's own {@code typeDefinitions} directly.
 */
public final class StringTypeConfigValidator implements ModelValidator {

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
      FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
      if (effectiveType instanceof StringFieldType stringFieldType && stringFieldType.getStringType() != null) {
        checkStringType(model, field.getId(), stringFieldType.getStringType(), errors);
      }
    }

    if (documentModel.getContent().getTypeDefinitions() != null) {
      for (TypeDefinition typeDefinition : documentModel.getContent().getTypeDefinitions()) {
        if (typeDefinition.getFieldType() instanceof StringFieldType stringFieldType && stringFieldType.getStringType() != null) {
          checkStringType(model, typeDefinition.getId(), stringFieldType.getStringType(), errors);
        }
      }
    }
    return errors;
  }

  private static void checkStringType(A12Model<?> model, String elementId, StringTypeOptions stringType,
      List<ModelValidationError> errors) {
    if (stringType.getPattern() != null && !stringType.getPattern().isEmpty() && !isValidRegex(stringType.getPattern())) {
      errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.stringTypeConfig.invalidPattern", elementId, stringType.getPattern())));
    }
    if (stringType.getMinLength() != null && stringType.getMaxLength() != null
        && stringType.getMinLength() > stringType.getMaxLength()) {
      errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.stringTypeConfig.minLengthBiggerMaxLength", elementId)));
    }
    if (Boolean.TRUE.equals(stringType.getLineBreaksPermitted()) && stringType.getMaxLength() != null
        && stringType.getMaxLength() == 1) {
      errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.stringTypeConfig.lineBreakAndMaxLengthOne", elementId)));
    }
    if (Boolean.TRUE.equals(stringType.getLineBreaksPermitted()) && Boolean.TRUE.equals(stringType.getAlphabeticalSorting())) {
      errors.add(error(model, elementId, ElementProperty.DATA_TYPE,
          ValidationMessages.get("validation.stringTypeConfig.lineBreakAndAlphabeticalSorting", elementId)));
    }
  }

  private static boolean isValidRegex(String pattern) {
    try {
      Pattern.compile(pattern);
      return true;
    } catch (PatternSyntaxException e) {
      return false;
    }
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String property, String message) {
    return new ModelValidationError(model, elementId, property, message, Severity.ERROR.name());
  }
}
