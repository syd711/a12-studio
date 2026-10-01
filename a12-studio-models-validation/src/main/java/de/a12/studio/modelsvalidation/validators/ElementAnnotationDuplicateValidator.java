package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Element-level counterpart of {@link AnnotationDuplicateValidator} for Document Models: SME's shared {@code
 * I_Annotated} mixin ({@code RepetitionNotUnique(annotations/name)}) is composed into every element, so no
 * element may declare the same annotation {@code name} twice. Form Model nodes are not covered here.
 */
public final class ElementAnnotationDuplicateValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof DocumentModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Element element : context.elementIndex().allElements()) {
      Set<String> seen = new HashSet<>();
      Set<String> reported = new HashSet<>();
      for (Annotation annotation : element.getAnnotations()) {
        String name = annotation.getName();
        if (name == null || name.isBlank()) {
          continue;
        }
        if (!seen.add(name) && reported.add(name)) {
          errors.add(new ModelValidationError(model, element.getId(), ElementProperty.GENERAL,
              ValidationMessages.get("validation.annotationDuplicate", name), Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }
}
