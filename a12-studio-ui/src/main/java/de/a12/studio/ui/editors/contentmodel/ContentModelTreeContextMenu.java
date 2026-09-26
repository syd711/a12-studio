package de.a12.studio.ui.editors.contentmodel;

import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.Button;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import org.kordamp.ikonli.javafx.FontIcon;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * The context menu of the content model's element tree: every toolbar action, each entry enabled exactly when its
 * toolbar button is. The items are created once (a context menu without items never opens); only their enabled state
 * is refreshed on showing.
 */
final class ContentModelTreeContextMenu {

  /** A toolbar action: the button whose enabled state the menu entry mirrors, and what the entry does. */
  record Action(Button button, EventHandler<ActionEvent> handler) {
  }

  /** The toolbar actions the menu offers. */
  record Actions(Action undo, Action redo, Action add, Action delete, Action moveUp, Action moveDown, Action cut,
                 Action copy, Action paste, Action duplicate) {
  }

  private ContentModelTreeContextMenu() {
  }

  /**
   * @param beforeShowing refreshes the toolbar buttons' enabled state; runs each time the menu is about to show,
   *                      before the entries copy that state
   */
  static ContextMenu create(Runnable beforeShowing, Actions actions) {
    Map<MenuItem, Button> mirrored = new LinkedHashMap<>();
    ContextMenu menu = new ContextMenu();
    menu.getItems().addAll(
        item(mirrored, "content_model_tree.add_child", Icons.PLUS, actions.add()),
        new SeparatorMenuItem(),
        item(mirrored, "undo", Icons.UNDO, actions.undo()),
        item(mirrored, "redo", Icons.REDO, actions.redo()),
        new SeparatorMenuItem(),
        item(mirrored, "content_model_tree.delete", Icons.TRASH, actions.delete()),
        new SeparatorMenuItem(),
        item(mirrored, "content_model_tree.move_up", Icons.ARROW_UP, actions.moveUp()),
        item(mirrored, "content_model_tree.move_down", Icons.ARROW_DOWN, actions.moveDown()),
        new SeparatorMenuItem(),
        item(mirrored, "content_model_tree.cut", Icons.CUT, actions.cut()),
        item(mirrored, "content_model_tree.copy", Icons.COPY, actions.copy()),
        item(mirrored, "content_model_tree.paste", Icons.PASTE, actions.paste()),
        item(mirrored, "content_model_tree.duplicate", Icons.COPY, actions.duplicate()));
    menu.setOnShowing(event -> {
      beforeShowing.run();
      mirrored.forEach((item, button) -> item.setDisable(button.isDisable()));
    });
    return menu;
  }

  private static MenuItem item(Map<MenuItem, Button> mirrored, String bundleKey, String icon, Action action) {
    MenuItem item = new MenuItem(StudioBundle.get(bundleKey));
    FontIcon fontIcon = WidgetFactory.createIcon(icon);
    fontIcon.getStyleClass().add("menu-icon");
    item.setGraphic(fontIcon);
    item.setOnAction(action.handler());
    mirrored.put(item, action.button());
    return item;
  }
}
