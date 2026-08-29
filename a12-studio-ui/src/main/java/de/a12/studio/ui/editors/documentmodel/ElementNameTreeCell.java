package de.a12.studio.ui.editors.documentmodel;

import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.Icons;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.HBox;
import de.a12.studio.ui.util.StudioBundle;

import java.util.stream.Collectors;

class ElementNameTreeCell extends TreeTableCell<ElementViewModel, String> {

  @Override
  protected void updateItem(String name, boolean empty) {
    super.updateItem(name, empty);
    if (empty || name == null) {
      setText(null);
      setGraphic(null);
      setTooltip(null);
      getStyleClass().remove("validation-error");
      return;
    }

    ElementViewModel viewModel = getTableRow().getItem();
    if (viewModel == null) {
      setText(name);
      setGraphic(null);
      setTooltip(null);
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
        Tooltip.install(annotationIcon, WidgetFactory.createTooltip(StudioBundle.get("element_has_annotations")));
        graphic.getChildren().add(annotationIcon);
      }
      if (viewModel.isRequired()) {
        Node requiredIcon = WidgetFactory.createIcon(Icons.ELEMENT_REQUIRED);
        requiredIcon.getStyleClass().addAll("tree-icon", "tree-icon-badge");
        Tooltip.install(requiredIcon, WidgetFactory.createTooltip(StudioBundle.get("required_element")));
        graphic.getChildren().add(requiredIcon);
      }
      setText(null);
      setGraphic(graphic);

      if (viewModel.hasError()) {
        if (!getStyleClass().contains("validation-error")) {
          getStyleClass().add("validation-error");
        }
        nameLabel.getStyleClass().add("validation-error");
        String messages = viewModel.getErrorMessages().stream().map(message -> "• " + message).collect(Collectors.joining("\n"));
        setTooltip(WidgetFactory.createTooltip(messages));
      }
      else {
        getStyleClass().remove("validation-error");
        setTooltip(null);
      }
    }
  }
}
