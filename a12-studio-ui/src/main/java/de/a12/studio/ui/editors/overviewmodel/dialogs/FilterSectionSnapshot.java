package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.FilterSection;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of a {@link FilterSection} before {@link SectionDataDialogController} lets
 * its embedded panels (Label, Fields) mutate it live, so {@link #restore()} can undo those changes on Cancel.
 * Mirrors {@link FilterGroupSnapshot}.
 */
class FilterSectionSnapshot {

  private final FilterSection section;

  private final String json;

  FilterSectionSnapshot(@NonNull FilterSection section) {
    this.section = section;
    this.json = JsonSettings.objectMapper.writeValueAsString(section);
  }

  void restore() {
    FilterSection restored = JsonSettings.objectMapper.readValue(json, FilterSection.class);
    section.setId(restored.getId());
    section.getLabel().clear();
    section.getLabel().addAll(restored.getLabel());
    section.getFields().clear();
    section.getFields().addAll(restored.getFields());
  }
}
