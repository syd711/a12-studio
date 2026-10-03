package de.a12.studio.modelsvalidation.validators.transformer;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.transformermodel.DeletePath;
import de.a12.studio.models.transformermodel.RenamePath;
import de.a12.studio.models.transformermodel.TransformerModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The Element Selection tab, ported from SME's {@code TransformerConfigModel} (29.4.0):
 * <ul>
 *   <li>Rename Paths: {@code OriginalPath} and {@code NewElementName} are required, the new name must match
 *       {@code ^[a-zA-Z_][a-zA-Z0-9_\-.]*$}, and {@code originalPathNotUnique} - an element is renamed at most once</li>
 *   <li>Delete Paths: {@code path} is required and {@code pathNotUnique}</li>
 * </ul>
 * Like SME's {@code RepetitionNotUnique}, every occurrence after the first of a repeated value is reported.
 */
public final class TransformerElementSelectionValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TransformerModel transformerModel) || transformerModel.getContent() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    checkRenamePaths(model, transformerModel.getContent().getRenamePathsOrEmpty(), errors);
    checkDeletePaths(model, transformerModel.getContent().getDeletePathsOrEmpty(), errors);
    return errors;
  }

  private static void checkRenamePaths(A12Model<?> model, List<RenamePath> renames, List<ModelValidationError> errors) {
    Set<String> seen = new HashSet<>();
    for (int index = 0; index < renames.size(); index++) {
      RenamePath rename = renames.get(index);
      String original = rename.getOriginalPath();
      if (original == null || original.isBlank()) {
        errors.add(error(model, TransformerElementIds.renamePath(index, "OriginalPath"),
            ValidationMessages.get("validation.transformer.renamePath.originalPathMissing")));
      }
      else if (!seen.add(original)) {
        errors.add(error(model, TransformerElementIds.renamePath(index, "OriginalPath"),
            ValidationMessages.get("validation.transformer.renamePath.originalPathDuplicate", original)));
      }

      String newName = rename.getNewElementName();
      if (newName == null || newName.isBlank()) {
        errors.add(error(model, TransformerElementIds.renamePath(index, "NewElementName"),
            ValidationMessages.get("validation.transformer.renamePath.newElementNameMissing")));
      }
      else if (!RenamePath.ELEMENT_NAME.matcher(newName).matches()) {
        errors.add(error(model, TransformerElementIds.renamePath(index, "NewElementName"),
            ValidationMessages.get("validation.transformer.renamePath.newElementNameInvalid", newName)));
      }
    }
  }

  private static void checkDeletePaths(A12Model<?> model, List<DeletePath> deletes, List<ModelValidationError> errors) {
    Set<String> seen = new HashSet<>();
    for (int index = 0; index < deletes.size(); index++) {
      String path = deletes.get(index).getPath();
      if (path == null || path.isBlank()) {
        errors.add(error(model, TransformerElementIds.deletePath(index),
            ValidationMessages.get("validation.transformer.deletePath.pathMissing")));
      }
      else if (!seen.add(path)) {
        errors.add(error(model, TransformerElementIds.deletePath(index),
            ValidationMessages.get("validation.transformer.deletePath.pathDuplicate", path)));
      }
    }
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
