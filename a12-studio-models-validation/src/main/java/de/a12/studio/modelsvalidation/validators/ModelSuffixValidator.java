package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.settings.GeneralSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.List;

/**
 * When "Enforce Model Suffixes" is enabled in a project's Validation Settings (see
 * {@link GeneralSettings#isEnforceModelSuffixes()}), every model's filename must end with the
 * type-appropriate suffix (e.g. {@code "_DM"} for a Document Model, see {@link ModelType#getSuffix()}).
 * Applies to every model type. A {@link TypeDefinitionModel} is persisted with header modelType
 * "document" (see {@code NewModelFactory}), so its suffix is resolved from the concrete Java type
 * rather than the header, matching {@link ModelType#TYPEDEFINITION} instead of {@link ModelType#DOCUMENT}.
 */
public final class ModelSuffixValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/id";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (context.project() == null || context.projectItem() == null) {
      return List.of();
    }
    GeneralSettings general = ProjectRootSettings.load(context.project().getFolder()).getGeneral();
    if (!general.isEnforceModelSuffixes()) {
      return List.of();
    }

    ModelType modelType = model instanceof TypeDefinitionModel ? ModelType.TYPEDEFINITION : model.getModelType();
    if (modelType == null || modelType.getSuffix() == null) {
      return List.of();
    }

    String fileName = context.projectItem().getName();
    if (fileName == null || !fileName.toLowerCase().endsWith(".json") || fileName.contains("__generated")) {
      return List.of();
    }
    String baseName = fileName.substring(0, fileName.length() - ".json".length());
    String expectedSuffix = "_" + modelType.getSuffix();
    if (baseName.endsWith(expectedSuffix)) {
      return List.of();
    }

    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.modelSuffix.mismatch", modelType.getDisplayName(), expectedSuffix),
        Severity.ERROR.name()));
  }
}
