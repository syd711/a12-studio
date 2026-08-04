package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Node ids must be unique, and one Document Model may only back one node type (SME: "One document
 * model can be used for only one node document model reference").
 */
public final class TreeUniqueNodeValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/nodes/documentModelRef";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> seenIds = new HashSet<>();
    Set<String> seenDocumentModels = new HashSet<>();
    for (TreeNode node : treeModel.getContent().getNodes()) {
      if (node.getId() != null && !seenIds.add(node.getId())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.the_node_id"" + node.getId() + "\" is used more than once.", Severity.ERROR.name()));
      }
      if (node.getDocumentModelRef() != null && !node.getDocumentModelRef().isBlank()
          && !seenDocumentModels.add(node.getDocumentModelRef())) {
        errors.add(new ModelValidationError(model, ELEMENT_ID,
            "validation.one_document_model_can_be_used_for_only_one_node_d""
                + node.getDocumentModelRef() + "\").", Severity.ERROR.name()));
      }
    }
    return errors;
  }
}
