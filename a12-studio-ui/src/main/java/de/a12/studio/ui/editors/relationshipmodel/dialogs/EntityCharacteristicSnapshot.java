package de.a12.studio.ui.editors.relationshipmodel.dialogs;

import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of an {@link EntityCharacteristic} before {@link
 * EntityCharacteristicDialogController} lets its fields and embedded panels mutate it live, so {@link #restore()}
 * can undo those changes on Cancel. Mirrors {@code
 * de.a12.studio.ui.editors.overviewmodel.dialogs.ColumnSnapshot}.
 */
class EntityCharacteristicSnapshot {

  private final EntityCharacteristic entity;

  private final String json;

  EntityCharacteristicSnapshot(@NonNull EntityCharacteristic entity) {
    this.entity = entity;
    this.json = JsonSettings.objectMapper.writeValueAsString(entity);
  }

  void restore() {
    EntityCharacteristic restored = JsonSettings.objectMapper.readValue(json, EntityCharacteristic.class);
    entity.setRole(restored.getRole());
    entity.setDocumentModel(restored.getDocumentModel());
    entity.setOrdered(restored.getOrdered());
    entity.setLinkConstraints(restored.getLinkConstraints());
    entity.getLabels().clear();
    entity.getLabels().addAll(restored.getLabels());
  }
}
