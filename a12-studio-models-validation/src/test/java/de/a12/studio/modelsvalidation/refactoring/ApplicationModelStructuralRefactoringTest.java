package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.ApplicationModelContent;
import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.applicationmodel.RegionClearDirective;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.SceneChange;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins gap 6 of "Application Model: gap review" in {@code docs/sme-reference-comparison.md}: renaming or
 * deleting a Region/Scene/Case must auto-rewrite (rename) or clear (delete) every within-model reference to
 * it, the same way SME's own dedicated refactoring feature does (minus the interactive review dialog, which
 * a12-studio has no framework for yet).
 */
class ApplicationModelStructuralRefactoringTest {

  private static ApplicationModel modelWithRegionReferences() {
    ApplicationModel model = new ApplicationModel();
    ApplicationModelContent content = new ApplicationModelContent();
    model.setContent(content);
    content.getDefaultRegion().add("HIDDEN");

    RegionClearDirective regionClear = new RegionClearDirective();
    regionClear.getRegion().add("HIDDEN");
    ViewAddDirective viewAdd = new ViewAddDirective();
    viewAdd.getRegion().add("HIDDEN");

    SceneChange sceneChange = new SceneChange();
    sceneChange.getOnEnter().add(regionClear);
    sceneChange.getOnExit().add(viewAdd);

    Scene scene = new Scene();
    scene.setName("SceneA");
    scene.setSceneChange(sceneChange);

    Flow flow = new Flow();
    flow.setName("Flow1");
    flow.getScenes().add(scene);

    Module module = new Module();
    module.setName("Mod1");
    module.getFlows().add(flow);
    content.getModules().add(module);
    return model;
  }

  @Test
  void renameRegionRewritesDefaultRegionAndEveryDirectiveRegion() {
    ApplicationModel model = modelWithRegionReferences();

    int count = ApplicationModelStructuralRefactoring.renameRegion(model, "HIDDEN", "CONTENT");

    assertEquals(3, count);
    assertEquals(List.of("CONTENT"), model.getContent().getDefaultRegion());
    Scene scene = model.getContent().getModules().get(0).getFlows().get(0).getScenes().get(0);
    assertEquals(List.of("CONTENT"), scene.getSceneChange().getOnEnter().get(0).getRegion());
    assertEquals(List.of("CONTENT"), scene.getSceneChange().getOnExit().get(0).getRegion());
  }

  @Test
  void deleteRegionClearsDefaultRegionAndEveryDirectiveRegion() {
    ApplicationModel model = modelWithRegionReferences();

    int count = ApplicationModelStructuralRefactoring.deleteRegion(model, "HIDDEN");

    assertEquals(3, count);
    assertEquals(List.of(), model.getContent().getDefaultRegion());
    Scene scene = model.getContent().getModules().get(0).getFlows().get(0).getScenes().get(0);
    assertEquals(List.of(), scene.getSceneChange().getOnEnter().get(0).getRegion());
    assertEquals(List.of(), scene.getSceneChange().getOnExit().get(0).getRegion());
  }

  @Test
  void renameRegionLeavesUnrelatedRegionNamesAlone() {
    ApplicationModel model = modelWithRegionReferences();

    int count = ApplicationModelStructuralRefactoring.renameRegion(model, "SIDEBAR", "CONTENT");

    assertEquals(0, count);
    assertEquals(List.of("HIDDEN"), model.getContent().getDefaultRegion());
  }

  private static Flow flowWithScenes(String... sceneNames) {
    Flow flow = new Flow();
    flow.setName("Flow1");
    for (String name : sceneNames) {
      Scene scene = new Scene();
      scene.setName(name);
      flow.getScenes().add(scene);
    }
    return flow;
  }

  @Test
  void renamePriorSceneReferencesUpdatesOnlyScenesInTheSameFlow() {
    Flow flow = flowWithScenes("SceneA", "SceneB", "SceneC");
    flow.getScenes().get(1).setPriorScene("SceneA");
    flow.getScenes().get(2).setPriorScene("SceneA");

    int count = ApplicationModelStructuralRefactoring.renamePriorSceneReferences(flow, "SceneA", "SceneRenamed");

    assertEquals(2, count);
    assertEquals("SceneRenamed", flow.getScenes().get(1).getPriorScene());
    assertEquals("SceneRenamed", flow.getScenes().get(2).getPriorScene());
  }

  @Test
  void clearPriorSceneReferencesUnsetsScenesPointingAtTheDeletedScene() {
    Flow flow = flowWithScenes("SceneA", "SceneB");
    flow.getScenes().get(1).setPriorScene("SceneA");

    int count = ApplicationModelStructuralRefactoring.clearPriorSceneReferences(flow, "SceneA");

    assertEquals(1, count);
    assertNull(flow.getScenes().get(1).getPriorScene());
  }

  @Test
  void renameDefaultCaseReferenceUpdatesTheOwningScene() {
    Scene scene = new Scene();
    scene.setName("SceneA");
    scene.setDefaultCase("Case1");
    scene.getCases().add(new Case());

    int count = ApplicationModelStructuralRefactoring.renameDefaultCaseReference(scene, "Case1", "CaseRenamed");

    assertEquals(1, count);
    assertEquals("CaseRenamed", scene.getDefaultCase());
  }

  @Test
  void clearDefaultCaseReferenceUnsetsTheOwningScenesReferenceToTheDeletedCase() {
    Scene scene = new Scene();
    scene.setName("SceneA");
    scene.setDefaultCase("Case1");

    int count = ApplicationModelStructuralRefactoring.clearDefaultCaseReference(scene, "Case1");

    assertEquals(1, count);
    assertNull(scene.getDefaultCase());
  }
}
