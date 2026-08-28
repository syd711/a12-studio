package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.A12Model;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * When the Application Groups feature is enabled for a project, every model's filename must start
 * with the configured group's "{@code <group>_}" prefix.
 */
public class ApplicationGroupFilenamePrefixValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/id";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (context.project() == null || context.projectItem() == null) {
      return List.of();
    }

    ApplicationGroupsSettings settings = ApplicationGroupsSettings.load(context.project().getFolder());
    if (!settings.isUseApplicationGroups()) {
      return List.of();
    }
    String groupName = settings.getApplicationGroupName();
    if (!ApplicationGroupFeature.isValidGroupName(groupName)) {
      return List.of();
    }

    String fileName = context.projectItem().getName();
    if (fileName == null || !fileName.toLowerCase().endsWith(".json")) {
      return List.of();
    }
    String baseName = fileName.substring(0, fileName.length() - ".json".length());
    String expectedPrefix = groupName + "_";
    if (baseName.startsWith(expectedPrefix)) {
      return List.of();
    }

    return List.of(new ModelValidationError(model, ELEMENT_ID,
        "File name \"" + fileName + "\" does not start with the application group prefix \"" + expectedPrefix + "\".",
        Severity.ERROR.name()));
  }
}
