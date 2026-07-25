package de.a12.studio.modelsvalidation;

import de.a12.studio.models.A12Model;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs every validator in a model type's list against one model, combining their results. A validator that
 * throws is skipped rather than failing the whole validation, so one bad rule can't hide every other
 * validator's findings (e.g. a ported kernel rule choking on an edge case shouldn't hide the structural
 * checks). Problems with no element id (e.g. the document schema version check) are dropped here, mirroring
 * how the a12 kernel's getElementProblems only ever surfaced element-sourced problems to the UI.
 */
public final class ValidatorRunner {

  private ValidatorRunner() {
  }

  public static List<ModelValidationError> runAll(List<ModelValidator> validators, A12Model<?> model, ValidationContext context) {
    List<ModelValidationError> errors = new ArrayList<>();
    for (ModelValidator validator : validators) {
      try {
        errors.addAll(validator.validate(model, context));
      } catch (Exception e) {
        // A single bad validator shouldn't hide every other validator's findings.
      }
    }
    return errors.stream().filter(error -> error.elementId() != null).toList();
  }
}
