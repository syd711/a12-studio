package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.List;

/**
 * The model's header id must equal its file name without the ".json" extension — the SME keeps both in
 * sync on create/rename/copy, and a mismatch surfaces in its workspace explorer as "File name and model
 * name are different for this model". Applies to every model type.
 */
public final class ModelIdFilenameValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/id";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (context.projectItem() == null || model.getId() == null) {
      return List.of();
    }
    String fileName = context.projectItem().getName();
    if (fileName == null || !fileName.toLowerCase().endsWith(".json")) {
      return List.of();
    }
    String expectedId = fileName.substring(0, fileName.length() - ".json".length());
    if (expectedId.equals(model.getId())) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID,
        ValidationMessages.get("validation.modelIdFilename.mismatch", model.getId(), fileName),
        Severity.ERROR.name()));
  }
}
