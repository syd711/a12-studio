package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.FilterGroup;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of a {@link FilterGroup} before {@link FilterGroupDialogController} lets its
 * fields and embedded panels mutate it live, so {@link #restore()} can undo those changes on Cancel. Mirrors
 * {@link ColumnSnapshot}.
 */
class FilterGroupSnapshot {

  private final FilterGroup group;

  private final String json;

  FilterGroupSnapshot(@NonNull FilterGroup group) {
    this.group = group;
    this.json = JsonSettings.objectMapper.writeValueAsString(group);
  }

  void restore() {
    FilterGroup restored = JsonSettings.objectMapper.readValue(json, FilterGroup.class);
    group.setId(restored.getId());
    group.setName(restored.getName());
    group.getLabel().clear();
    group.getLabel().addAll(restored.getLabel());
    group.setIcon(restored.getIcon());
    group.setCollapsed(restored.getCollapsed());
    group.getFilterItems().clear();
    group.getFilterItems().addAll(restored.getFilterItems());
  }
}
