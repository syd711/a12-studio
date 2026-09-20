package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * A Query Model must have a target Document Model, and that must exist in the project. The generic {@link
 * de.a12.studio.modelsvalidation.validators.HeaderModelReferenceValidator} only catches a dangling target when the
 * header's DOCUMENT-type reference (synced by {@code QuerySettingsPanelController}) still names it, so a query whose
 * target model was deleted or renamed outside the app - or whose header reference was dropped - would otherwise show
 * an empty tree and no error at all. Every other Query validator silently skips a query it cannot resolve the
 * target of, so this is the one place that says so.
 */
public final class QueryTargetDocumentModelRequiredValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/targetDocumentModel";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof QueryModel queryModel) || queryModel.getContent() == null) {
      return List.of();
    }
    String targetDocumentModel = queryModel.getContent().getTargetDocumentModel();
    if (targetDocumentModel == null || targetDocumentModel.isBlank()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.queryTargetDocumentModelRequired.missing"), Severity.ERROR.name()));
    }
    // A Combined Document Model is accepted like elsewhere (Form bindings): resolving fields against one is not
    // supported by the Query validators yet, but naming one is not a dangling reference.
    if (context.otherModels() != null && !context.hasOtherDocumentOrCombinedModel(targetDocumentModel)) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.queryTargetDocumentModelRequired.unresolved", targetDocumentModel),
          Severity.ERROR.name()));
    }
    return List.of();
  }
}
