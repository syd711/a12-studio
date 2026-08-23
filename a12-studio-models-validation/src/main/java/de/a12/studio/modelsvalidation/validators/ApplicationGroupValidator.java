package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.features.ApplicationGroupFeature;
import de.a12.studio.models.projects.settings.AdvancedSettings;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * While a project has application groups enabled, every model's {@code applicationGroup} header
 * annotation must match the project's configured group name, catching a model that predates the
 * current group name or was never migrated by {@link ApplicationGroupFeature}. Only meant to be
 * registered with {@link de.a12.studio.modelsvalidation.ValidationService} while the feature is
 * enabled (see {@link AdvancedSettings#isUseApplicationGroups()}); the enablement check below is
 * just a safety net for that registration.
 */
public final class ApplicationGroupValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/annotations/applicationGroup";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    AdvancedSettings settings = context.project().getSettings().getAdvancedSettings();
    if (!settings.isUseApplicationGroups()) {
      return List.of();
    }
    String expectedGroup = settings.getApplicationGroupName();
    String actualGroup = findApplicationGroup(model);
    if (expectedGroup.equals(actualGroup) || StringUtils.isEmpty(expectedGroup)) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.applicationGroup.mismatch", actualGroup == null ? "" : actualGroup, expectedGroup),
        Severity.ERROR.name()));
  }

  private static String findApplicationGroup(A12Model<?> model) {
    for (Annotation annotation : model.getAnnotations()) {
      if (ApplicationGroupFeature.ANNOTATION_NAME.equals(annotation.getName())) {
        return annotation.getValue();
      }
    }
    return null;
  }
}
