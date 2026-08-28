package de.a12.studio.plugin.manager;

import de.a12.studio.models.projects.Project;
import org.jspecify.annotations.NonNull;

/**
 * Extension point interface for plugins that need to react whenever a project is opened.
 *
 * <p>Implement this interface and register it in your {@code plugin.json} under the extension
 * point name {@code "projectOpened"}. {@link #onProjectOpened(Project)} is called once, on a
 * background thread, right after the project has finished loading and before the project-open
 * event is dispatched to the rest of the UI.
 *
 * <p>Example plugin.json entry:
 * <pre>
 * {
 *   "extensionPoints": [
 *     { "name": "projectOpened", "class": "com.example.plugin.MyProjectOpenedListener" }
 *   ]
 * }
 * </pre>
 */
public interface IProjectOpenedListener {

  /**
   * Called after {@code project} has finished loading.
   *
   * @param project the project that was just opened
   */
  void onProjectOpened(@NonNull Project project);
}
