package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/** A content model must have a root element with an id and a type (the content engine's root element rules are strict). */
public final class ContentRootElementValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/root";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel)) {
      return List.of();
    }
    if (contentModel.getContent().getRoot() == null) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.contentRootElement.missingRoot"), Severity.ERROR.name()));
    }
    if (contentModel.getContent().getRoot().getType() == null || contentModel.getContent().getRoot().getType().isBlank()) {
      return List.of(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.contentRootElement.missingType"), Severity.ERROR.name()));
    }
    return List.of();
  }
}
