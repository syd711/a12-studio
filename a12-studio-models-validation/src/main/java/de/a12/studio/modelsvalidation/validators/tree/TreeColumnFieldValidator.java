package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeColumn;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.modelsvalidation.validators.ModelValidator;
import de.a12.studio.modelsvalidation.validators.overview.OverviewElementResolution;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Column mappings - of a node type (its own Document Model) and of a child relationship configuration (the relationship's
 * link Document Model) - must point to an existing tree column, each column only once, and their fields must resolve to a
 * field that is not repeatable and not annotated "indexed" = false (SME reports an error for such fields because Data
 * Services cannot query them). The Document Model may be a Combination Model (SME accepts one) and the field a compound
 * id into an included model.
 */
public final class TreeColumnFieldValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/nodes/columns";
  public static final String LINK_ELEMENT_ID = "content/nodes/childRelationshipConfigurations/columns";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    List<ModelValidationError> errors = new ArrayList<>();
    Set<String> columnIds = new HashSet<>();
    for (TreeColumn column : treeModel.getContent().getColumns()) {
      columnIds.add(column.getId());
    }

    for (TreeNode node : treeModel.getContent().getNodes()) {
      String owner = ValidationMessages.get("validation.treeOwner.node", TreeValidationSupport.name(node));
      checkMappings(model, errors, node.getColumns(), columnIds, indexOf(TreeValidationSupport.documentModel(node.getDocumentModelRef(), context), context),
          node.getDocumentModelRef(), owner, TreeValidationSupport.name(node), null, ELEMENT_ID);

      for (TreeChildRelationshipConfiguration configuration : node.getChildRelationshipConfigurations()) {
        if (configuration.getColumns() == null) {
          continue;
        }
        RelationshipModel relationship = TreeValidationSupport.relationships(context).apply(configuration.getRelationshipModelRef());
        String linkDocumentModel = relationship != null && relationship.getContent() != null
            ? relationship.getContent().getLinkDocumentModelValue() : null;
        String linkOwner = ValidationMessages.get("validation.treeOwner.childRelationship", configuration.getRelationshipModelRef(),
            TreeValidationSupport.name(node));
        checkMappings(model, errors, configuration.getColumns(), columnIds, indexOf(TreeValidationSupport.documentModel(linkDocumentModel, context), context),
            linkDocumentModel, linkOwner, TreeValidationSupport.name(node), configuration.getRelationshipModelRef(), LINK_ELEMENT_ID);
      }
    }
    return errors;
  }

  private static ElementIndex indexOf(DocumentModel documentModel, ValidationContext context) {
    return documentModel != null && documentModel.getContent() != null && documentModel.getContent().getModelRoot() != null
        ? new ElementIndex(documentModel, context.otherDocumentModels()) : null;
  }

  private static void checkMappings(A12Model<?> model, List<ModelValidationError> errors, List<TreeNodeColumn> mappings,
      Set<String> columnIds, ElementIndex index, String documentModelId, String owner, String nodeName, String relationshipRef,
      String elementId) {
    Set<String> mapped = new HashSet<>();
    for (TreeNodeColumn mapping : mappings) {
      String columnRef = mapping.getColumnRef();
      if (columnRef == null || columnRef.isBlank()) {
        errors.add(error(model, elementId, "validation.treeColumnField.columnMissing", owner));
      }
      else if (!columnIds.contains(columnRef)) {
        errors.add(relationshipRef == null
            ? error(model, elementId, "validation.treeColumnField.unknownColumn", columnRef, nodeName)
            : error(model, elementId, "validation.treeColumnField.unknownColumnLink", columnRef, relationshipRef, nodeName));
      }
      else if (!mapped.add(columnRef)) {
        errors.add(error(model, elementId, "validation.treeColumnField.duplicateColumn", columnRef, owner));
      }

      String elementRef = mapping.getElementRef();
      if (elementRef == null || elementRef.isBlank()) {
        errors.add(error(model, elementId, "validation.treeColumnField.fieldMissing", columnRef, owner));
        continue;
      }
      if (index == null || OverviewElementResolution.isMetaFieldId(elementRef)) {
        continue;
      }
      Element element = OverviewElementResolution.resolve(index, elementRef);
      if (element == null) {
        errors.add(error(model, elementId, "validation.treeColumnField.missingField", elementRef, documentModelId));
      }
      else if (OverviewElementResolution.isInRepeatableGroup(index, elementRef)) {
        errors.add(error(model, elementId, "validation.treeColumnField.repeatableField", element.getName(), documentModelId));
      }
      else if (OverviewElementResolution.isIndexedFalse(element)) {
        errors.add(error(model, elementId, "validation.common.indexedAnnotationFalse", element.getName()));
      }
    }
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String key, Object... arguments) {
    return new ModelValidationError(model, elementId, ValidationMessages.get(key, arguments), Severity.ERROR.name());
  }
}
