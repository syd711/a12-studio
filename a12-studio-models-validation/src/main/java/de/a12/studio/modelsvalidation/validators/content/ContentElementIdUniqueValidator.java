package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentElement;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Content element ids identify elements within the model and must be unique across the whole element tree. */
public final class ContentElementIdUniqueValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/root";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel) || contentModel.getContent().getRoot() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    collectDuplicates(contentModel, contentModel.getContent().getRoot(), new HashSet<>(), errors);
    return errors;
  }

  private void collectDuplicates(ContentModel model, ContentElement element, Set<String> seen, List<ModelValidationError> errors) {
    if (element.getId() != null && !seen.add(element.getId())) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.contentElementIdUnique.duplicate", element.getId()),
          Severity.ERROR.name()));
    }
    if (element.getChildren() != null) {
      for (ContentElement child : element.getChildren()) {
        collectDuplicates(model, child, seen, errors);
      }
    }
  }
}
