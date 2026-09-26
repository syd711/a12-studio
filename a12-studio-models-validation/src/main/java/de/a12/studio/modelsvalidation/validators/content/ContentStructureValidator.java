package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.contentmodel.ContentElementLibrary;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.contentmodel.ContentModule;
import de.a12.studio.models.contentmodel.ContentStructure;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The element tree has to keep to the parent and child rules of its element types (a Table has a head, a body and a
 * foot in this order, a Grid only holds Rows, ...). SME's editor never lets the user break them, so a tree that does was
 * edited by hand or by another tool; the Content Engine cannot rely on such a tree. See {@link ContentStructure}.
 */
public final class ContentStructureValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof ContentModel contentModel) || contentModel.getContent() == null || contentModel.getContent().getRoot() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (ContentStructure.Violation violation : ContentStructure.violations(contentModel.getContent().getRoot())) {
      if (violation.element().getId() == null) {
        continue;
      }
      errors.add(switch (violation.kind()) {
        case PARENT_NOT_ALLOWED -> ContentTree.finding(contentModel, violation.element(), Severity.ERROR,
            "validation.contentStructure.parentNotAllowed", violation.parentType());
        case CHILDREN_NOT_ALLOWED -> ContentTree.finding(contentModel, violation.element(), Severity.ERROR,
            "validation.contentStructure.childrenNotAllowed",
            violation.childTypes().isEmpty() ? "-" : String.join(", ", violation.childTypes()),
            ContentElementLibrary.find(violation.element()).map(ContentModule::label).orElse(""));
      });
    }
    return errors;
  }
}
