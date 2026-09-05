package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of a {@link Column} before {@link OverviewColumnDialogController} lets its
 * fields and embedded panels ({@link de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController} for
 * the label, {@link de.a12.studio.ui.editors.overviewmodel.StylesPanelController} for the header/content cell
 * styles) mutate it live, so {@link #restore()} can undo those changes on Cancel. See {@code CaseSnapshot} for
 * the same pattern.
 */
class ColumnSnapshot {

  private final Column column;

  private final String json;

  ColumnSnapshot(@NonNull Column column) {
    this.column = column;
    this.json = JsonSettings.objectMapper.writeValueAsString(column);
  }

  void restore() {
    Column restored = JsonSettings.objectMapper.readValue(json, Column.class);
    column.setId(restored.getId());
    column.getLabel().clear();
    column.getLabel().addAll(restored.getLabel());
    column.setWidth(restored.getWidth());
    column.setFixedWidth(restored.getFixedWidth());
    column.setAlignment(restored.getAlignment());
    column.setPinDirection(restored.getPinDirection());
    column.setStyles(restored.getStyles());
    column.setIcon(restored.getIcon());
    column.setLabelHidden(restored.getLabelHidden());
    column.setElementRef(restored.getElementRef());
    column.setSortable(restored.getSortable());
    column.setPreferredSorting(restored.getPreferredSorting());
    column.setAttachmentDisplayMode(restored.getAttachmentDisplayMode());
    column.setMultiSelectDisplayMode(restored.getMultiSelectDisplayMode());
    column.getSuffix().clear();
    column.getSuffix().addAll(restored.getSuffix());
    column.setSuffixRef(restored.getSuffixRef());
    column.setUseDynamicSuffix(restored.getUseDynamicSuffix());
    column.getSummary().clear();
    column.getSummary().addAll(restored.getSummary());
    column.setName(restored.getName());
    column.setExpression(restored.getExpression());
  }
}
