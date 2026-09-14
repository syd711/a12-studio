package de.a12.studio.ui.util;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModelResolver;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Resolves the Document Model an {@link de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel}
 * adds onto, for the "Additive Elements Only" tree toggle's read-only preview (see
 * {@code DocumentModelElementsTreeController}). Thin project-tree adapter over {@link
 * AdditiveDocumentModelResolver}'s reverse lookup (see its javadoc for the lookup itself and its
 * limitations) - {@link de.a12.studio.modelsvalidation.validators.ElementIndex} in
 * a12-studio-models-validation uses the same resolver directly, via {@link
 * de.a12.studio.modelsvalidation.ValidationContext}'s own model lists, to make an Additive Document
 * Model's relative-path fields (e.g. a Computation's {@code computedFieldRelPath}) resolve against fields
 * the base model provides.
 */
public final class AdditiveDocumentModels {

  private AdditiveDocumentModels() {
  }

  /**
   * The base Document Model of the first Combination Model found (by project tree order) whose content
   * has an Addition step referencing {@code additiveModel}, or empty if no Combination Model uses it yet,
   * or its base model id doesn't resolve to an actual Document Model in the project. If more than one
   * Combination Model uses the same Additive Document Model, the first one found wins - see {@link
   * AdditiveDocumentModelResolver}'s javadoc for why that ambiguity is acceptable here.
   */
  public static Optional<DocumentModel> findBaseModel(@NonNull ProjectItem projectItem, @NonNull DocumentModel additiveModel) {
    return AdditiveDocumentModelResolver.findBaseModel(additiveModel,
        ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.COMBINATION),
        ProjectDocumentModels.getOtherDocumentModels(projectItem));
  }
}
