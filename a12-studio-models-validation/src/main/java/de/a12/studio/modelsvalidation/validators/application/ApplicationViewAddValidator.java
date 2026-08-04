package de.a12.studio.modelsvalidation.validators.application;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Directive;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.ModelDescriptor;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.applicationmodel.SceneChange;
import de.a12.studio.models.applicationmodel.ViewAddDirective;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * VIEW_ADD directives need a UI component name (SME: "$name$ is required for VIEW_ADD directives."),
 * and every model referenced from a view descriptor must exist in the workspace.
 */
public final class ApplicationViewAddValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/modules/flows/scenes/sceneChange";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ApplicationModel applicationModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Module module : applicationModel.getContent().getModules()) {
      for (Flow flow : module.getFlows()) {
        for (Scene scene : flow.getScenes()) {
          checkSceneChange(model, scene.getSceneChange(), context, errors);
          for (Case sceneCase : scene.getCases()) {
            checkSceneChange(model, sceneCase.getSceneChange(), context, errors);
          }
        }
      }
    }
    return errors;
  }

  private void checkSceneChange(A12Model<?> model, SceneChange sceneChange, ValidationContext context,
                                List<ModelValidationError> errors) {
    if (sceneChange == null) {
      return;
    }
    checkDirectives(model, sceneChange.getOnEnter(), context, errors);
    checkDirectives(model, sceneChange.getOnExit(), context, errors);
  }

  private void checkDirectives(A12Model<?> model, List<Directive> directives, ValidationContext context,
                               List<ModelValidationError> errors) {
    if (directives == null) {
      return;
    }
    for (Directive directive : directives) {
      if (!(directive instanceof ViewAddDirective viewAdd)) {
        continue;
      }
      if (viewAdd.getName() == null || viewAdd.getName().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.a_name_is_required_for_view_add_directives", Severity.ERROR.name()));
      }
      for (ModelDescriptor descriptor : viewAdd.getModels()) {
        if (descriptor.getName() != null && !descriptor.getName().isBlank()
            && context.findOtherModel(descriptor.getName()) == null) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "validation.the_model"" + descriptor.getName() + "\" referenced by a VIEW_ADD directive does not exist in the workspace.",
              Severity.ERROR.name()));
        }
      }
    }
  }
}
