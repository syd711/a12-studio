package de.a12.studio.plugin.manager;

import de.a12.studio.models.projects.Project;
import javafx.scene.Node;
import org.jspecify.annotations.NonNull;

/**
 * Extension point interface for plugins that contribute a page to the "Project Settings" section
 * of the Preferences dialog (as opposed to the app-wide "A12 Studio Settings" section).
 *
 * <p>Implement this interface and register it in your {@code plugin.json} under the extension
 * point name {@code "projectSettingsPanel"} to have your entry appear in the project-settings nav.
 *
 * <p>Example plugin.json entry:
 * <pre>
 * {
 *   "extensionPoints": [
 *     { "name": "projectSettingsPanel", "class": "com.example.plugin.MySettingsPanel" }
 *   ]
 * }
 * </pre>
 */
public interface IProjectSettingsPanelContribution {

  /**
   * The label shown in the project-settings nav button.
   *
   * @return human-readable label; must not be {@code null} or blank
   */
  @NonNull
  String getLabel();

  /**
   * Optional graphic node for the nav button (e.g. a {@code FontIcon}).
   * Return {@code null} to show no icon.
   *
   * @return graphic node, or {@code null}
   */
  Node getGraphic();

  /**
   * Builds the panel shown when the nav button is clicked. Called fresh every time the button is
   * clicked, so the returned node may bind directly against the given project's live state.
   *
   * @param project the currently open project
   * @return the panel content; must not be {@code null}
   */
  @NonNull
  Node createPanel(@NonNull Project project);
}
