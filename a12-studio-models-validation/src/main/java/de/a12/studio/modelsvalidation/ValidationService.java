package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.modelsvalidation.services.ApplicationModelValidationService;
import de.a12.studio.modelsvalidation.services.DocumentModelValidationService;
import de.a12.studio.modelsvalidation.services.FormModelValidationService;
import de.a12.studio.modelsvalidation.services.OverviewModelValidationService;
import de.a12.studio.modelsvalidation.validators.MissingLocaleValidator;
import de.a12.studio.modelsvalidation.validators.TimeZoneValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Validates the models of one A12 Studio {@link Project}. Dispatches to the sub validation service for the
 * model's concrete type, each of which runs its own list of {@link de.a12.studio.modelsvalidation.validators.ModelValidator}s
 * (see e.g. {@link DocumentModelValidationService}).
 */
public class ValidationService {

  private static final MissingLocaleValidator MISSING_LOCALE_VALIDATOR = new MissingLocaleValidator();
  private static final TimeZoneValidator TIME_ZONE_VALIDATOR = new TimeZoneValidator();

  private final Project project;
  private final DocumentModelValidationService documentModelValidationService = new DocumentModelValidationService();
  private final OverviewModelValidationService overviewModelValidationService = new OverviewModelValidationService();
  private final FormModelValidationService formModelValidationService = new FormModelValidationService();
  private final ApplicationModelValidationService applicationModelValidationService = new ApplicationModelValidationService();

  public ValidationService(Project project) {
    this.project = project;
  }

  /** Every validation problem found in {@code model}, depending on its concrete type. */
  public List<ModelValidationError> validate(A12Model<?> model) {
    ValidationContext context = buildContext(model);
    return switch (model) {
      case DocumentModel documentModel -> documentModelValidationService.validate(documentModel, context);
      case OverviewModel overviewModel -> overviewModelValidationService.validate(overviewModel, context);
      case FormModel formModel -> formModelValidationService.validate(formModel, context);
      case ApplicationModel applicationModel -> applicationModelValidationService.validate(applicationModel, context);
      default -> List.of();
    };
  }

  /** The single problem reported against {@code elementId} in {@code model}, if any. */
  public Optional<ModelValidationError> validateElement(A12Model<?> model, String elementId) {
    return validate(model).stream().filter(error -> elementId.equals(error.elementId())).findFirst();
  }

  /**
   * Every human-readable settings problem for this model (locales, time zone, etc., as edited via the Model
   * Settings dialog), whether single-model (missing locale) or cross-model (e.g. a time zone that disagrees
   * with the rest of the project). Empty if there are none, e.g. for driving both a settings-button badge
   * and its error tooltip.
   */
  public List<String> getSettingsIssueMessages(A12Model<?> model) {
    List<String> messages = new ArrayList<>();
    getTimeZoneMismatchError(model).ifPresent(messages::add);
    getMissingLocaleError(model).ifPresent(messages::add);
    return messages;
  }

  /** For the Locales settings panel: at least one locale is required. */
  public Optional<String> getMissingLocaleError(A12Model<?> model) {
    return MISSING_LOCALE_VALIDATOR.validate(model, buildContext(model)).stream()
        .map(ModelValidationError::message)
        .findFirst();
  }

  /** For the Timezone settings panel: every document model in a project must use the same time zone. */
  public Optional<String> getTimeZoneMismatchError(A12Model<?> model) {
    return TIME_ZONE_VALIDATOR.validate(model, buildContext(model)).stream()
        .map(ModelValidationError::message)
        .findFirst();
  }

  private ValidationContext buildContext(A12Model<?> model) {
    return new ValidationContext(project, ProjectModels.getOtherDocumentModels(project, model));
  }
}
