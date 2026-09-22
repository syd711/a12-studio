package de.a12.studio.ui.util;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModelResolver;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.util.List;
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

  /**
   * Every Combination Model that references {@code additiveModel}, each paired with its resolved base
   * Document Model - see {@link AdditiveDocumentModelResolver#findCandidateContexts}. Used by {@link
   * de.a12.studio.ui.editors.documentmodel.DocumentModelElementsTreeController} to detect when {@link
   * #findBaseModel}'s silent first-wins resolution is actually ambiguous, so it can ask the user which
   * Combination Model to preview against instead - matching SME's own behavior for opening an Additive
   * Document Model (silently when there is exactly one candidate, by asking when there are several).
   */
  public static List<AdditiveDocumentModelResolver.AdditiveContext> findCandidateContexts(
      @NonNull ProjectItem projectItem, @NonNull DocumentModel additiveModel) {
    return AdditiveDocumentModelResolver.findCandidateContexts(additiveModel,
        ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.COMBINATION),
        ProjectDocumentModels.getOtherDocumentModels(projectItem));
  }
}
