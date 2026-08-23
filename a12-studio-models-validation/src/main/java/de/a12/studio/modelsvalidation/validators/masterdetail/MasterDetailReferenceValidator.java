package de.a12.studio.modelsvalidation.validators.masterdetail;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.masterdetailmodel.FormMapping;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The referenced Overview Model or Tree Model must exist and be of the matching type; the same holds for
 * every mapped Form Model and Document Model in the form mapping (SME: "The given ... model could not be
 * found.").
 */
public final class MasterDetailReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/overviewModel";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof MasterDetailModel masterDetailModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    String overviewModel = masterDetailModel.getContent().getOverviewModel();
    if (overviewModel != null && !overviewModel.isBlank()
        && !(context.findOtherModel(overviewModel) instanceof OverviewModel)) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.masterDetailReference.overviewNotFound", overviewModel), Severity.ERROR.name()));
    }

    String treeModel = masterDetailModel.getContent().getTreeModel();
    if (treeModel != null && !treeModel.isBlank()
        && !(context.findOtherModel(treeModel) instanceof TreeModel)) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.masterDetailReference.treeNotFound", treeModel), Severity.ERROR.name()));
    }

    for (FormMapping mapping : masterDetailModel.getContent().getFormMapping()) {
      if (mapping.getDocumentModel() != null && !mapping.getDocumentModel().isBlank()
          && context.findOtherDocumentModel(mapping.getDocumentModel()) == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.masterDetailReference.documentNotFound", mapping.getDocumentModel()), Severity.ERROR.name()));
      }
      if (mapping.getFormModel() != null && !mapping.getFormModel().isBlank()
          && !(context.findOtherModel(mapping.getFormModel()) instanceof FormModel)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.masterDetailReference.formNotFound", mapping.getFormModel()), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
