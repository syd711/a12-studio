package de.a12.studio.modelsvalidation.validators.application;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Menu;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Region;
import de.a12.studio.models.applicationmodel.Scene;
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
 * Identifier uniqueness within an application model: module names, flow names per module, scene names
 * per flow, case names per scene, and (sub)region names (SME: "This ... name is not unique.").
 *
 * <p>Each category gets its own element id (region/module names are checked model-wide, so those two are
 * fixed constants; flow and scene names are only checked within one module, and case names only within one
 * scene, so those are built from the owning module's/scene's current name) so the panel that owns that
 * category — {@code RegionPanelController}, {@code ModulesPanelController}, {@code FlowsPanelController}
 * (both flows and scenes, since it shows a whole module's flow/scene tree at once) and {@code
 * CasesPanelController} — can look up just its own errors via {@link
 * de.a12.studio.modelsvalidation.ValidationService#validateElement} instead of receiving every category's
 * errors at once.
 */
public final class ApplicationUniqueNamesValidator implements ModelValidator {

  public static final String REGION_ELEMENT_ID = "content/region";

  public static final String MODULES_ELEMENT_ID = "content/modules";

  public static String flowsElementId(String moduleName) {
    return "content/modules/" + moduleName + "/flows";
  }

  public static String scenesElementId(String moduleName) {
    return "content/modules/" + moduleName + "/flows/scenes";
  }

  public static String casesElementId(String sceneName) {
    return "content/scenes/" + sceneName + "/cases";
  }

  public static String childMenuElementId(String parentMenuName) {
    return "content/modules/menu/" + parentMenuName + "/children";
  }

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ApplicationModel applicationModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    Set<String> moduleNames = new HashSet<>();
    for (Module module : applicationModel.getContent().getModules()) {
      if (module.getName() != null && !moduleNames.add(module.getName())) {
        errors.add(error(model, MODULES_ELEMENT_ID, ValidationMessages.get("validation.applicationUniqueNames.module", module.getName())));
      }
      Set<String> flowNames = new HashSet<>();
      for (Flow flow : module.getFlows()) {
        if (flow.getName() != null && !flowNames.add(flow.getName())) {
          errors.add(error(model, flowsElementId(module.getName()),
              ValidationMessages.get("validation.applicationUniqueNames.flow", flow.getName())));
        }
        Set<String> sceneNames = new HashSet<>();
        for (Scene scene : flow.getScenes()) {
          if (scene.getName() != null && !sceneNames.add(scene.getName())) {
            errors.add(error(model, scenesElementId(module.getName()),
                ValidationMessages.get("validation.applicationUniqueNames.scene", scene.getName())));
          }
          Set<String> caseNames = new HashSet<>();
          for (Case sceneCase : scene.getCases()) {
            if (sceneCase.getName() != null && !caseNames.add(sceneCase.getName())) {
              errors.add(error(model, casesElementId(scene.getName()),
                  ValidationMessages.get("validation.applicationUniqueNames.case", sceneCase.getName())));
            }
          }
        }
      }
    }

    if (applicationModel.getContent().getRegion() != null) {
      Set<String> regionNames = new HashSet<>();
      collectRegionNames(model, applicationModel.getContent().getRegion(), regionNames, errors);
    }

    for (Module module : applicationModel.getContent().getModules()) {
      if (module.getMenu() != null) {
        checkChildMenuNames(model, module.getMenu(), errors);
      }
    }
    return errors;
  }

  private void checkChildMenuNames(A12Model<?> model, Menu menu, List<ModelValidationError> errors) {
    Set<String> childNames = new HashSet<>();
    for (Menu child : menu.getChildren()) {
      if (child.getName() != null && !childNames.add(child.getName())) {
        errors.add(error(model, childMenuElementId(menu.getName()),
            ValidationMessages.get("validation.applicationUniqueNames.childMenu", child.getName())));
      }
    }
    for (Menu child : menu.getChildren()) {
      checkChildMenuNames(model, child, errors);
    }
  }

  private void collectRegionNames(A12Model<?> model, Region region, Set<String> seen, List<ModelValidationError> errors) {
    if (region.getName() != null && !seen.add(region.getName())) {
      errors.add(error(model, REGION_ELEMENT_ID, ValidationMessages.get("validation.applicationUniqueNames.region", region.getName())));
    }
    for (Region subRegion : region.getSubRegions()) {
      collectRegionNames(model, subRegion, seen, errors);
    }
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String message) {
    return new ModelValidationError(model, elementId, message, Severity.ERROR.name());
  }
}
