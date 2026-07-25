package de.a12.studio.ui.util;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Looks up sibling {@link DocumentModel}s in the same project, needed for cross-model settings validation
 * (e.g. the time zone of every document model in a project must agree).
 */
public final class ProjectDocumentModels {

  private ProjectDocumentModels() {
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
   * The {@link ProjectItem} backing the {@link DocumentModel} with the given id, searched from the project's
   * canonical root (see {@link #getOtherDocumentModels} for why). Used to open a referenced model (e.g. an
   * Include's target) in an editor tab, which needs the {@link ProjectItem} rather than just the model.
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
    if (item.getModel() instanceof DocumentModel documentModel && modelId.equals(documentModel.getId())) {
      return Optional.of(item);
    }
    return Optional.empty();
  }
}
