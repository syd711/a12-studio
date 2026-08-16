package de.a12.studio.ui.editors.querymodel;

import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.HBox;

/**
 * Read-only rendering of one {@link QueryTreeRow}: icon + name for the target Document Model row and Field/Group
 * rows, plain italic text (no icon) for the synthetic "Model Tree" header row. The same visual idiom as {@code
 * documentmodel.ElementNameTreeCell}/{@code formmodel.documenttree.FormSourceElementTreeCell}, but neither is reusable here
 * (package-private, and bound to {@code ElementViewModel} rather than {@link QueryTreeRow}).
 */
class QueryTreeNameCell extends TreeTableCell<QueryTreeRow, String> {

  @Override
  protected void updateItem(String name, boolean empty) {
    super.updateItem(name, empty);
    QueryTreeRow row = empty ? null : getTreeTableRow().getItem();
    if (empty || name == null || row == null) {
      setText(null);
      setGraphic(null);
      return;
    }

    if (row.getKind() == QueryTreeRow.Kind.ROOT_LABEL) {
      Label nameLabel = new Label(name);
      nameLabel.getStyleClass().add("placeholder-label");
      setText(null);
      setGraphic(nameLabel);
      return;
    }

    Node icon = row.getKind() == QueryTreeRow.Kind.TARGET_DOCUMENT_MODEL
        ? WidgetFactory.createModelIcon(row.getIcon())
        : WidgetFactory.createIcon(row.getIcon());
    icon.getStyleClass().add("tree-icon");
    Label nameLabel = new Label(name);
    nameLabel.getStyleClass().add("tree-cell-name-label");
    HBox graphic = new HBox(4, icon, nameLabel);
    graphic.setAlignment(Pos.CENTER_LEFT);
    setText(null);
    setGraphic(graphic);
  }
}
