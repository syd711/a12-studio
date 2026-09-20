package de.a12.studio.ui.util;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.combineddocumentmodel.CombinedDocumentModelElements;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelHeterogeneity;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Looks up sibling {@link DocumentModel}s in the same project, needed for cross-model settings validation
 * (e.g. the time zone of every document model in a project must agree).
 */
public final class ProjectDocumentModels {

  private ProjectDocumentModels() {
  }

  /**
   * {@link #getOtherDocumentModels}, plus for every Combination Model of the project the synthetic Document Model
   * that stands in for it (see {@link CombinedDocumentModelElements}; it carries the combination's own id, so a
   * reference to the combination resolves to it). For whoever has to follow a Document Model reference that may
   * name a combination - e.g. the Document Model of a Form Model bound to one.
   */
  public static List<DocumentModel> getOtherDocumentModelsWithCombinations(@NonNull ProjectItem projectItem) {
    List<DocumentModel> result = new ArrayList<>(getOtherDocumentModels(projectItem));
    for (A12Model<?> combination : getOtherModelsOfType(projectItem, ModelType.COMBINATION)) {
      DocumentModel standIn = resolveDocumentModelForFieldReferences(combination.getId());
      if (standIn != null) {
        result.add(standIn);
      }
    }
    return result;
  }

  /**
   * Every {@link DocumentModel} in {@code projectItem}'s project, excluding {@code projectItem} itself.
   * <p>
   * Walks from the project's canonical root ({@link Studio#getCurrentProject()}) rather than {@code
   * projectItem}'s own parent chain, since a {@link ProjectItem} backing a restored editor tab is
   * reconstructed standalone from disk (see {@code TabPaneController#projectOpened}) and has no parent link.
   */
  public static List<DocumentModel> getOtherDocumentModels(@NonNull ProjectItem projectItem) {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return List.of();
    }

