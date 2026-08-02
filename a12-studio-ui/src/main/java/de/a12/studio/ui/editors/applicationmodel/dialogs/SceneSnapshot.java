package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

/**
 * Captures a deep, JSON-based clone of a {@link Scene} before {@link SceneDialogController} lets its embedded
 * panels ({@link de.a12.studio.ui.editors.applicationmodel.MatchConditionsPanelController}, {@link
 * de.a12.studio.ui.editors.applicationmodel.SceneChangePanelController}, {@link
 * de.a12.studio.ui.editors.applicationmodel.CasesPanelController}) mutate it live, so {@link #restore()} can
 * undo those changes on Cancel. See {@link CaseSnapshot} for why a JSON round-trip is used instead of a manual
 * field-by-field copy like {@link MenuSnapshot}. Only needed for an edit of an already-attached scene; a newly
 * added one isn't attached to its parent flow's scenes list until the dialog resolves with OK, so it needs no
 * undo and is never snapshotted.
 */
class SceneSnapshot {

  private final Scene scene;

  private final String json;

  SceneSnapshot(@NonNull Scene scene) {
    this.scene = scene;
    this.json = JsonSettings.objectMapper.writeValueAsString(scene);
  }

  void restore() {
    Scene restored = JsonSettings.objectMapper.readValue(json, Scene.class);
    scene.setName(restored.getName());
    scene.setDescription(restored.getDescription());
    scene.setPriorScene(restored.getPriorScene());
    scene.setDefaultCase(restored.getDefaultCase());
    scene.getMatchConditions().clear();
    scene.getMatchConditions().addAll(restored.getMatchConditions());
    scene.setSceneChange(restored.getSceneChange());
    scene.getCases().clear();
    scene.getCases().addAll(restored.getCases());
  }
}
