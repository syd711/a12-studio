package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.ui.util.Icons;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.HBox;

class ElementNameTreeCell extends TreeTableCell<ElementViewModel, String> {

  @Override
  protected void updateItem(String name, boolean empty) {
    super.updateItem(name, empty);
    if (empty || name == null) {
      setText(null);
      setGraphic(null);
      getStyleClass().remove("validation-error");
      return;
    }

    ElementViewModel viewModel = getTableRow().getItem();
    if (viewModel == null) {
      setText(name);
      setGraphic(null);
      getStyleClass().remove("validation-error");
    }
    else {
      Node icon = WidgetFactory.createIcon(viewModel.getIcon());
      icon.getStyleClass().add("tree-icon");

      Label nameLabel = new Label(name);
      nameLabel.getStyleClass().add("tree-cell-name-label");
      HBox graphic = new HBox(4, icon, nameLabel);
      graphic.setAlignment(Pos.CENTER_LEFT);
      if (viewModel.hasAnnotations()) {
        Node annotationIcon = WidgetFactory.createIcon(Icons.ELEMENT_ANNOTATION);
        annotationIcon.getStyleClass().addAll("tree-icon", "tree-icon-badge");
        Tooltip.install(annotationIcon, WidgetFactory.createTooltip("Element has annotations"));
        graphic.getChildren().add(annotationIcon);
      }
      if (viewModel.isRequired()) {
        Node requiredIcon = WidgetFactory.createIcon(Icons.ELEMENT_REQUIRED);
        requiredIcon.getStyleClass().addAll("tree-icon", "tree-icon-badge");
        Tooltip.install(requiredIcon, WidgetFactory.createTooltip("Required element"));
        graphic.getChildren().add(requiredIcon);
      }
      setText(null);
      setGraphic(graphic);

      if (viewModel.hasError()) {
        if (!getStyleClass().contains("validation-error")) {
          getStyleClass().add("validation-error");
        }
      }
      else {
        getStyleClass().remove("validation-error");
      }
    }
  }
}
