package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.List;

/**
 * {@link ModelConfig#getSupportedCharacters()} (edited via {@code SupportedCharactersPanelController} as a
 * raw JSON array of strings) only gets a JSON-parse check today. SME additionally requires every entry to be
 * a single character with no stray leading/trailing whitespace, mirroring the kernel's own "single quoted
 * character" rule.
 */
public final class SupportedCharactersValidator implements ModelValidator {

  // Not a real element id: supportedCharacters lives on the model's ModelConfig header, not in an element
  // tree (see MissingLocaleValidator.ELEMENT_ID for why a stable placeholder is needed).
  public static final String ELEMENT_ID = "header/modelConfig/supportedCharacters";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }
    ModelConfig modelConfig = documentModel.getContent() == null ? null : documentModel.getContent().getModelConfig();
    if (modelConfig == null || modelConfig.getSupportedCharacters() == null) {
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (String character : modelConfig.getSupportedCharacters()) {
      if (character == null || character.length() != 1 || !character.equals(character.trim())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.supportedCharacters.invalidEntry", String.valueOf(character)), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
