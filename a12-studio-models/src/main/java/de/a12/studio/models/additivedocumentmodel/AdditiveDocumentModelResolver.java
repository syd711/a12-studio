package de.a12.studio.models.additivedocumentmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModel;
import de.a12.studio.models.documentmodel.DocumentModel;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves the Document Model an {@link AdditiveDocumentModel} adds onto, by reverse lookup across every
 * {@link CombinedDocumentModel} the caller can see: an Additive Document Model's own file carries no
 * reference to that base model - only a Combination Model's Addition step does, via its {@code
 * AdditiveModel.dmId} alongside the Combination Model's own {@code baseModelId} (see {@code
 * PersonEmployee_Cm.json} / {@code PersonEmployee_Ad.json} in {@code testing/workspaces}).
 * <p>
 * Lives here (rather than in a12-studio-ui, where the original project-tree-backed lookup was written)
 * so both that UI lookup ({@code AdditiveDocumentModels}, which now delegates to this class) and
 * model-validation's {@code ElementIndex}/{@code MissingReferenceValidator} (which need it to resolve an
 * Additive Document Model's own relative-path fields, e.g. a Computation's {@code computedFieldRelPath}
 * pointing at a field the base model provides) can use the same logic without a12-studio-models-validation
 * having to depend on a12-studio-ui.
 * <p>
 * This is purely a best-effort reverse lookup for editor assistance, not a persisted or semantically
 * authoritative relationship, and a12-studio has no real Document Model join/expansion engine (unlike
 * SME's kernel-backed {@code DocumentModelJoiningService}) to compute the actual combined result. If more
 * than one Combination Model uses the same Additive Document Model, the first one found (in {@code
 * otherModels} order) wins.
 */
public final class AdditiveDocumentModelResolver {

  private AdditiveDocumentModelResolver() {
  }

  /**
   * @param otherModels every other model in the project (of any type) - only its {@link CombinedDocumentModel}
   *                     entries are relevant here.
   * @param otherDocumentModels every other {@link DocumentModel} in the project, used to resolve the winning
   *                     Combination Model's {@code baseModelId} to an actual model.
   */
  public static Optional<DocumentModel> findBaseModel(DocumentModel additiveModel, List<A12Model<?>> otherModels,
      List<DocumentModel> otherDocumentModels) {
    String additiveId = additiveModel.getId();
    if (additiveId == null) {
      return Optional.empty();
    }

    for (A12Model<?> model : otherModels) {
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
      Optional<DocumentModel> baseModel = findDocumentModelById(otherDocumentModels, combinedModel.getContent().getBaseModelId());
      if (baseModel.isPresent()) {
        return baseModel;
      }
    }
    return Optional.empty();
  }

  private static Optional<DocumentModel> findDocumentModelById(List<DocumentModel> otherDocumentModels, String modelId) {
    if (modelId == null) {
      return Optional.empty();
    }
    return otherDocumentModels.stream().filter(dm -> modelId.equals(dm.getId())).findFirst();
  }
}
