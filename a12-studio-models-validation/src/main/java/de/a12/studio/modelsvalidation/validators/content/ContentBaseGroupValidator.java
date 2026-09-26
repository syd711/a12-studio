package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/**
 * The base group of a Content Model ({@code content.configuration.baseGroupId}), as SME's validator checks it: it needs
 * the Content Model to be bound to a Document Model, and it must be a group of that Document Model. Nothing is reported
 * while the Document Model itself cannot be found - that is the reference validator's finding.
 */
public final class ContentBaseGroupValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/baseGroupId";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel)) {
      return List.of();
    }
    ContentDocument document = ContentDocument.of(contentModel, context);
    String baseGroupId = document.baseGroupId();
    if (baseGroupId == null) {
      return List.of();
    }
    if (document.documentModelId() == null) {
      return List.of(error(model, "validation.contentBaseGroup.noDocumentModel", baseGroupId));
    }
    if (document.structure() == null) {
      return List.of();
    }
    if (document.baseGroup() == null) {
      return List.of(error(model, "validation.contentBaseGroup.notFound", baseGroupId, document.documentModelId()));
    }
    if (!document.baseGroup().isGroup()) {
      return List.of(error(model, "validation.contentBaseGroup.notAGroup", baseGroupId, document.documentModelId()));
    }
    return List.of();
  }

  private static ModelValidationError error(A12Model<?> model, String key, Object... arguments) {
    return new ModelValidationError(model, ELEMENT_ID, ValidationMessages.get(key, arguments), Severity.ERROR.name());
  }
}
