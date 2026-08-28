package de.a12.studio.plugin.manager;

import de.a12.studio.models.projects.ProjectItem;
import org.jspecify.annotations.NonNull;

/**
 * Extension point interface for plugins that need to react whenever a model is about to be saved.
 *
 * <p>Implement this interface and register it in your {@code plugin.json} under the extension
 * point name {@code "modelSave"}. {@link #beforeSave(ProjectItem)} is called synchronously right
 * before every {@code ProjectItem.save()} call across the application (property-panel commits,
 * explicit editor saves, renames, copies, ...), so implementations must be fast and must not throw.
 *
 * <p>Example plugin.json entry:
 * <pre>
 * {
 *   "extensionPoints": [
 *     { "name": "modelSave", "class": "com.example.plugin.MySaveInterceptor" }
 *   ]
 * }
 * </pre>
 */
public interface IModelSaveInterceptor {

  /**
   * Called right before {@code item} is written to disk. Implementations may mutate the item's
   * model or even rename the item (e.g. via {@code ProjectItem.renameTo(...)}) before the write
   * happens.
   *
   * @param item the item about to be saved; never a folder
   */
  void beforeSave(@NonNull ProjectItem item);
}
