package de.a12.studio.modelsvalidation.validators.application;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Scene graph consistency: a scene cannot be its own prior scene, a prior scene must exist within the
 * same flow, and the content's default region must be a known (sub)region (SME: "The scene cannot be
 * its own prior scene." / "This prior scene is not known within the flow." / "This default region is
 * unknown.").
 */
public final class ApplicationSceneGraphValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/modules/flows/scenes";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ApplicationModel applicationModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    for (Module module : applicationModel.getContent().getModules()) {
      for (Flow flow : module.getFlows()) {
        Set<String> sceneNames = new HashSet<>();
        for (Scene scene : flow.getScenes()) {
          if (scene.getName() != null) {
            sceneNames.add(scene.getName());
          }
        }
        for (Scene scene : flow.getScenes()) {
          String priorScene = scene.getPriorScene();
          if (priorScene == null || priorScene.isBlank()) {
            continue;
          }
          if (priorScene.equals(scene.getName())) {
            errors.add(new ModelValidationError(model, ELEMENT_ID,
                "validation.the_scene"" + scene.getName() + "\" cannot be its own prior scene.", Severity.ERROR.name()));
          }
          else if (!sceneNames.contains(priorScene)) {
            errors.add(new ModelValidationError(model, ELEMENT_ID,
                "validation.the_prior_scene"" + priorScene + "\" of scene \"" + scene.getName()
                    + "\" is not known within the flow.", Severity.ERROR.name()));
          }
        }
      }
    }

    List<String> defaultRegion = applicationModel.getContent().getDefaultRegion();
    if (defaultRegion != null && !defaultRegion.isEmpty() && applicationModel.getContent().getRegion() != null) {
      Set<String> regionNames = new HashSet<>();
      collectRegionNames(applicationModel.getContent().getRegion(), regionNames);
      for (String name : defaultRegion) {
        if (!regionNames.contains(name)) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "validation.this_default_region_is_unknown"" + name + "\".", Severity.ERROR.name()));
        }
      }
    }
    return errors;
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
