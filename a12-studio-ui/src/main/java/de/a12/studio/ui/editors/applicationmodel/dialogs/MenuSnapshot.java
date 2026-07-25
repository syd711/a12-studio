package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.Label;
import de.a12.studio.models.applicationmodel.InitialActivity;
import de.a12.studio.models.applicationmodel.Menu;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures the subset of a {@link Menu} that {@link ChildMenuDialogController}'s embedded property editors can
 * change (name, label, initial activity, permission), so {@link #restore()} can undo whatever they already
 * applied to the live menu object while the dialog was open, on Cancel. Deliberately leaves {@link
 * Menu#getChildren()} untouched, since this dialog never edits a child menu's own children. Only needed for an
 * edit of an already-attached menu; a newly added one isn't attached to its parent's children list until the
 * dialog resolves with OK, so it needs no undo and is never snapshotted.
 */
class MenuSnapshot {

  private final Menu menu;

  private final String name;
  private final List<Label> label = new ArrayList<>();
  private final String permission;
  private final boolean hadInitialActivity;
  private final Map<String, String> descriptor;
  private final Boolean withoutData;

  MenuSnapshot(@NonNull Menu menu) {
    this.menu = menu;
    this.name = menu.getName();
    copyLabels(menu.getLabel(), label);
    this.permission = menu.getPermission();

    InitialActivity initialActivity = menu.getInitialActivity();
    this.hadInitialActivity = initialActivity != null;
    this.descriptor = initialActivity != null ? new LinkedHashMap<>(initialActivity.getDescriptor()) : null;
    this.withoutData = initialActivity != null ? initialActivity.getWithoutData() : null;
  }

  /**
   * Restores every captured field onto the menu in place, preserving the identity of its label list (rather
   * than replacing it) so anything already holding a reference to it keeps working.
   */
  void restore() {
    menu.setName(name);
    replaceContents(menu.getLabel(), label);
    menu.setPermission(permission);

    if (!hadInitialActivity) {
      menu.setInitialActivity(null);
      return;
    }
    InitialActivity initialActivity = menu.getInitialActivity();
    if (initialActivity == null) {
      initialActivity = new InitialActivity();
      menu.setInitialActivity(initialActivity);
    }
    initialActivity.getDescriptor().clear();
    initialActivity.getDescriptor().putAll(descriptor);
    initialActivity.setWithoutData(withoutData);
  }

  private static void copyLabels(List<Label> source, List<Label> target) {
    for (Label label : source) {
      Label copy = new Label();
      copy.setLocale(label.getLocale());
      copy.setText(label.getText());
      target.add(copy);
    }
  }

  private static <T> void replaceContents(List<T> target, List<T> snapshot) {
    target.clear();
    target.addAll(snapshot);
  }
}
