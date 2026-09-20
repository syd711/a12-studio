package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * An include a Form Model was expanded from ({@code includeId}/{@code formModelRef}/{@code hostDocumentModelPath}
 * on its top-level elements, see {@code FormIncludeExpander}) must still be expandable: all three properties are
 * there, the included Form Model exists, and the Document Model path still exists in this Form Model's Document
 * Model. Reported once per include, on its first element.
 */
public final class FormIncludeProvenanceValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> reported = new HashSet<>();
    for (ScreenElement element : FormModelWalker.find(formModel.getContent(), ScreenElement.class)) {
      String includeId = element.getIncludeId();
      String formModelRef = element.getFormModelRef();
      String path = element.getHostDocumentModelPath();
      if (isEmpty(includeId) && isEmpty(formModelRef) && path == null) {
        continue;
      }
      if (!reported.add(includeId + "|" + formModelRef + "|" + path)) {
        continue;
      }
      String label = isEmpty(includeId) ? element.getId() : includeId;
      List<String> missing = new ArrayList<>();
      if (isEmpty(includeId)) {
        missing.add("includeId");
      }
      if (isEmpty(formModelRef)) {
        missing.add("formModelRef");
      }
      if (path == null) {
        missing.add("hostDocumentModelPath");
      }
      if (!missing.isEmpty()) {
        errors.add(error(model, element, ValidationMessages.get("validation.formInclude.incomplete", label, String.join(", ", missing))));
      }
      else if (!(context.findOtherModel(formModelRef) instanceof FormModel)) {
        errors.add(error(model, element, ValidationMessages.get("validation.formInclude.modelMissing", label, formModelRef)));
      }
      else if (!path.isBlank() && !path.equals("/") && !indexes.isEmpty()
          && indexes.stream().noneMatch(index -> index.resolveIdByPath(path).isPresent())) {
        errors.add(error(model, element, ValidationMessages.get("validation.formInclude.pathMissing", label, path)));
      }
    }
    return errors;
  }

  private static ModelValidationError error(A12Model<?> model, ScreenElement element, String message) {
    return new ModelValidationError(model, element.getId(), message, Severity.ERROR.name());
  }

  private static boolean isEmpty(String value) {
    return value == null || value.isEmpty();
  }
}
