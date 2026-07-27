package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;

import java.util.List;

/**
 * Per-validate call state shared by every validator in {@code de.a12.studio.modelsvalidation.validators}:
 * the project the model being validated belongs to, the project item backing it (its file — needed e.g.
 * for the id-matches-filename rule; may be null when the model has no file yet), every other
 * {@link DocumentModel} in that project (cross-model checks such as resolving an Include reference), and
 * every other model of any type (reference-integrity checks such as a tree model's document model refs).
 */
public record ValidationContext(Project project, ProjectItem projectItem,
                                List<DocumentModel> otherDocumentModels, List<A12Model<?>> otherModels) {

  /** The other model with the given header id, or null; used by reference-integrity validators. */
  public A12Model<?> findOtherModel(String id) {
    if (id == null) {
      return null;
    }
    return otherModels.stream().filter(model -> id.equals(model.getId())).findFirst().orElse(null);
  }

  /** The other {@link DocumentModel} with the given header id, or null. */
  public DocumentModel findOtherDocumentModel(String id) {
    if (id == null) {
      return null;
    }
    return otherDocumentModels.stream().filter(model -> id.equals(model.getId())).findFirst().orElse(null);
  }
}
