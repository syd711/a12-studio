package de.a12.studio.modelsvalidation.validators.form;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.formmodel.Control;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.formmodel.FormModelWalker;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * A Control that shows a repeatable field outside the repeat that field is in (SME's {@code isIndexableControl},
 * see {@link ControlIndexSupport}) has to say which repetition it shows, so it needs an index. Whether a Control
 * needs one depends on the Document Model - a group that became repeatable, or a field that moved into one, turns
 * a fine Control into an ambiguous one - which is the SME backend's consistency error "indexable Control without
 * index". A Control that carries an index without needing one is not reported: the index is simply not used.
 */
public final class FormControlIndexRequiredValidator implements ModelValidator {

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof FormModel formModel) || formModel.getContent() == null) {
      return List.of();
    }
    List<ElementIndex> indexes = FormDocumentModelIndexes.referencedDocumentModelIndexes(model, context);
    if (indexes.isEmpty()) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Control control : FormModelWalker.find(formModel.getContent(), Control.class)) {
      if (control.getIndex() != null && control.getIndex().getType() != null && control.getIndex().getValue() != null) {
        continue;
      }
      Optional<ElementIndex> index = indexes.stream().filter(candidate -> candidate.isResolvable(control.getElementRef())).findFirst();
      if (index.isPresent() && ControlIndexSupport.isIndexable(control, formModel.getContent(), index.get())) {
        errors.add(new ModelValidationError(formModel, control.getId(),
            ValidationMessages.get("validation.controlIndex.required", DependentControlOptionsMustExistValidator.controlLabel(control)),
            Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
