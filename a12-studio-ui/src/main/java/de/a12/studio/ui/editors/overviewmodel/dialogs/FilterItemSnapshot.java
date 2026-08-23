package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.FilterItem;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of a {@link FilterItem} before {@link FilterItemDialogController} lets its
 * fields and embedded panels mutate it live, so {@link #restore()} can undo those changes on Cancel. Mirrors
 * {@link ColumnSnapshot}.
 */
class FilterItemSnapshot {

  private final FilterItem item;

  private final String json;

  FilterItemSnapshot(@NonNull FilterItem item) {
    this.item = item;
    this.json = JsonSettings.objectMapper.writeValueAsString(item);
  }

  void restore() {
    FilterItem restored = JsonSettings.objectMapper.readValue(json, FilterItem.class);
    item.setId(restored.getId());
    item.setType(restored.getType());
    item.setOptions(restored.getOptions());
    item.setShowInFilterBar(restored.getShowInFilterBar());
    item.setCollapsed(restored.getCollapsed());
    item.getLabel().clear();
    item.getLabel().addAll(restored.getLabel());
    item.setIcon(restored.getIcon());
  }
}
