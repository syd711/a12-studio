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
import java.util.function.BooleanSupplier;

/**
 * The context menu of the content model's element tree: every toolbar action, each entry enabled exactly when its
 * toolbar button is, plus the actions that have no toolbar button (adding and pasting above or below an element). The
 * items are created once (a context menu without items never opens); only their enabled state is refreshed on showing.
 */
final class ContentModelTreeContextMenu {

  /** An action: when it is disabled (for a toolbar action: whenever its button is), and what the entry does. */
  record Action(BooleanSupplier disabled, EventHandler<ActionEvent> handler) {

    /** A toolbar action: the entry mirrors the enabled state of {@code button}. */
    static Action of(Button button, EventHandler<ActionEvent> handler) {
      return new Action(button::isDisable, handler);
    }
  }

  /** The actions the menu offers. */
  record Actions(Action undo, Action redo, Action add, Action addAbove, Action addBelow, Action delete, Action moveUp,
                 Action moveDown, Action cut, Action copy, Action paste, Action pasteAbove, Action pasteBelow,
                 Action duplicate) {
  }

  private ContentModelTreeContextMenu() {
  }

  /**
   * @param beforeShowing refreshes the toolbar buttons' enabled state; runs each time the menu is about to show,
   *                      before the entries copy that state
   */
  static ContextMenu create(Runnable beforeShowing, Actions actions) {
    Map<MenuItem, BooleanSupplier> mirrored = new LinkedHashMap<>();
    ContextMenu menu = new ContextMenu();
    menu.getItems().addAll(
        item(mirrored, "content_model_tree.add_child", Icons.PLUS, actions.add()),
        item(mirrored, "content_model_tree.add_above", Icons.PLUS, actions.addAbove()),
        item(mirrored, "content_model_tree.add_below", Icons.PLUS, actions.addBelow()),
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
        item(mirrored, "content_model_tree.paste_above", Icons.PASTE, actions.pasteAbove()),
        item(mirrored, "content_model_tree.paste_below", Icons.PASTE, actions.pasteBelow()),
        item(mirrored, "content_model_tree.duplicate", Icons.COPY, actions.duplicate()));
    menu.setOnShowing(event -> {
      beforeShowing.run();
      mirrored.forEach((item, disabled) -> item.setDisable(disabled.getAsBoolean()));
    });
    return menu;
  }

  private static MenuItem item(Map<MenuItem, BooleanSupplier> mirrored, String bundleKey, String icon, Action action) {
    MenuItem item = new MenuItem(StudioBundle.get(bundleKey));
    FontIcon fontIcon = WidgetFactory.createIcon(icon);
    fontIcon.getStyleClass().add("menu-icon");
    item.setGraphic(fontIcon);
    item.setOnAction(action.handler());
    mirrored.put(item, action.disabled());
    return item;
  }
}
