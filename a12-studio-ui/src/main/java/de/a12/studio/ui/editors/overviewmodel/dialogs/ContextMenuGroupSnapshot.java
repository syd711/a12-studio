package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.ActionGroup;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of an {@link ActionGroup} before {@link ContextMenuGroupDialogController}
 * lets its fields mutate it live, so {@link #restore()} can undo those changes on Cancel. Mirrors {@link
 * FilterGroupSnapshot}.
 */
class ContextMenuGroupSnapshot {

  private final ActionGroup group;

  private final String json;

  ContextMenuGroupSnapshot(@NonNull ActionGroup group) {
    this.group = group;
    this.json = JsonSettings.objectMapper.writeValueAsString(group);
  }

  void restore() {
    ActionGroup restored = JsonSettings.objectMapper.readValue(json, ActionGroup.class);
    group.setName(restored.getName());
    group.getTitle().clear();
    group.getTitle().addAll(restored.getTitle());
    group.getActions().clear();
    group.getActions().addAll(restored.getActions());
  }
}
