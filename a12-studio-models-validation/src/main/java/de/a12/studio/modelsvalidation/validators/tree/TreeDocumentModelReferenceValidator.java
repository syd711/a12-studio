package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/** Every node type needs a Document Model, and it must exist in the workspace. */
public final class TreeDocumentModelReferenceValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/nodes/documentModelRef";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    for (TreeNode node : treeModel.getContent().getNodes()) {
      if (node.getDocumentModelRef() == null || node.getDocumentModelRef().isBlank()) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.a_document_model_must_be_selected_for_node_type"" + node.getId() + "\".", Severity.ERROR.name()));
      }
      else if (context.findOtherDocumentModel(node.getDocumentModelRef()) == null) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.the_document_model"" + node.getDocumentModelRef() + "\" of node type \"" + node.getId()
                + "\" does not exist in the workspace.", Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
