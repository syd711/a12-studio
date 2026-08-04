package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/** Every entry in {@code content.styles} must have a value (SME: "This field is required."). */
public final class OverviewStylesValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/styles";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (String style : overviewModel.getContent().getStyles()) {
      if (style == null || style.isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID, "A style value is required; remove the empty entry or set a value.", Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
