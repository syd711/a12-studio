package de.a12.studio.modelsvalidation.validators.application;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.MatchCondition;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.SceneChange;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scene graph consistency: a scene cannot be its own prior scene, a prior scene must exist within the same
 * flow, a scene's default case must be one of its own cases, every directive's region and the content's
 * default region must be a known (sub)region, and match conditions must be well-formed (SME: "The scene
 * cannot be its own prior scene." / "This prior scene is not known within the flow." / "Invalid Reference"
 * (defaultCase/directive region) / "This default region is unknown." / "Must set exactly one of mustEqual
 * and isSet, but cannot set both." / "At least one match condition must be provided for this scene.").
 */
public final class ApplicationSceneGraphValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/modules/flows/scenes";

  public static final String DEFAULT_REGION_ELEMENT_ID = "content/defaultRegion";

  public static final String DIRECTIVE_REGION_ELEMENT_ID = "content/modules/flows/scenes/sceneChange/region";

  public static final String DEFAULT_CASE_ELEMENT_ID = "content/modules/flows/scenes/defaultCase";

  public static final String MATCH_CONDITIONS_ELEMENT_ID = "content/modules/flows/scenes/matchConditions";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ApplicationModel applicationModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    Set<String> regionNames = new HashSet<>();
    if (applicationModel.getContent().getRegion() != null) {
      collectRegionNames(applicationModel.getContent().getRegion(), regionNames);
    }

    for (Module module : applicationModel.getContent().getModules()) {
      for (Flow flow : module.getFlows()) {
        Set<String> sceneNames = new HashSet<>();
        for (Scene scene : flow.getScenes()) {
          if (scene.getName() != null) {
            sceneNames.add(scene.getName());
          }
        }
        for (Scene scene : flow.getScenes()) {
          checkPriorScene(model, scene, sceneNames, errors);
          checkDefaultCase(model, scene, errors);
          checkMatchConditions(model, scene, errors);
          checkSceneChangeRegions(model, scene.getSceneChange(), regionNames, errors);
          for (Case sceneCase : scene.getCases()) {
            checkSceneChangeRegions(model, sceneCase.getSceneChange(), regionNames, errors);
          }
        }
      }
    }

    checkDefaultRegion(model, applicationModel.getContent().getDefaultRegion(), regionNames, errors);
    return errors;
  }

  private void checkPriorScene(A12Model<?> model, Scene scene, Set<String> sceneNames, List<ModelValidationError> errors) {
    String priorScene = scene.getPriorScene();
    if (priorScene == null || priorScene.isBlank()) {
      return;
    }
    if (priorScene.equals(scene.getName())) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.applicationSceneGraph.sceneIsOwnPrior", scene.getName()), Severity.ERROR.name()));
    }
    else if (!sceneNames.contains(priorScene)) {
      errors.add(new ModelValidationError(model, ELEMENT_ID,
          ValidationMessages.get("validation.applicationSceneGraph.unknownPriorScene", priorScene, scene.getName()),
          Severity.ERROR.name()));
    }
  }

  private void checkDefaultCase(A12Model<?> model, Scene scene, List<ModelValidationError> errors) {
    String defaultCase = scene.getDefaultCase();
    if (defaultCase == null || defaultCase.isBlank()) {
      return;
    }
    boolean known = scene.getCases().stream().anyMatch(sceneCase -> defaultCase.equals(sceneCase.getName()));
    if (!known) {
      errors.add(new ModelValidationError(model, DEFAULT_CASE_ELEMENT_ID,
          ValidationMessages.get("validation.applicationSceneGraph.unknownDefaultCase", defaultCase, scene.getName()),
          Severity.ERROR.name()));
    }
  }

  private void checkMatchConditions(A12Model<?> model, Scene scene, List<ModelValidationError> errors) {
    List<MatchCondition> matchConditions = scene.getMatchConditions();
    if (matchConditions.isEmpty()) {
      errors.add(new ModelValidationError(model, MATCH_CONDITIONS_ELEMENT_ID,
          ValidationMessages.get("validation.applicationSceneGraph.matchConditionsRequired", scene.getName()), Severity.ERROR.name()));
      return;
    }
    for (MatchCondition matchCondition : matchConditions) {
      if (matchCondition.getKey() == null || matchCondition.getKey().isBlank()) {
        continue;
      }
      boolean hasMustEqual = matchCondition.getMustEqual() != null;
      boolean hasIsSet = matchCondition.getIsSet() != null;
      if (hasMustEqual == hasIsSet) {
        errors.add(new ModelValidationError(model, MATCH_CONDITIONS_ELEMENT_ID,
            ValidationMessages.get("validation.applicationSceneGraph.matchConditionExactlyOne", matchCondition.getKey()),
            Severity.ERROR.name()));
      }
    }
  }

  private void checkSceneChangeRegions(A12Model<?> model, SceneChange sceneChange, Set<String> regionNames, List<ModelValidationError> errors) {
    if (sceneChange == null) {
      return;
    }
    checkDirectiveRegions(model, sceneChange.getOnEnter(), regionNames, errors);
    checkDirectiveRegions(model, sceneChange.getOnExit(), regionNames, errors);
  }

  private void checkDirectiveRegions(A12Model<?> model, List<Directive> directives, Set<String> regionNames, List<ModelValidationError> errors) {
    for (Directive directive : directives) {
      for (String region : directive.getRegion()) {
        if (!regionNames.contains(region)) {
          errors.add(new ModelValidationError(model, DIRECTIVE_REGION_ELEMENT_ID,
              ValidationMessages.get("validation.applicationSceneGraph.unknownDirectiveRegion", region), Severity.ERROR.name()));
        }
      }
    }
  }

  private void checkDefaultRegion(A12Model<?> model, List<String> defaultRegion, Set<String> regionNames, List<ModelValidationError> errors) {
    if (defaultRegion == null || defaultRegion.isEmpty()) {
      return;
    }
    for (String name : defaultRegion) {
      if (!regionNames.contains(name)) {
        errors.add(new ModelValidationError(model, DEFAULT_REGION_ELEMENT_ID,
            ValidationMessages.get("validation.applicationSceneGraph.unknownDefaultRegion", name), Severity.ERROR.name()));
      }
    }
  }

  private void collectRegionNames(Region region, Set<String> names) {
    if (region.getName() != null) {
      names.add(region.getName());
    }
    for (Region subRegion : region.getSubRegions()) {
      collectRegionNames(subRegion, names);
    }
  }
}
