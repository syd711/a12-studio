package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of a context-menu action ({@link Button}) before {@link
 * ContextMenuActionDialogController} lets its fields and embedded panels mutate it live, so {@link #restore()}
 * can undo those changes on Cancel. Mirrors {@link ColumnSnapshot}/{@link FilterItemSnapshot}. Deliberately
 * doesn't restore {@code primary}/{@code destructive}/{@code labelHidden} - this dialog never edits them (see
 * {@link ContextMenuActionDialogController}'s class doc), so they're left untouched either way.
 */
class ContextMenuActionSnapshot {

  private final Button action;

  private final String json;

  ContextMenuActionSnapshot(@NonNull Button action) {
    this.action = action;
    this.json = JsonSettings.objectMapper.writeValueAsString(action);
  }

  void restore() {
    Button restored = JsonSettings.objectMapper.readValue(json, Button.class);
    action.setEvent(restored.getEvent());
    action.setConfirmation(restored.getConfirmation());
    action.setIcon(restored.getIcon());
    action.getLabel().clear();
    action.getLabel().addAll(restored.getLabel());
    action.getDescription().clear();
    action.getDescription().addAll(restored.getDescription());
    action.setStyles(restored.getStyles());
    action.setAnnotations(restored.getAnnotations());
  }
}
