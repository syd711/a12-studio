package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
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

  /** SME's "dependent on" / "triggering dependency" marks: a "D" and/or "T" badge (with a count above one) whose
   * tooltip lists the related elements. */
  private static void addDependencyBadges(HBox graphic, FormElementViewModel item) {
    if (item.getDependentOn().isEmpty() && item.getMasterOf().isEmpty()) {
      return;
    }
    StringBuilder tooltip = new StringBuilder();
    appendDependencies(tooltip, StudioBundle.get("form_model_tree.dependent_on"), item.getDependentOn());
    appendDependencies(tooltip, StudioBundle.get("form_model_tree.triggering_dependency"), item.getMasterOf());
    HBox badges = new HBox(2);
    badges.setAlignment(Pos.CENTER_LEFT);
    if (!item.getDependentOn().isEmpty()) {
      badges.getChildren().add(createBadge("D", item.getDependentOn().size(), "tree-dependency-badge-dependent"));
    }
    if (!item.getMasterOf().isEmpty()) {
      badges.getChildren().add(createBadge("T", item.getMasterOf().size(), "tree-dependency-badge-master"));
    }
    Tooltip.install(badges, WidgetFactory.createTooltip(tooltip.toString().stripTrailing()));
    graphic.getChildren().add(badges);
  }

  private static Label createBadge(String letter, int count, String styleClass) {
    Label badge = new Label(count > 1 ? letter + count : letter);
    badge.getStyleClass().addAll("tree-dependency-badge", styleClass);
    return badge;
  }

  private static void appendDependencies(StringBuilder tooltip, String headline, java.util.List<FormDependencyBadges.Entry> entries) {
    if (entries.isEmpty()) {
      return;
    }
    tooltip.append(headline).append('\n');
    for (FormDependencyBadges.Entry entry : entries) {
      tooltip.append("  ").append(entry.path()).append(" ")
          .append(StudioBundle.get("form_model_tree.dependency_kind", entry.kind())).append('\n');
    }
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

    Node icon = item.isModelReferenceIcon()
        ? WidgetFactory.createModelIcon(item.getIcon())
        : WidgetFactory.createIcon(item.getIcon());
    icon.getStyleClass().add("tree-icon");
    Tooltip.install(icon, WidgetFactory.createTooltip(item.getTypeLabel()));
    Label nameLabel = new Label(item.getName());
    nameLabel.getStyleClass().add("tree-cell-name-label");
    HBox graphic = new HBox(4, icon, nameLabel);
    if (item.getNode() instanceof ScreenElement element && element.getIncludeId() != null && !element.getIncludeId().isEmpty()
        && element.getFormModelRef() != null && !element.getFormModelRef().isEmpty()) {
      // Marks what an include was expanded from, like SME's "link" icon on included elements.
      Node includeIcon = WidgetFactory.createIcon(Icons.ELEMENT_INCLUDE);
      includeIcon.getStyleClass().add("tree-icon");
      Tooltip.install(includeIcon, WidgetFactory.createTooltip(StudioBundle.get("form_model_tree.included_from", element.getFormModelRef())));
      graphic.getChildren().add(1, includeIcon);
    }
    addDependencyBadges(graphic, item);
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
