package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/** An overview model needs at least one column (SME: "Columns must not be empty"). */
public final class OverviewColumnsNotEmptyValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/columns";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    if (overviewModel.getContent().getColumns() != null && !overviewModel.getContent().getColumns().isEmpty()) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID, "Columns must not be empty.", Severity.ERROR.name()));
  }
}
