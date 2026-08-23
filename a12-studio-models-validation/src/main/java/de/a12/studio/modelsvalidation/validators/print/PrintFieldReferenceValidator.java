package de.a12.studio.modelsvalidation.validators.print;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.printmodel.FieldRef;
import de.a12.studio.models.printmodel.PrintElementDefinition;
import de.a12.studio.models.printmodel.PrintFieldElement;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Field elements must reference a Document Model existing in the workspace, and their slash-separated
 * field path must resolve to a field of that model (print engine "Reference Validation": all field
 * references must resolve against the referenced Document Model).
 */
public final class PrintFieldReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/elementDefinitions/field";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof PrintModel printModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    Map<String, ElementIndex> indexCache = new HashMap<>();

    for (PrintElementDefinition definition : printModel.getContent().getElementDefinitions()) {
      if (!(definition instanceof PrintFieldElement fieldElement) || fieldElement.getField() == null) {
        continue;
      }
      FieldRef field = fieldElement.getField();
      if (field.getModel() == null || field.getModel().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.printFieldReference.missingModelReference", definition.getId()),
            Severity.ERROR.name()));
        continue;
      }
      DocumentModel documentModel = context.findOtherDocumentModel(field.getModel());
      if (documentModel == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.printFieldReference.missingModel", field.getModel()),
            Severity.ERROR.name()));
        continue;
      }
      if (field.getPath() == null || field.getPath().isBlank()) {
        continue;
      }
      ElementIndex index = indexCache.computeIfAbsent(field.getModel(), key -> new ElementIndex(documentModel));
      if (!pathResolves(index, field.getPath())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.printFieldReference.unresolvablePath", field.getPath(), field.getModel()),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static boolean pathResolves(ElementIndex index, String path) {
    for (Element element : index.allElements()) {
      if (path.equals(index.getPath(element))) {
        return true;
      }
    }
    return false;
  }
}
