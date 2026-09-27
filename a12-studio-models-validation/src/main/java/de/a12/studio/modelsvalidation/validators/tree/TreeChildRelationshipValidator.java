package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeHeterogeneity;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * A node type's child relationship configurations: the relationship and the parent role must be set, the relationship
 * must exist and have an entity of the node type's Document Model (or of a super type), the parent role must be that
 * entity's role (SME: "Invalid Reference"), and the child side must be covered by node types - an error when none is
 * added for it, a warning ("Missing node type for child role of relationship") when it is only partly covered, which
 * makes the Tree Engine fail at runtime for a document it has no node type for.
 */
public final class TreeChildRelationshipValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/nodes/childRelationshipConfigurations";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel)) {
      return List.of();
    }
    List<TreeNode> nodes = treeModel.getContent().getNodes();
    List<A12Model<?>> documentModels = TreeValidationSupport.heterogeneityModels(context);
    Function<String, RelationshipModel> relationships = TreeValidationSupport.relationships(context);
    List<ModelValidationError> errors = new ArrayList<>();

    for (TreeNode node : nodes) {
      String name = TreeValidationSupport.name(node);
      for (TreeChildRelationshipConfiguration configuration : node.getChildRelationshipConfigurations()) {
        String relationshipRef = configuration.getRelationshipModelRef();
        String parentRole = configuration.getParentRole();
        boolean relationshipSet = relationshipRef != null && !relationshipRef.isBlank();
        boolean roleSet = parentRole != null && !parentRole.isBlank();
        if (!relationshipSet) {
          errors.add(error(model, Severity.ERROR, "validation.treeChildRelationship.relationshipMissing", name));
        }
        if (!roleSet) {
          errors.add(error(model, Severity.ERROR, "validation.treeChildRelationship.parentRoleMissing", relationshipRef, name));
        }
        RelationshipModel relationship = relationshipSet ? relationships.apply(relationshipRef) : null;
        if (relationshipSet && relationship == null) {
          errors.add(error(model, Severity.ERROR, "validation.treeChildRelationship.relationshipNotFound", relationshipRef, name));
          continue;
        }
        String documentModelId = node.getDocumentModelRef();
        if (relationship != null && documentModelId != null && !documentModelId.isBlank()) {
          if (!TreeHeterogeneity.relationshipFits(relationship, documentModelId, documentModels)) {
            errors.add(error(model, Severity.ERROR, "validation.treeChildRelationship.relationshipDoesNotFit", relationshipRef, documentModelId));
            continue;
          }
          if (roleSet && !TreeHeterogeneity.fittingRoles(relationship, documentModelId, documentModels).contains(parentRole)) {
            errors.add(error(model, Severity.ERROR, "validation.treeChildRelationship.parentRoleInvalid", parentRole, relationshipRef, documentModelId));
          }
        }
        if (relationship != null && roleSet) {
          checkNodeTypes(model, errors, configuration, nodes, relationships, documentModels);
        }
      }
    }
    return errors;
  }

  private static void checkNodeTypes(A12Model<?> model, List<ModelValidationError> errors, TreeChildRelationshipConfiguration configuration,
      List<TreeNode> nodes, Function<String, RelationshipModel> relationships, List<A12Model<?>> documentModels) {
    TreeHeterogeneity.SubTypesInfo info = TreeHeterogeneity.childInfo(configuration, relationships, documentModels);
    if (info == null) {
      return;
    }
    if (TreeHeterogeneity.noNodeTypeIsAdded(documentModels, info.superType(), nodes)) {
      errors.add(error(model, Severity.ERROR, "validation.treeChildRelationship.noNodeType", configuration.getRelationshipModelRef(), info.superType()));
    }
    else if (TreeHeterogeneity.isMissingNodeType(documentModels, info, nodes)) {
      errors.add(error(model, Severity.WARNING, "validation.treeChildRelationship.missingNodeType", configuration.getRelationshipModelRef(), info.superType()));
    }
  }

  private static ModelValidationError error(A12Model<?> model, Severity severity, String key, Object... arguments) {
    return new ModelValidationError(model, ELEMENT_ID, ValidationMessages.get(key, arguments), severity.name());
  }
}
