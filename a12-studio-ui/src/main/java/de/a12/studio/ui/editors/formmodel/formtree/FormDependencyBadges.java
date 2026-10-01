package de.a12.studio.ui.editors.formmodel.formtree;

import de.a12.studio.models.formmodel.Control;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * SME's dependency decoration of the Form Model tree ({@code dependencySuffix.tsx}): a control that triggers
 * other screen elements through its {@code dependentControls} is marked as a trigger and lists them, and each
 * such screen element is marked as dependent on the control. Controls themselves are never dependents.
 *
 * <p>Works on the built tree (paths are the display names of the ancestors), so it has to be re-applied after
 * every rebuild - see {@link FormModelTreeController}.
 */
public final class FormDependencyBadges {

  public static final String UNKNOWN_PATH = "<Unknown>";

  private FormDependencyBadges() {
  }

  /** One related element: its tree path and what kind of dependency it is ({@code Control}). */
  public record Entry(String path, String kind) {
  }

  /** SME's "dependent on" / "triggering dependency" marks: a "D" and/or "T" badge (with a count above one) whose
   * tooltip lists the related elements; null if there is nothing to show. */
  public static Node createBadges(List<Entry> dependentOn, List<Entry> masterOf) {
    if (dependentOn.isEmpty() && masterOf.isEmpty()) {
      return null;
    }
    StringBuilder tooltip = new StringBuilder();
    appendDependencies(tooltip, StudioBundle.get("form_model_tree.dependent_on"), dependentOn);
    appendDependencies(tooltip, StudioBundle.get("form_model_tree.triggering_dependency"), masterOf);
    HBox badges = new HBox(2);
    badges.setAlignment(Pos.CENTER_LEFT);
    if (!dependentOn.isEmpty()) {
      badges.getChildren().add(createBadge("D", dependentOn.size(), "tree-dependency-badge-dependent"));
    }
    if (!masterOf.isEmpty()) {
      badges.getChildren().add(createBadge("T", masterOf.size(), "tree-dependency-badge-master"));
    }
    Tooltip.install(badges, WidgetFactory.createTooltip(tooltip.toString().stripTrailing()));
    return badges;
  }

  private static Label createBadge(String letter, int count, String styleClass) {
    Label badge = new Label(count > 1 ? letter + count : letter);
    badge.getStyleClass().addAll("tree-dependency-badge", styleClass);
    return badge;
  }

  private static void appendDependencies(StringBuilder tooltip, String headline, List<Entry> entries) {
    if (entries.isEmpty()) {
      return;
    }
    tooltip.append(headline).append('\n');
    for (Entry entry : entries) {
      tooltip.append("  ").append(entry.path()).append(" ")
          .append(StudioBundle.get("form_model_tree.dependency_kind", entry.kind())).append('\n');
    }
  }

  /** Sets {@link FormElementViewModel#getDependentOn()}/{@link FormElementViewModel#getMasterOf()} on every row. */
  static void apply(TreeItem<FormElementViewModel> root) {
    List<TreeItem<FormElementViewModel>> items = new ArrayList<>();
    collect(root, items);

    Map<String, TreeItem<FormElementViewModel>> byId = new LinkedHashMap<>();
    for (TreeItem<FormElementViewModel> item : items) {
      item.getValue().setDependencies(List.of(), List.of());
      String id = item.getValue().getId();
      if (id != null) {
        byId.putIfAbsent(id, item);
      }
    }

    Map<TreeItem<FormElementViewModel>, List<Entry>> dependentOn = new LinkedHashMap<>();
    Map<TreeItem<FormElementViewModel>, List<Entry>> masterOf = new LinkedHashMap<>();
    for (TreeItem<FormElementViewModel> item : items) {
      if (!(item.getValue().getNode() instanceof Control control) || control.getDependentControls() == null) {
        continue;
      }
      LinkedHashSet<String> targets = new LinkedHashSet<>();
      for (Control.DependentControls.Entry entry : control.getDependentControls().getScreenElement()) {
        if (entry.getIdref() != null) {
          targets.add(entry.getIdref());
        }
      }
      for (String target : targets) {
        TreeItem<FormElementViewModel> targetItem = byId.get(target);
        masterOf.computeIfAbsent(item, key -> new ArrayList<>())
            .add(new Entry(targetItem == null ? UNKNOWN_PATH : path(targetItem), "Control"));
        // controls can't be dependents themselves
        if (targetItem != null && !(targetItem.getValue().getNode() instanceof Control)) {
          dependentOn.computeIfAbsent(targetItem, key -> new ArrayList<>()).add(new Entry(path(item), "Control"));
        }
      }
    }
    for (TreeItem<FormElementViewModel> item : items) {
      List<Entry> dependent = dependentOn.getOrDefault(item, List.of());
      List<Entry> master = masterOf.getOrDefault(item, List.of());
      if (!dependent.isEmpty() || !master.isEmpty()) {
        item.getValue().setDependencies(dependent, master);
      }
    }
  }

  private static void collect(TreeItem<FormElementViewModel> item, List<TreeItem<FormElementViewModel>> out) {
    if (item.getValue() != null) {
      out.add(item);
    }
    for (TreeItem<FormElementViewModel> child : item.getChildren()) {
      collect(child, out);
    }
  }

  static String path(TreeItem<FormElementViewModel> item) {
    List<String> names = new ArrayList<>();
    for (TreeItem<FormElementViewModel> current = item; current != null && current.getValue() != null; current = current.getParent()) {
      names.add(0, current.getValue().getName());
    }
    return String.join("/", names);
  }
}
