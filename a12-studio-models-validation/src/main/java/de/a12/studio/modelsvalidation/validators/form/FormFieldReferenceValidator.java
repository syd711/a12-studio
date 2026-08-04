package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FieldConfigEntry;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Every field configuration entry must reference a field that still exists in one of the referenced
 * Document Models (the SME problems view reports "a field is referenced in the Form Model that no
 * longer exists in the Document Model").
 */
public final class FormFieldReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/fieldConfiguration";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel)
        || formModel.getContent() == null
        || formModel.getContent().getFieldConfiguration() == null) {
      return List.of();
    }

    Set<String> knownElementIds = collectReferencedDocumentModelElementIds(model, context);
    if (knownElementIds.isEmpty()) {
      // Without a resolvable document model there is nothing to check against
      // (FormDocumentModelReferenceValidator already reports the missing reference).
      return List.of();
    }

    List<ModelValidationError> errors = new ArrayList<>();
    for (FieldConfigEntry entry : formModel.getContent().getFieldConfiguration().getField()) {
      if (entry.getElementRef() != null && !entry.getElementRef().isBlank()
          && !knownElementIds.contains(entry.getElementRef())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "The field \"" + entry.getElementRef()
                + "\" is referenced in the Form Model but does not exist in the referenced Document Model.",
            Severity.ERROR.name()));
      }
    }
    return errors;
  }

  private static Set<String> collectReferencedDocumentModelElementIds(A12Model<?> model, ValidationContext context) {
    Set<String> ids = new HashSet<>();
    if (model.getModelReferences() == null) {
      return ids;
    }
    for (ModelReference reference : model.getModelReferences()) {
      if (reference.getModelType() != ModelType.DOCUMENT) {
        continue;
      }
      DocumentModel documentModel = context.findOtherDocumentModel(reference.getReference());
      if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
        continue;
      }
      new ElementIndex(documentModel).allElements().forEach(element -> {
        if (element.getId() != null) {
          ids.add(element.getId());
        }
      });
    }
    return ids;
  }
}
