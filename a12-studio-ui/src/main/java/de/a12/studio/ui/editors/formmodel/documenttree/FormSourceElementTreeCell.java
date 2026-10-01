package de.a12.studio.ui.editors.formmodel.documenttree;

import de.a12.studio.ui.editors.documentmodel.ElementViewModel;
import de.a12.studio.ui.editors.formmodel.formtree.FormDependencyBadges;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;

import java.util.function.Function;

/**
 * Read-only rendering of one Document Model element in {@link DocumentSourceTreeController}'s tree: icon + name,
 * the same visual idiom as {@code documentmodel.ElementNameTreeCell} but as a plain {@link TreeCell} - that
 * class is {@code TreeTableCell}-based and package-private, so not reusable here, and this tree has no "type"
 * column or validation-error styling to show, so a small dedicated cell is simpler than adapting it.
 */
class FormSourceElementTreeCell extends TreeCell<ElementViewModel> {

  private final Function<String, DocumentSourceTreeController.Marks> marks;

  FormSourceElementTreeCell(Function<String, DocumentSourceTreeController.Marks> marks) {
    this.marks = marks;
  }

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
    DocumentSourceTreeController.Marks elementMarks = marks.apply(item.getElement().getId());
    if (elementMarks != null) {
      Node badges = FormDependencyBadges.createBadges(elementMarks.dependentOn(), elementMarks.masterOf());
      if (badges != null) {
        graphic.getChildren().add(badges);
      }
    }
    graphic.setAlignment(Pos.CENTER_LEFT);
    setText(null);
    setGraphic(graphic);
  }
}
