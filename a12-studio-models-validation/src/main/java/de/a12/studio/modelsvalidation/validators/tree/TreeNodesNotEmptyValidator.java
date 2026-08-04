package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.List;

/** A tree model needs at least one node type (SME: "Node types must not be empty"). */
public final class TreeNodesNotEmptyValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/nodes";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    if (!treeModel.getContent().getNodes().isEmpty()) {
      return List.of();
    }
    return List.of(new ModelValidationError(model, ELEMENT_ID, "Node types must not be empty.", Severity.ERROR.name()));
  }
}
