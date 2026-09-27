package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The header annotation name {@code "bindingConfiguration"} is reserved: the Overview Model's binding-purpose
 * resolution reads it as structured JSON (see {@code OverviewBindingPurpose} and the {@code
 * bindingConfiguration} note in the Overview Model section of {@code docs/sme-reference-comparison.md}), so a
 * hand-authored annotation of that name would silently corrupt that resolution - SME's {@code
 * editor_annotationNameMustNotBeReserved}. {@code AnnotationsPanelController} (UI module) already hides this
 * name from its own rows, but nothing stopped it from being typed in the first place; this is the validator
 * half.
 */
public final class FormReservedAnnotationNameValidator implements ModelValidator {

  public static final String ELEMENT_ID = "header/annotations";

  private static final String BINDING_CONFIGURATION_ANNOTATION_NAME = "bindingConfiguration";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel) || model.getAnnotations() == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Annotation annotation : model.getAnnotations()) {
      if (BINDING_CONFIGURATION_ANNOTATION_NAME.equals(annotation.getName())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.formAnnotation.reservedName", BINDING_CONFIGURATION_ANNOTATION_NAME),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
