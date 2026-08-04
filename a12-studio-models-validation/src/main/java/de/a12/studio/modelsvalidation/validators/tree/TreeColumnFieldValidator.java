package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * Node column mappings must point to an existing tree column, and their element refs must resolve to a
 * field of the node's Document Model that is not annotated "indexed" = false (SME reports an error for
 * fields with that annotation because Data Services cannot query them).
 */
public final class TreeColumnFieldValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/nodes/columns";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    List<String> columnIds = treeModel.getContent().getColumns().stream().map(TreeColumn::getId).toList();

    for (TreeNode node : treeModel.getContent().getNodes()) {
      DocumentModel documentModel = context.findOtherDocumentModel(node.getDocumentModelRef());
      ElementIndex index = documentModel != null && documentModel.getContent() != null
          && documentModel.getContent().getModelRoot() != null ? new ElementIndex(documentModel) : null;

      for (TreeNodeColumn mapping : node.getColumns()) {
        if (mapping.getColumnRef() == null || !columnIds.contains(mapping.getColumnRef())) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "The column reference \"" + mapping.getColumnRef() + "\" of node type \"" + node.getId()
                  + "\" does not exist in the tree's columns.", Severity.ERROR.name()));
        }
        if (index == null || mapping.getElementRef() == null || mapping.getElementRef().isBlank()) {
          continue;
        }
        Element element = index.allElements().stream()
            .filter(candidate -> mapping.getElementRef().equals(candidate.getId()))
            .findFirst()
            .orElse(null);
        if (element == null) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "The reference is invalid. The referenced field \"" + mapping.getElementRef()
                  + "\" does not exist in the document model \"" + node.getDocumentModelRef() + "\".",
              Severity.ERROR.name()));
        }
        else if (isIndexedFalse(element)) {
          errors.add(new ModelValidationError(model, ELEMENT_ID,
              "The \"indexed\" annotation of field \"" + element.getName()
                  + "\" should not be false. Please resolve this problem in the corresponding Document Model.",
              Severity.ERROR.name()));
        }
      }
    }
    return errors;
  }

  private static boolean isIndexedFalse(Element element) {
    if (element.getAnnotations() == null) {
      return false;
    }
    for (Annotation annotation : element.getAnnotations()) {
      if ("indexed".equals(annotation.getName()) && "false".equalsIgnoreCase(String.valueOf(annotation.getValue()))) {
        return true;
      }
    }
    return false;
  }
}
