package de.a12.studio.plugin.manager;

import de.a12.studio.models.projects.Project;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

/**
 * Extension point interface for plugins that contribute a button to the project tree's toolbar
 * (the bar above the project tree that also holds the built-in "New", "Reload", "Expand All" and
 * "Collapse All" buttons).
 *
 * <p>Implement this interface and register it in your {@code plugin.json} under the extension
 * point name {@code "projectToolbarButton"} to have your button appended to that toolbar,
 * separated from the built-in buttons by a vertical separator.
 *
 * <p>Example plugin.json entry:
 * <pre>
 * {
 *   "extensionPoints": [
 *     { "name": "projectToolbarButton", "class": "com.example.plugin.MyToolbarButton" }
 *   ]
 * }
 * </pre>
 */
public interface IProjectToolbarButtonContribution {

  /**
   * Graphic shown on the button (e.g. a {@code FontIcon}). The button carries no text label, so
   * this must not be {@code null}.
   *
   * @return graphic node; must not be {@code null}
   */
  @NonNull
  Node getGraphic();

  /**
   * Tooltip text shown when hovering over the button.
   *
   * @return human-readable tooltip text; must not be {@code null} or blank
   */
  @NonNull
  String getTooltip();

  /**
   * Called when the user clicks the button.
   *
   * @param owner   the owner {@link Stage} to use as parent for any dialogs
   * @param project the currently open project
   */
  void execute(@NonNull Stage owner, @NonNull Project project);
}
