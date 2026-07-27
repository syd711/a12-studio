package de.a12.studio.modelsvalidation.validators.masterdetail;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/** A master-detail module needs a type, and type "overview" requires an Overview Model to be selected. */
public final class MasterDetailTypeConsistencyValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/type";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof MasterDetailModel masterDetailModel)) {
      return List.of();
    }
    String type = masterDetailModel.getContent().getType();
    if (type == null || type.isBlank()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          "The master-detail type is required.", Severity.ERROR.name()));
    }
    if ("overview".equals(type)
        && (masterDetailModel.getContent().getOverviewModel() == null || masterDetailModel.getContent().getOverviewModel().isBlank())) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          "An overview model is required for master-detail type \"overview\".", Severity.ERROR.name()));
    }
    return List.of();
  }
}
