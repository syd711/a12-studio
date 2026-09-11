package de.a12.studio.ui.util;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.util.Objects;
import java.util.Optional;

/**
 * Resolves the Document Model an {@link de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel}
 * adds onto, for the "Additive Elements Only" tree toggle's read-only preview (see
 * {@code DocumentModelElementsTreeController}).
 * <p>
 * An Additive Document Model's own file carries no reference to that base model - only the consuming
 * Combination Model does, via its {@code baseModelId} and an Addition step's {@code AdditiveModel.dmId}
 * (see {@code PersonEmployee_Cm.json} / {@code PersonEmployee_Ad.json} in {@code testing/workspaces}).
 * This is therefore a reverse lookup across every Combination Model in the project, purely for editor
 * preview purposes - not a persisted or semantically authoritative relationship, and a12-studio has no
 * real Document Model join/expansion engine (unlike SME's kernel-backed
 * {@code DocumentModelJoiningService}) to compute the actual combined result.
 */
public final class AdditiveDocumentModels {

  private AdditiveDocumentModels() {
  }

  /**
   * The base Document Model of the first Combination Model found (by project tree order) whose content
   * has an Addition step referencing {@code additiveModel}, or empty if no Combination Model uses it yet,
   * or its base model id doesn't resolve to an actual Document Model in the project. If more than one
   * Combination Model uses the same Additive Document Model, the first one found wins - see the class
   * javadoc for why that ambiguity is acceptable here.
   */
  public static Optional<DocumentModel> findBaseModel(@NonNull ProjectItem projectItem, @NonNull DocumentModel additiveModel) {
    String additiveId = additiveModel.getId();
    if (additiveId == null) {
      return Optional.empty();
    }

    for (A12Model<?> model : ProjectDocumentModels.getOtherModelsOfType(projectItem, ModelType.COMBINATION)) {
      if (!(model instanceof CombinedDocumentModel combinedModel) || combinedModel.getContent() == null) {
        continue;
      }
      boolean addsThisModel = combinedModel.getContent().getCombinationSteps().stream()
          .map(CombinationStep::getAdditiveModel)
          .filter(Objects::nonNull)
          .anyMatch(additiveRef -> additiveId.equals(additiveRef.getDmId()));
      if (!addsThisModel) {
        continue;
      }
      Optional<DocumentModel> baseModel = findDocumentModelById(projectItem, combinedModel.getContent().getBaseModelId());
      if (baseModel.isPresent()) {
        return baseModel;
      }
    }
    return Optional.empty();
  }

  private static Optional<DocumentModel> findDocumentModelById(@NonNull ProjectItem projectItem, String modelId) {
    if (modelId == null) {
      return Optional.empty();
    }
    return ProjectDocumentModels.getOtherDocumentModels(projectItem).stream()
        .filter(dm -> modelId.equals(dm.getId()))
        .findFirst();
  }
}
