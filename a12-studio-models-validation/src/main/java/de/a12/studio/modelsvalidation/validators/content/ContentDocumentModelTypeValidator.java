package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ModelTypeMessages;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * The Document Model of a Content Model has to be a Document Model (or a Combination Model, which stands in for one): SME
 * offers nothing else and calls any other reference "Invalid Reference". That the referenced model exists at all is the
 * generic {@link HeaderModelReferenceValidator}'s finding, so a missing model is not reported twice.
 */
public final class ContentDocumentModelTypeValidator implements ModelValidator {

  public static final String ELEMENT_ID = HeaderModelReferenceValidator.ELEMENT_ID;

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel) || contentModel.getDocumentModelId() == null || context.otherModels() == null) {
      return List.of();
    }
    String id = contentModel.getDocumentModelId();
    A12Model<?> referenced = context.findOtherModel(id);
    if (referenced == null || referenced instanceof DocumentModel || referenced instanceof CombinedDocumentModel) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.contentDocumentModel.wrongType", id, ModelTypeMessages.getDisplayName(referenced.getModelType())),
        Severity.ERROR.name()));
  }
}
