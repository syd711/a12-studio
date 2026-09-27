package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Label;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeVirtualRoot;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The Virtual Root is shown in the hierarchical column, so each of its labels needs a text (SME:
 * {@code labelIsRequired}, for a language that has an entry). Its actions and context menu follow the action rules of
 * {@link TreeActionsValidator}.
 */
public final class TreeVirtualRootValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/configuration/virtualRoot/label";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel) || treeModel.getContent().getConfiguration() == null) {
      return List.of();
    }
    TreeVirtualRoot virtualRoot = treeModel.getContent().getConfiguration().getVirtualRoot();
    if (virtualRoot == null) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (Label label : virtualRoot.getLabel()) {
      if (label.getLocale() != null && !label.getLocale().isBlank() && (label.getText() == null || label.getText().isBlank())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            ValidationMessages.get("validation.treeVirtualRoot.labelMissing", label.getLocale()), Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
