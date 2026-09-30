package de.a12.studio.ui.editors.combineddocumentmodel;

import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;

/**
 * Read-only rendering of one element in {@link CombinationPreviewPanelController}'s tree: icon + name, the
 * same idiom as {@code formmodel.documenttree.FormSourceElementTreeCell} (itself a plain {@link TreeCell}
 * copy of {@code documentmodel.ElementNameTreeCell} for the same reason - that class is {@code
 * TreeTableCell}-based and package-private) - this tree has no type column, rename or validation-error
 * styling to show either, so a small dedicated cell is simpler than adapting either one.
 */
class CombinationPreviewElementTreeCell extends TreeCell<ElementViewModel> {

  @Override
  protected void updateItem(ElementViewModel item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
      return;
    }

    Node icon = WidgetFactory.createIcon(item.getIcon());
    icon.getStyleClass().add("tree-icon");
    String type = item.getType();
    if (type != null) {
      Tooltip.install(icon, WidgetFactory.createTooltip(type));
    }
    Label nameLabel = new Label(item.getName());
    nameLabel.getStyleClass().add("tree-cell-name-label");
    HBox graphic = new HBox(4, icon, nameLabel);
    graphic.setAlignment(Pos.CENTER_LEFT);
    setText(null);
    setGraphic(graphic);
  }
}
