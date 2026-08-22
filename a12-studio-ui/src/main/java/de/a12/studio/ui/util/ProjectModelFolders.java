package de.a12.studio.ui.util;

import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Resolves the folder a "New Model" dialog should default to when the user hasn't explicitly chosen one (e.g.
 * the toolbar "New" button with nothing selected in the project tree, or a right-click on the project root
 * itself) — both currently fall back to the project root via {@code ProjectTreeController#resolveTargetFolder}.
 * Explicit folder selections (right-clicking a specific folder) bypass this and are used as-is.
 */
public final class ProjectModelFolders {

  private static final String MODELS_FOLDER_NAME = "models";

  private ProjectModelFolders() {
  }

  /**
   * Prefers a folder named "models" anywhere under {@code projectRoot} (shallowest, then first encountered);
   * falls back to the folder containing the first existing model found in the project; falls back to {@code
   * projectRoot} itself if the project has neither.
   */
  public static ProjectItem resolveDefaultModelFolder(@NonNull ProjectItem projectRoot) {
    ProjectItem modelsFolder = findModelsFolder(projectRoot);
    if (modelsFolder != null) {
      return modelsFolder;
    }

    ProjectItem firstModelParent = findFirstModelParent(projectRoot);
    return firstModelParent != null ? firstModelParent : projectRoot;
  }

  private static ProjectItem findModelsFolder(@NonNull ProjectItem root) {
    Deque<ProjectItem> queue = new ArrayDeque<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      ProjectItem item = queue.poll();
      for (ProjectItem child : item.getChildren()) {
        if (child.isFolder()) {
          if (child.getName().equalsIgnoreCase(MODELS_FOLDER_NAME)) {
            return child;
          }
          queue.add(child);
        }
      }
    }
    return null;
  }

  private static ProjectItem findFirstModelParent(@NonNull ProjectItem root) {
    Deque<ProjectItem> queue = new ArrayDeque<>();
    queue.add(root);
    while (!queue.isEmpty()) {
      ProjectItem item = queue.poll();
      for (ProjectItem child : item.getChildren()) {
        if (child.isFolder()) {
          queue.add(child);
        }
        else if (child.getModel() != null) {
          return item;
        }
      }
    }
    return null;
  }
}
