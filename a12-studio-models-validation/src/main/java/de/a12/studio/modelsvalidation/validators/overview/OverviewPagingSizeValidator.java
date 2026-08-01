package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * If set, the Paging Size must be at least 1 (SME: {@code zeroNotAllowed}/{@code minValue: 1}). The
 * Studio's own Paging Size spinner already enforces this, but this defends hand-edited JSON.
 */
public final class OverviewPagingSizeValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/pagingSize";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel) || overviewModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    Integer pagingSize = overviewModel.getContent().getConfiguration().getPagingSize();
    if (pagingSize == null || pagingSize >= 1) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID, "Paging Size must be at least 1.", Severity.ERROR.name()));
  }
}
