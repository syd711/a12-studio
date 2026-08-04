package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/** Both entities need a Document Model, and every referenced Document Model (incl. the link DM) must exist in the workspace. */
public final class RelationshipDocumentModelReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/entityCharacteristics/documentModel";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipModel relationshipModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (EntityCharacteristic entity : relationshipModel.getContent().getEntityCharacteristics()) {
      if (entity.getDocumentModel() == null || entity.getDocumentModel().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.a_document_model_must_be_selected_for_role"" + entity.getRole() + "\".", Severity.ERROR.name()));
      }
      else if (context.findOtherDocumentModel(entity.getDocumentModel()) == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.the_document_model"" + entity.getDocumentModel() + "\" of role \"" + entity.getRole()
                + "\" does not exist in the workspace.", Severity.ERROR.name()));
      }
    }

    String linkDocumentModel = relationshipModel.getContent().getLinkDocumentModelValue();
    if (linkDocumentModel != null && !linkDocumentModel.isBlank()
        && context.findOtherDocumentModel(linkDocumentModel) == null) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          "validation.the_link_document_model"" + linkDocumentModel + "\" does not exist in the workspace.", Severity.ERROR.name()));
    }
    return errors;
  }
}
