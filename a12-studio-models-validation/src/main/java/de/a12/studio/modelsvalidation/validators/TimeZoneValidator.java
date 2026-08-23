package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.List;

/**
 * Every document model in a project must use the same time zone, for the Timezone settings panel. Mirrors
 * SME's {@code TimeZoneCheck} custom validation rule.
 */
public final class TimeZoneValidator implements ModelValidator {

  // Not a real element id, see MissingLocaleValidator#ELEMENT_ID for why a stable placeholder is needed.
  public static final String ELEMENT_ID = "header/timeZone";

  // The only two time zones a document model's settings can be configured with (see TimezonePanelController);
  // every document model in a project must agree on one of them, mirroring SME's TimeZoneCheck rule.
  private static final String EUROPE_BERLIN = "Europe/Berlin";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    String timeZone = getTimeZone(documentModel);
    if (EUROPE_BERLIN.equals(timeZone)) {
      return List.of();
    }
    boolean otherModelUsesEuropeBerlin =
        context.otherDocumentModels().stream().anyMatch(other -> EUROPE_BERLIN.equals(getTimeZone(other)));
    if (!otherModelUsesEuropeBerlin) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.timeZone.mismatch"), Severity.ERROR.name()));
  }

  private static String getTimeZone(DocumentModel documentModel) {
    DocumentModelContent content = documentModel.getContent();
    ModelConfig modelConfig = content != null ? content.getModelConfig() : null;
    return modelConfig != null ? modelConfig.getTimeZone() : null;
  }
}
