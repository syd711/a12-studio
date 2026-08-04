package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/** When Filter is enabled, a Filter Mode must be selected (SME rule "filterModeIsRequiredWhenShowFilter"). */
public final class OverviewFilterModeRequiredValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/filterConfiguration/filterMode";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    if (!Boolean.TRUE.equals(overviewModel.getContent().getConfiguration().getEnableFilter())) {
      return List.of();
    }
    String filterMode = overviewModel.getContent().getConfiguration().getFilterConfiguration() != null
        ? overviewModel.getContent().getConfiguration().getFilterConfiguration().getFilterMode()
        : null;
    if (filterMode != null && !filterMode.isBlank()) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID, "The field is mandatory.", Severity.ERROR.name()));
  }
}
