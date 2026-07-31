package de.a12.studio.ui.editors.documentmodel.dialogs;

import tools.jackson.core.type.TypeReference;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.TypeDefinition;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.util.List;

/**
 * Captures a deep, JSON-based clone of a {@link DocumentModel}'s {@code typeDefinitions} section and its
 * header {@code modelReferences} (the latter needed since Import/Delete Import - see {@link
 * de.a12.studio.ui.editors.typedefinitionmodel.TypeDefinitionTableController#onImport} and {@code
 * #removeImport} - mutate the header's {@link ModelReference} list, not {@code typeDefinitions} itself)
 * before {@link TypeDefinitionSettingsDialog} lets its embedded table/field editors mutate either live, so
 * {@link #restore()} can undo those changes on Cancel. A JSON round-trip is used (rather than a manual
 * field-by-field copy) because {@link TypeDefinition#getFieldType()} is polymorphic. Restoring replaces the
 * contents of the model's own lists in place, rather than the list references themselves, since {@link
 * de.a12.studio.ui.editors.typedefinitionmodel.TypeDefinitionTableController} keeps its own reference to
 * those same lists.
 */
class TypeDefinitionsSnapshot {

  private final DocumentModel model;

  private final String typeDefinitionsJson;

  private final String modelReferencesJson;

  TypeDefinitionsSnapshot(@NonNull DocumentModel model) {
    this.model = model;
    this.typeDefinitionsJson = JsonSettings.objectMapper.writeValueAsString(model.getContent().getTypeDefinitions());
    this.modelReferencesJson = JsonSettings.objectMapper.writeValueAsString(model.getModelReferences());
  }

  void restore() {
    List<TypeDefinition> restoredTypeDefinitions =
        JsonSettings.objectMapper.readValue(typeDefinitionsJson, new TypeReference<List<TypeDefinition>>() {});
    List<TypeDefinition> typeDefinitions = model.getContent().getTypeDefinitions();
    typeDefinitions.clear();
    typeDefinitions.addAll(restoredTypeDefinitions);

    List<ModelReference> restoredModelReferences =
        JsonSettings.objectMapper.readValue(modelReferencesJson, new TypeReference<List<ModelReference>>() {});
    List<ModelReference> modelReferences = model.getModelReferences();
    modelReferences.clear();
    modelReferences.addAll(restoredModelReferences);
  }
}
