package de.a12.studio.modelsvalidation.validators.relationship;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Document Models that are connected via a Relationship must possess only one root group each (SME BA
 * docs, "Related Document Models" caution note) - the Relationship's connection point is the root group,
 * so a Document Model with zero or several root groups leaves it ambiguous.
 */
public final class RelationshipRootGroupCountValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/entityCharacteristics/documentModel";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof RelationshipModel relationshipModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (EntityCharacteristic entity : relationshipModel.getContent().getEntityCharacteristics()) {
      String documentModelId = entity.getDocumentModel();
      if (documentModelId == null || documentModelId.isBlank()) {
        // Missing/unresolved references are already reported by RelationshipDocumentModelReferenceValidator.
        continue;
      }
      DocumentModel documentModel = context.findOtherDocumentModel(documentModelId);
      if (documentModel == null || documentModel.getContent().getModelRoot() == null) {
        continue;
      }
      int rootGroupCount = documentModel.getContent().getModelRoot().getRootGroups().size();
      if (rootGroupCount != 1) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.relationshipRootGroupCount.wrongCount",
                documentModelId, entity.getRole(), rootGroupCount),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
