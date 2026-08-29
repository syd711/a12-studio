package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.TreeCell;
import javafx.scene.layout.HBox;

import java.util.function.Function;
import java.util.stream.Collectors;

/** Renders one Form Model tree node: icon + name, plus a per-node context menu built by {@link FormModelActions}. */
class FormModelTreeCell extends TreeCell<FormElementViewModel> {

  private final Function<FormElementViewModel, ContextMenu> contextMenuFactory;

  FormModelTreeCell(Function<FormElementViewModel, ContextMenu> contextMenuFactory) {
    this.contextMenuFactory = contextMenuFactory;
  }

  @Override
  protected void updateItem(FormElementViewModel item, boolean empty) {
    super.updateItem(item, empty);
    if (empty || item == null) {
      setText(null);
      setGraphic(null);
      setContextMenu(null);
      setTooltip(null);
      getStyleClass().remove("validation-error");
      return;
    }

    Node icon = WidgetFactory.createIcon(item.getIcon());
    icon.getStyleClass().add("tree-icon");
    Label nameLabel = new Label(item.getName());
    nameLabel.getStyleClass().add("tree-cell-name-label");
    HBox graphic = new HBox(4, icon, nameLabel);
    graphic.setAlignment(Pos.CENTER_LEFT);
    setText(null);
    setGraphic(graphic);
    setContextMenu(contextMenuFactory.apply(item));

    if (item.hasError()) {
      if (!getStyleClass().contains("validation-error")) {
        getStyleClass().add("validation-error");
      }
      nameLabel.getStyleClass().add("validation-error");
      String messages = item.getErrorMessages().stream().map(message -> "• " + message).collect(Collectors.joining("\n"));
      setTooltip(WidgetFactory.createTooltip(messages));
    }
    else {
      getStyleClass().remove("validation-error");
      setTooltip(null);
    }
  }
}
