package de.a12.studio.ui.editors.documentmodel.dialogs;

import tools.jackson.core.type.TypeReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Captures a deep, JSON-based clone of a {@link DocumentModel}'s {@code typeDefinitions} section before {@link
 * TypeDefinitionSettingsDialog} lets its embedded table/field editors mutate it live, so {@link #restore()} can
 * undo those changes on Cancel. A JSON round-trip is used (rather than a manual field-by-field copy) because
 * {@link TypeDefinition#getFieldType()} is polymorphic. Restoring replaces the contents of the model's own
 * {@code typeDefinitions} list in place, rather than the list reference itself, since {@link
 * de.a12.studio.ui.editors.typedefinitionmodel.TypeDefinitionTableController} keeps its own reference to that
 * same list.
 */
class TypeDefinitionsSnapshot {

  private final DocumentModel model;

  private final String json;

  TypeDefinitionsSnapshot(@NonNull DocumentModel model) {
    this.model = model;
    this.json = JsonSettings.objectMapper.writeValueAsString(model.getContent().getTypeDefinitions());
  }

  void restore() {
    List<TypeDefinition> restored = JsonSettings.objectMapper.readValue(json, new TypeReference<List<TypeDefinition>>() {});
    List<TypeDefinition> target = model.getContent().getTypeDefinitions();
    target.clear();
    target.addAll(restored);
  }
}
