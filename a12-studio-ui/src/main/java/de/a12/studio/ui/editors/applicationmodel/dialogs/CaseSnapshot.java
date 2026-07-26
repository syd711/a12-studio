package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of a {@link Case} before {@link CaseDialogController} lets its embedded
 * panels ({@link de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController} for the label, {@link
 * de.a12.studio.ui.editors.propertyeditors.SceneChangePanelController} for {@code onEnter}) mutate it live, so
 * {@link #restore()} can undo those changes on Cancel. Unlike {@link MenuSnapshot}'s manual field-by-field
 * copy, this relies on {@link Case} (and everything it nests) being a plain Jackson bean, so a round-trip
 * through {@link JsonSettings#objectMapper} is enough to reconstruct every field, including the polymorphic
 * {@link de.a12.studio.models.applicationmodel.Directive} list inside its {@code sceneChange}. Only needed for
 * an edit of an already-attached case; a newly added one isn't attached to its parent scene's cases list until
 * the dialog resolves with OK, so it needs no undo and is never snapshotted.
 */
class CaseSnapshot {

  private final Case caseObj;

  private final String json;

  CaseSnapshot(@NonNull Case caseObj) {
    this.caseObj = caseObj;
    this.json = JsonSettings.objectMapper.writeValueAsString(caseObj);
  }

  void restore() {
    Case restored = JsonSettings.objectMapper.readValue(json, Case.class);
    caseObj.setName(restored.getName());
    caseObj.getLabel().clear();
    caseObj.getLabel().addAll(restored.getLabel());
    caseObj.setSceneChange(restored.getSceneChange());
  }
}
