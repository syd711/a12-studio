package de.a12.studio.plugin.manager;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * Extension point interface for plugins that need to adjust the filename of a model about to be
 * created.
 *
 * <p>Implement this interface and register it in your {@code plugin.json} under the extension
 * point name {@code "newModelName"}. {@link #adjustName(ProjectItem, ModelType, String)} is called
 * right before the new model file is created, for every "New Model" entry point (the project tree
 * "New" menu, plugin-contributed import wizards, ...).
 *
 * <p>Example plugin.json entry:
 * <pre>
 * {
 *   "extensionPoints": [
 *     { "name": "newModelName", "class": "com.example.plugin.MyNameInterceptor" }
 *   ]
 * }
 * </pre>
 */
public interface INewModelNameInterceptor {

  /**
   * Returns the filename (without extension) to actually use for the new model.
   *
   * @param targetFolder  the folder the new model will be created in
   * @param modelType     the type of model being created; may be {@code null} if not yet known
   * @param proposedName  the filename the user typed (or the caller proposed)
   * @return the filename to use; return {@code proposedName} unchanged to leave it as-is
   */
  @NonNull
  String adjustName(@NonNull ProjectItem targetFolder, @Nullable ModelType modelType, @NonNull String proposedName);
}
