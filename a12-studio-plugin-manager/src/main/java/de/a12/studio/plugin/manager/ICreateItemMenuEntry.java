package de.a12.studio.plugin.manager;

import de.a12.studio.models.projects.ProjectItem;
import javafx.scene.Node;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

/**
 * Extension point interface for plugins that contribute entries to the
 * "New &gt; Document Model" create menu in the project tree.
 *
 * <p>Implement this interface and register it in your {@code plugin.json} under the
 * extension point name {@code "createMenu"} to have your entry appear in the menu.
 *
 * <p>Example plugin.json entry:
 * <pre>
 * {
 *   "extensionPoints": [
 *     { "name": "createMenu", "class": "com.example.plugin.MyMenuEntry" }
 *   ]
 * }
 * </pre>
 */
public interface ICreateItemMenuEntry {

  /**
   * The label shown in the menu item.
   *
   * @return human-readable menu item label; must not be {@code null} or blank
   */
  @NonNull
  String getMenuLabel();

  /**
   * Optional graphic node for the menu item (e.g. a {@code FontIcon}).
   * Return {@code null} to show no icon.
   *
   * @return graphic node, or {@code null}
   */
  Node getMenuGraphic();

  /**
   * Called when the user clicks the menu item.
   *
   * @param owner        the owner {@link Stage} to use as parent for any modal dialogs
   * @param targetFolder the project tree folder into which any new model should be placed
   */
  void execute(@NonNull Stage owner, @NonNull ProjectItem targetFolder);
}
