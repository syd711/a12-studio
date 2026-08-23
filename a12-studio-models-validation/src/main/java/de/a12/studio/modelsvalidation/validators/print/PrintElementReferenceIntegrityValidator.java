package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.printmodel.EntityRef;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintElementReference;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.printmodel.PrintSegmentDefinition;
import de.a12.studio.models.printmodel.PrintStructureEntry;
import de.a12.studio.models.printmodel.PrintTextElement;
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
 * Internal reference integrity of a print model: every segment element reference and every text entity
 * must point to an existing element definition, and every structure entry must point to an existing
 * segment (part of the print engine's "Internal Validation").
 */
public final class PrintElementReferenceIntegrityValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/segments";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof PrintModel printModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();

    Set<String> definitionIds = new HashSet<>();
    for (PrintElementDefinition definition : printModel.getContent().getElementDefinitions()) {
      if (definition.getId() != null) {
        definitionIds.add(definition.getId());
      }
    }
    Set<String> segmentIds = new HashSet<>();
    List<PrintSegmentDefinition> segments = printModel.getContent().getSegments() != null
        ? printModel.getContent().getSegments().getDefinitions()
        : List.of();
    for (PrintSegmentDefinition segment : segments) {
      if (segment.getId() != null) {
        segmentIds.add(segment.getId());
      }
    }

    for (PrintSegmentDefinition segment : segments) {
      for (PrintElementReference reference : segment.getElementReferences()) {
        if (reference.getRefId() == null || !definitionIds.contains(reference.getRefId())) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              ValidationMessages.get("validation.printElementReferenceIntegrity.missingElementDefinition",
                  segment.getTitle(), reference.getRefId()), Severity.ERROR.name()));
        }
      }
    }

    for (PrintElementDefinition definition : printModel.getContent().getElementDefinitions()) {
      if (definition instanceof PrintTextElement text && text.getText() != null) {
        for (EntityRef entity : text.getText().getEntities()) {
          if (entity.getRefId() == null || !definitionIds.contains(entity.getRefId())) {
            errors.add(new ModelValidationError(model, ELEMENT_ID,
                ValidationMessages.get("validation.printElementReferenceIntegrity.missingEntity",
                    definition.getId(), entity.getRefId()), Severity.ERROR.name()));
          }
        }
      }
    }

    if (printModel.getContent().getGeneral() != null && printModel.getContent().getGeneral().getStructure() != null) {
      for (PrintStructureEntry entry : printModel.getContent().getGeneral().getStructure()) {
        if (entry.getId() == null || !segmentIds.contains(entry.getId())) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              ValidationMessages.get("validation.printElementReferenceIntegrity.missingSegment", entry.getId()),
              Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }
}