    List<DocumentModel> result = new ArrayList<>();
    collectDocumentModels(project.getRoot(), projectItem.getPath(), result);
    return result;
  }

  private static void collectDocumentModels(@NonNull ProjectItem item, @NonNull String excludedPath, @NonNull List<DocumentModel> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectDocumentModels(child, excludedPath, result);
      }
    }
    else if (!item.getPath().equals(excludedPath) && item.getModel() instanceof DocumentModel documentModel) {
      result.add(documentModel);
    }
  }

  /**
   * Every model of the given {@link ModelType} in {@code projectItem}'s project, excluding {@code
   * projectItem} itself, sorted by id. Unlike {@link #getOtherDocumentModels}, not limited to {@link
   * DocumentModel}: used by {@link de.a12.studio.ui.editors.propertyeditors.ModelReferencesPanelController}
   * to offer every model a header {@code ModelReference} could point at, whatever its type.
   */
  public static List<A12Model<?>> getOtherModelsOfType(@NonNull ProjectItem projectItem, ModelType modelType) {
    Project project = Studio.getCurrentProject();
    if (project == null || modelType == null) {
      return List.of();
    }

    List<A12Model<?>> result = new ArrayList<>();
    collectModelsOfType(project.getRoot(), projectItem.getPath(), modelType, result);
    result.sort((a, b) -> a.getId().compareTo(b.getId()));
    return result;
  }

  private static void collectModelsOfType(@NonNull ProjectItem item, @NonNull String excludedPath, @NonNull ModelType modelType, @NonNull List<A12Model<?>> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectModelsOfType(child, excludedPath, modelType, result);
      }
    }
    else if (!item.getPath().equals(excludedPath) && item.getModel() != null && item.getModel().getModelType() == modelType) {
      result.add(item.getModel());
    }
  }

  /**
   * Every {@link RelationshipModel} in {@code projectItem}'s project, sorted by id. Unfiltered when {@code
   * documentModelId} is {@code null}; otherwise limited to relationships with an {@link EntityCharacteristic}
   * whose {@code documentModel} is {@code documentModelId} or one of its reachable super types (see {@link
   * DocumentModelHeterogeneity}) - the Form Model "Relationships" panel's candidate list ({@link
   * de.a12.studio.ui.editors.formmodel.RelationshipModelPanelController}), same as SME's {@code
   * calculateRelationshipModelListData}. A Combination Model id has no super types there, so only relationships
   * naming the combination itself match. Mirrors the same relationship lookup {@code
   * de.a12.studio.ui.editors.querymodel.QueryTraversalOption} already does per-role; this variant dedups to one
   * row per relationship model instead of one per role.
   */
  public static List<RelationshipModel> getRelationshipModelsConnectedTo(@NonNull ProjectItem projectItem, @Nullable String documentModelId) {
    List<A12Model<?>> models = getOtherModelsOfType(projectItem, ModelType.RELATIONSHIP);
    Set<String> relevantDocumentModelIds = new HashSet<>();
    if (documentModelId != null) {
      relevantDocumentModelIds.add(documentModelId);
      relevantDocumentModelIds.addAll(
          DocumentModelHeterogeneity.reachableSuperTypes(getOtherDocumentModels(projectItem), documentModelId));
    }
    List<RelationshipModel> result = new ArrayList<>();
    for (A12Model<?> model : models) {
      if (!(model instanceof RelationshipModel relationshipModel) || relationshipModel.getContent() == null) {
        continue;
      }
      if (documentModelId == null || relationshipModel.getContent().getEntityCharacteristics().stream()
          .map(EntityCharacteristic::getDocumentModel)
          .anyMatch(relevantDocumentModelIds::contains)) {
        result.add(relationshipModel);
      }
    }
    result.sort(Comparator.comparing(A12Model::getId));
    return result;
  }

  /**
   * Resolves {@code modelId} to the {@link DocumentModel} to use for element-reference lookups: the model
   * itself if {@code modelId} names a plain Document Model, or - if it names a Combination Model instead
   * (e.g. an Overview Model's {@code document-model-for-overview} reference pointing at a {@code
   * PersonEmployee_Cm}-shaped combined document, see {@code PersonEmployee_Ov.json}) - a synthetic merge of
   * that Combination Model's base Document Model and every {@code Addition} step's additive model (see
   * {@link CombinedDocumentModelElements}). Resolved from the project's canonical root ({@link
   * Studio#getCurrentProject()}, see {@link #getOtherDocumentModels} for why), not {@code modelId}'s own
   * parent chain. {@code null} if {@code modelId} is {@code null}, no project is open, {@code modelId}
   * doesn't resolve to any model in the project, or resolves to something that's neither.
   */
  public static DocumentModel resolveDocumentModelForFieldReferences(String modelId) {
    Project project = Studio.getCurrentProject();
    if (project == null || modelId == null) {
      return null;
    }
    return CombinedDocumentModelElements.resolveForFieldReferences(project.getRoot(), modelId);
  }

  /**
   * The {@link ProjectItem} backing the model (of any {@link ModelType}) with the given id, searched from the
   * project's canonical root (see {@link #getOtherDocumentModels} for why). Used to open a referenced model
   * (e.g. an Include's target, or an Overview Model's Query/Document Model reference) in an editor tab, which
   * needs the {@link ProjectItem} rather than just the model.
   */
  public static Optional<ProjectItem> findProjectItemByModelId(@NonNull String modelId) {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return Optional.empty();
    }
    return findByModelId(project.getRoot(), modelId);
  }

  private static Optional<ProjectItem> findByModelId(@NonNull ProjectItem item, @NonNull String modelId) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        Optional<ProjectItem> found = findByModelId(child, modelId);
        if (found.isPresent()) {
          return found;
        }
      }
      return Optional.empty();
    }
    if (item.getModel() != null && modelId.equals(item.getModel().getId())) {
      return Optional.of(item);
    }
    return Optional.empty();
  }

  /**
   * Opens the model referenced by {@code modelId} in an editor tab, selecting its tab instead if it's already
   * open (see {@code TabPaneController#modelOpened}). Does nothing if no model with that id exists in the
   * current project. Shared by every "edit reference" button across the property editors (e.g. {@link
   * de.a12.studio.ui.editors.propertyeditors.TargetModelPanelController}, {@link
   * de.a12.studio.ui.editors.maindetailmodel.MainModelReferencePanelController}, {@link
   * de.a12.studio.ui.editors.documentmodel.IncludePropertiesPanelController}, {@link
   * de.a12.studio.ui.editors.overviewmodel.OverviewReferencePanelController}).
   */
  public static void openModelInEditor(@NonNull String modelId) {
    findProjectItemByModelId(modelId).ifPresent(ProjectDocumentModels::openModelInEditor);
  }

  /**
   * Opens {@code item} in an editor tab, selecting its tab instead if it's already open, adding it to the
   * project's opened-files list and firing a {@code ModelOpenedEvent} so the tab pane picks it up.
   */
  public static void openModelInEditor(@NonNull ProjectItem item) {
    Project project = Studio.getCurrentProject();
    if (project != null && item.isModelSupported()) {
      project.getSettings().getUISettings().addOpenedFile(item.getPath());
      project.getSettings().getUISettings().save();
    }
    StudioEventManager.getInstance().fireModelOpenEvent(item);
  }
}
