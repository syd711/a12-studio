package de.a12.studio.modelsvalidation.validators.application;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.Case;
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
 * Identifier uniqueness within an application model: module names, flow names per module, scene names
 * per flow, case names per scene, and (sub)region names (SME: "This ... name is not unique.").
 */
public final class ApplicationUniqueNamesValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/modules";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ApplicationModel applicationModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    Set<String> moduleNames = new HashSet<>();
    for (Module module : applicationModel.getContent().getModules()) {
      if (module.getName() != null && !moduleNames.add(module.getName())) {
        errors.add(error(model, "This module name is not unique: \"" + module.getName() + "\"."));
      }
      Set<String> flowNames = new HashSet<>();
      for (Flow flow : module.getFlows()) {
        if (flow.getName() != null && !flowNames.add(flow.getName())) {
          errors.add(error(model, "This flow name is not unique: \"" + flow.getName() + "\"."));
        }
        Set<String> sceneNames = new HashSet<>();
        for (Scene scene : flow.getScenes()) {
          if (scene.getName() != null && !sceneNames.add(scene.getName())) {
            errors.add(error(model, "This scene name is not unique: \"" + scene.getName() + "\"."));
          }
          Set<String> caseNames = new HashSet<>();
          for (Case sceneCase : scene.getCases()) {
            if (sceneCase.getName() != null && !caseNames.add(sceneCase.getName())) {
              errors.add(error(model, "This case name is not unique: \"" + sceneCase.getName() + "\"."));
            }
          }
        }
      }
    }

    if (applicationModel.getContent().getRegion() != null) {
      Set<String> regionNames = new HashSet<>();
      collectRegionNames(model, applicationModel.getContent().getRegion(), regionNames, errors);
    }
    return errors;
  }

  private void collectRegionNames(A12Model<?> model, Region region, Set<String> seen, List<ModelValidationError> errors) {
    if (region.getName() != null && !seen.add(region.getName())) {
      errors.add(error(model, "This region name is not unique: \"" + region.getName() + "\"."));
    }
    for (Region subRegion : region.getSubRegions()) {
      collectRegionNames(model, subRegion, seen, errors);
    }
  }

  private static ModelValidationError error(A12Model<?> model, String message) {
    return new ModelValidationError(model, ELEMENT_ID, message, Severity.ERROR.name());
  }
}
