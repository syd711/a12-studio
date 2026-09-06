package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelReference;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * One {@link ElementIndex} per Document Model a Form Model references, each built with the project's other
 * Document Models so an id pointing into an {@code include}d model - a compound
 * {@code "<includeGroupId>_<targetId>"} id whose target elements live entirely in the referenced model, not
 * locally (see {@link ElementIndex#resolve}) - resolves correctly instead of being reported as missing.
 */
final class FormDocumentModelIndexes {

  private FormDocumentModelIndexes() {}

  static List<ElementIndex> referencedDocumentModelIndexes(A12Model<?> model, ValidationContext context) {
    List<ElementIndex> indexes = new ArrayList<>();
    if (model.getModelReferences() == null) {
      return indexes;
    }
    for (ModelReference reference : model.getModelReferences()) {
      if (reference.getModelType() != ModelType.DOCUMENT) {
        continue;
      }
      DocumentModel documentModel = context.findOtherDocumentModel(reference.getReference());
      if (documentModel == null || documentModel.getContent() == null || documentModel.getContent().getModelRoot() == null) {
        continue;
      }
      indexes.add(new ElementIndex(documentModel, context.otherDocumentModels()));
    }
    return indexes;
  }
}
