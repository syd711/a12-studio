package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.List;

/**
 * A string field with a {@code pattern} must also specify the error text shown when the pattern doesn't
 * match, otherwise the field fails value validation with no explanation for the user. Mirrors SME's
 * {@code PATTERN_AND_NO_ERROR_MESSAGE} rule on {@code DomainField}.
 */
public final class StringPatternErrorMessageValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel);
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : index.allElements()) {
      if (!(element instanceof FieldElement field) || field.getField() == null) {
        continue;
      }
      FieldType effectiveType = index.effectiveFieldType(field.getField().getFieldType());
      if (!(effectiveType instanceof StringFieldType stringFieldType) || stringFieldType.getStringType() == null) {
        continue;
      }
      var stringType = stringFieldType.getStringType();
      String pattern = stringType.getPattern();
      if (pattern == null || pattern.isEmpty() || hasErrorMessage(stringType.getErrorMessage())) {
        continue;
      }
      errors.add(new ModelValidationError(model, field.getId(),
          "The pattern '" + pattern + "' is specified, but the corresponding error text is not.", Severity.ERROR.name()));
    }
    return errors;
  }

  private static boolean hasErrorMessage(List<Label> errorMessage) {
    return errorMessage != null && errorMessage.stream().anyMatch(label -> label.getText() != null && !label.getText().isBlank());
  }
}
