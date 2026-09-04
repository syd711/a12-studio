package de.a12.studio.ui.util;

import de.a12.studio.models.projects.ProjectItem;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;

/**
 * Resolves the folder a "New Model" (or model-import) dialog should default to, and drives the
 * combobox-based location chooser shared by those dialogs — the toolbar "New" button with
 * nothing selected in the project tree, a right-click on the project root itself, and the
 * "Create from Excel"/"Create from Access Database" plugin dialogs all fall back to or offer this
 * same default via {@code ProjectTreeController#resolveTargetFolder} / {@link #configureLocationCombo}.
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

  /**
   * Every folder in {@code projectRoot}'s tree, including {@code projectRoot} itself, sorted by absolute path
   * so {@code projectRoot} (a prefix of every descendant's path) always sorts first. Used to populate the "New
   * Model" dialog's location combo box, letting the user target any project folder rather than being locked to
   * wherever the dialog was opened from.
   */
  public static List<ProjectItem> listAllFolders(@NonNull ProjectItem projectRoot) {
    List<ProjectItem> folders = new ArrayList<>();
    collectFolders(projectRoot, folders);
    folders.sort(Comparator.comparing(ProjectItem::getPath));
    return folders;
  }

  /**
   * Wires a "Location" {@link ComboBox} with every folder in {@code targetFolder}'s project, displaying each
   * as its path relative to the project root ("/" for the root itself), and defaults its value to the folder
   * named "models" if one exists anywhere in the tree, else the project root itself (first in the sorted list
   * returned by {@link #listAllFolders}).
   *
   * <p>Shared by {@code NewModelDialogController} and the "Create from Excel"/"Create from Access Database"
   * plugin dialogs so all three offer the same folder-picking UI and default.
   */
  public static void configureLocationCombo(@NonNull ComboBox<ProjectItem> combo, @NonNull ProjectItem targetFolder) {
    ProjectItem root = findProjectRoot(targetFolder);
    List<ProjectItem> folders = listAllFolders(root);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(ProjectItem folder) {
        return folder == null ? "" : displayLocation(root, folder);
      }

      @Override
      public ProjectItem fromString(String string) {
        return null;
      }
    });
    combo.getItems().setAll(folders);
    ProjectItem defaultFolder = folders.stream()
        .filter(folder -> folder.getName().equalsIgnoreCase(MODELS_FOLDER_NAME))
        .findFirst()
        .orElse(folders.get(0));
    combo.setValue(defaultFolder);
  }

  private static ProjectItem findProjectRoot(@NonNull ProjectItem item) {
    ProjectItem root = item;
    while (root.getParent() != null) {
      root = root.getParent();
    }
    return root;
  }

  private static String displayLocation(@NonNull ProjectItem projectRoot, @NonNull ProjectItem folder) {
    if (folder.equals(projectRoot)) {
      return "/";
    }
    Path relative = projectRoot.getFile().toPath().toAbsolutePath().relativize(folder.getFile().toPath().toAbsolutePath());
    return relative.toString().replace(File.separatorChar, '/');
  }

  private static void collectFolders(@NonNull ProjectItem item, @NonNull List<ProjectItem> result) {
    result.add(item);
    for (ProjectItem child : item.getChildren()) {
      if (child.isFolder()) {
        collectFolders(child, result);
      }
    }
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
