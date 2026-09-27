package de.a12.studio.modelsvalidation.refactoring;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.SceneChange;

import java.util.List;
import java.util.function.Consumer;

/**
 * Rewrites (rename) or clears (delete) the within-model references a Region/Scene/Case rename or delete would
 * otherwise leave dangling, mirroring SME's dedicated "Within the Model" refactoring
 * ({@code docs/modules/appModel/05_refactoring_app_model.adoc}) - minus its interactive per-reference
 * Commit/Edit/Ignore review dialog, which a12-studio has no framework for yet (for any model type). Callers
 * apply these the same way the rest of this codebase's cross-model rename machinery
 * ({@link ProjectReferenceRefactoring}, {@link de.a12.studio.models.util.ModelReferenceRewriter}, {@link
 * RoleRenameRefactoring}'s own caller) already does: silently, with no interactive review.
 *
 * <ul>
 *   <li><b>Region</b>: every {@link Directive#getRegion()} entry and {@code content.defaultRegion} entry
 *       across the whole model (a Region is referenced by plain name from anywhere in the model, not scoped
 *       to a Flow/Scene).</li>
 *   <li><b>Scene</b>: every other {@link Scene#getPriorScene()} within the <em>same</em> {@link Flow} - Prior
 *       Scene is scoped to the owning Flow both in SME's own reference provider and in a12-studio's {@code
 *       SceneDialogController#priorSceneOptions}/{@code ApplicationSceneGraphValidator#checkPriorScene}.</li>
 *   <li><b>Case</b>: the owning {@link Scene#getDefaultCase()} only - a Case has no other referrer.</li>
 * </ul>
 */
public final class ApplicationModelStructuralRefactoring {

  private ApplicationModelStructuralRefactoring() {
  }

  /** Renames every reference to region {@code oldName} to {@code newName}; returns how many were rewritten. */
  public static int renameRegion(ApplicationModel model, String oldName, String newName) {
    if (oldName == null || newName == null || oldName.equals(newName)) {
      return 0;
    }
    int[] count = {0};
    withRegionLists(model, list -> {
      for (int i = 0; i < list.size(); i++) {
        if (oldName.equals(list.get(i))) {
          list.set(i, newName);
          count[0]++;
        }
      }
    });
    return count[0];
  }

  /** Clears every reference to region {@code name}; returns how many were removed. */
  public static int deleteRegion(ApplicationModel model, String name) {
    if (name == null) {
      return 0;
    }
    int[] count = {0};
    withRegionLists(model, list -> count[0] += removeAll(list, name));
    return count[0];
  }

  private static void withRegionLists(ApplicationModel model, Consumer<List<String>> action) {
    if (model == null || model.getContent() == null) {
      return;
    }
    if (model.getContent().getDefaultRegion() != null) {
      action.accept(model.getContent().getDefaultRegion());
    }
    forEachDirective(model, directive -> {
      if (directive.getRegion() != null) {
        action.accept(directive.getRegion());
      }
    });
  }

  private static void forEachDirective(ApplicationModel model, Consumer<Directive> action) {
    for (Module module : model.getContent().getModules()) {
      for (Flow flow : module.getFlows()) {
        for (Scene scene : flow.getScenes()) {
          forEachDirective(scene.getSceneChange(), action);
          for (Case sceneCase : scene.getCases()) {
            forEachDirective(sceneCase.getSceneChange(), action);
          }
        }
      }
    }
  }

  private static void forEachDirective(SceneChange sceneChange, Consumer<Directive> action) {
    if (sceneChange == null) {
      return;
    }
    if (sceneChange.getOnEnter() != null) {
      sceneChange.getOnEnter().forEach(action);
    }
    if (sceneChange.getOnExit() != null) {
      sceneChange.getOnExit().forEach(action);
    }
  }

  /** Renames every other Scene's Prior Scene within {@code owningFlow} pointing at {@code oldName}. */
  public static int renamePriorSceneReferences(Flow owningFlow, String oldName, String newName) {
    if (owningFlow == null || oldName == null || newName == null || oldName.equals(newName)) {
      return 0;
    }
    int count = 0;
    for (Scene scene : owningFlow.getScenes()) {
      if (oldName.equals(scene.getPriorScene())) {
        scene.setPriorScene(newName);
        count++;
      }
    }
    return count;
  }

  /** Clears every Prior Scene within {@code owningFlow} pointing at {@code name}. */
  public static int clearPriorSceneReferences(Flow owningFlow, String name) {
    if (owningFlow == null || name == null) {
      return 0;
    }
    int count = 0;
    for (Scene scene : owningFlow.getScenes()) {
      if (name.equals(scene.getPriorScene())) {
        scene.setPriorScene(null);
        count++;
      }
    }
    return count;
  }

  /** Renames {@code owningScene}'s Default Case reference if it points at {@code oldName}. */
  public static int renameDefaultCaseReference(Scene owningScene, String oldName, String newName) {
    if (owningScene == null || oldName == null || newName == null || oldName.equals(newName)) {
      return 0;
    }
    if (oldName.equals(owningScene.getDefaultCase())) {
      owningScene.setDefaultCase(newName);
      return 1;
    }
    return 0;
  }

  /** Clears {@code owningScene}'s Default Case reference if it points at {@code name}. */
  public static int clearDefaultCaseReference(Scene owningScene, String name) {
    if (owningScene == null || name == null) {
      return 0;
    }
    if (name.equals(owningScene.getDefaultCase())) {
      owningScene.setDefaultCase(null);
      return 1;
    }
    return 0;
  }

  private static int removeAll(List<String> list, String value) {
    int removed = 0;
    for (int i = list.size() - 1; i >= 0; i--) {
      if (value.equals(list.get(i))) {
        list.remove(i);
        removed++;
      }
    }
    return removed;
  }
}
