package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.printmodel.GenericPrintElement;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintElementReference;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.printmodel.PrintSegmentDefinition;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Headline levels must be used in order — Headline 2 only after Headline 1, and so on (print modeling
 * docs, mandatory for accessible PDFs). WARNING severity; headline elements have no typed DTO, so the
 * level is read defensively from the generic element's extras.
 */
public final class PrintHeadlineOrderValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/elementDefinitions/headline";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof PrintModel printModel) || printModel.getContent().getSegments() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    int lastLevel = 0;
    for (PrintSegmentDefinition segment : printModel.getContent().getSegments().getDefinitions()) {
      for (PrintElementReference reference : segment.getElementReferences()) {
        Integer level = headlineLevel(printModel, reference.getRefId());
        if (level == null) {
          continue;
        }
        if (level > lastLevel + 1) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "validation.headline_level" + level + " is used before level " + (lastLevel + 1)
                  + ". Headline levels must be used in order.", Severity.WARNING.name()));
        }
        lastLevel = level;
      }
    }
    return errors;
  }

  private static Integer headlineLevel(PrintModel model, String refId) {
    if (refId == null) {
      return null;
    }
    for (PrintElementDefinition definition : model.getContent().getElementDefinitions()) {
      if (refId.equals(definition.getId()) && definition instanceof GenericPrintElement generic
          && "Headline".equalsIgnoreCase(generic.getType())
          && generic.getExtras().get("level") instanceof Number level) {
        return level.intValue();
      }
    }
    return null;
  }
}
