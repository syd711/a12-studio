package de.a12.studio.modelsvalidation.validators;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Ports the kernel's shared {@code ModelHeader} rule {@code annotationNamesNotUnique}
 * ({@code RepetitionNotUnique(annotations/name)}, {@code core/ModelHeader.json}): a model's header {@code
 * annotations} list must not declare the same {@code name} twice. Every meta-model composes this rule via the
 * shared {@code I_Annotated}/{@code ModelHeader} mixin, so - like {@link HeaderRolesValidator} - it applies to
 * every model type, not just one; a model whose service registers this should register it regardless of what
 * else that service checks.
 * <p>
 * Scope: the model's own header annotations only. SME's {@code I_Annotated} mixin is also composed into many
 * node types below the header (a Document Model {@code Element}, a Form Model {@code ScreenElement}/{@code
 * Row}/{@code Cell}, ...), each with its own {@code annotations} list that could independently have a
 * duplicate name - that is a separate, much larger cross-cutting gap (would need a per-model-type element
 * walk) and is not covered here.
 */
public final class AnnotationDuplicateValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/annotations";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> seen = new HashSet<>();
    Set<String> reported = new HashSet<>();
    for (Annotation annotation : model.getAnnotations()) {
      String name = annotation.getName();
      if (name == null || name.isBlank()) {
        continue;
      }
      if (!seen.add(name) && reported.add(name)) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.annotationDuplicate", name), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
