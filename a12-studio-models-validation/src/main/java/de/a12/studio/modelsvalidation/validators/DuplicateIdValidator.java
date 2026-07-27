package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Port of the kernel's IdUniqueRule (decompiled from kernel-md-model, EUPL-1.2 dual-licensed): every
 * element id in a document model must be unique.
 */
public final class DuplicateIdValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel documentModel)) {
      return List.of();
    }

    ElementIndex index = new ElementIndex(documentModel);
    Map<String, List<Element>> byId = new HashMap<>();
    for (Element element : index.allElements()) {
      if (element.getId() != null) {
        byId.computeIfAbsent(element.getId(), k -> new ArrayList<>()).add(element);
      }
    }
    List<ModelValidationError> errors = new ArrayList<>();
    byId.forEach((id, elements) -> {
      if (elements.size() > 1) {
        String elementPaths = elements.stream().map(index::getPath).reduce((a, b) -> a + ", " + b).orElse("");
        for (Element element : elements) {
          errors.add(new ModelValidationError(model, element.getId(), ElementProperty.GENERAL,
              "The id [" + id + "] of element on path '" + index.getPath(element) + "' is not unique: Used by [" + elementPaths + "].",
              Severity.ERROR.name()));
        }
      }
    });
    return errors;
  }
}
