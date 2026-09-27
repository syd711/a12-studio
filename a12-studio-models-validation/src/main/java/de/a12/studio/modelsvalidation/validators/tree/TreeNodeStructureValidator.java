package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeHeterogeneity;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.models.treemodel.TreeNodeColumn;
import de.a12.studio.models.treemodel.TreeNodeInheritance;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * The rules SME's {@code customConditions} put on how node types fit together:
 * <ul>
 *   <li>a node type needs at least one column - unless it inherits its columns, or a parent node type's child relationship
 *       configuration maps columns that show on it ({@code columnMustNotBeEmpty}, {@code TMNoColumnRefInParentRelationshipConfig});</li>
 *   <li>a relationship is used once per node type ({@code relationshipModelRefMustUnique}, not for inherited ones);</li>
 *   <li>an inheriting node type needs a super type node type to inherit from ({@code nodeTypeHasNoParentToInherit});</li>
 *   <li>a column mapped in a child relationship configuration must not be mapped again by a child node type
 *       ({@code columnRefIsUsedInChildNode});</li>
 *   <li>a warning when node types lead back to each other ({@code hasCircularRelationship}).</li>
 * </ul>
 */
public final class TreeNodeStructureValidator implements ModelValidator {

  public static final String ELEMENT_ID = "content/nodes";

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
      if (node.getDocumentModelRef() != null && !node.getDocumentModelRef().isBlank()) {
        if (needsColumns(node, nodes, relationships, documentModels)) {
          errors.add(error(model, "validation.treeNodeStructure.columnsMissing", Severity.ERROR, name));
        }
        if (TreeNodeInheritance.hasInheritedConfig(node) && !TreeNodeInheritance.isSubTypeNode(node, nodes, documentModels)) {
          errors.add(error(model, "validation.treeNodeStructure.nothingToInherit", Severity.ERROR, name));
        }
        if (TreeHeterogeneity.hasCircularRelationship(node, nodes, relationships, documentModels)) {
          errors.add(error(model, "validation.treeNodeStructure.circular", Severity.WARNING, name));
        }
      }
      if (!TreeNodeInheritance.isInherited(node, TreeNodeInheritance.Part.CHILD_RELATIONSHIP_CONFIGURATIONS)) {
        Set<String> seen = new HashSet<>();
        for (TreeChildRelationshipConfiguration configuration : node.getChildRelationshipConfigurations()) {
          String relationship = configuration.getRelationshipModelRef();
          if (relationship != null && !relationship.isBlank() && !seen.add(relationship)) {
            errors.add(error(model, "validation.treeNodeStructure.relationshipTwice", Severity.ERROR, relationship, name));
          }
        }
      }
      checkChildMappings(model, errors, node, nodes, relationships, documentModels);
    }
    return errors;
  }

  /**
   * Whether {@code node} has no column of its own and nothing that makes up for it: SME's rule fires when it does not
   * inherit its columns and has none, and either no child relationship configuration leads to it, or one that does maps no
   * columns.
   */
  private static boolean needsColumns(TreeNode node, List<TreeNode> nodes, Function<String, RelationshipModel> relationships,
      List<A12Model<?>> documentModels) {
    if (TreeNodeInheritance.isInherited(node, TreeNodeInheritance.Part.COLUMNS)
        || node.getColumns().stream().anyMatch(column -> column.getColumnRef() != null && !column.getColumnRef().isBlank())) {
      return false;
    }
    List<TreeChildRelationshipConfiguration> parents = new ArrayList<>();
    for (TreeNode other : nodes) {
      for (TreeChildRelationshipConfiguration configuration : other.getChildRelationshipConfigurations()) {
        TreeHeterogeneity.SubTypesInfo info = TreeHeterogeneity.childInfo(configuration, relationships, documentModels);
        if (info != null && (info.superType().equals(node.getDocumentModelRef())
            || TreeHeterogeneity.isSubTypeOf(documentModels, node.getDocumentModelRef(), info.superType()))) {
          parents.add(configuration);
        }
      }
    }
    return parents.isEmpty() || parents.stream().anyMatch(configuration -> configuration.getColumns() == null || configuration.getColumns().isEmpty());
  }

  private static void checkChildMappings(A12Model<?> model, List<ModelValidationError> errors, TreeNode node, List<TreeNode> nodes,
      Function<String, RelationshipModel> relationships, List<A12Model<?>> documentModels) {
    for (TreeChildRelationshipConfiguration configuration : node.getChildRelationshipConfigurations()) {
      if (configuration.getColumns() == null || configuration.getColumns().isEmpty()) {
        continue;
      }
      TreeHeterogeneity.SubTypesInfo info = TreeHeterogeneity.childInfo(configuration, relationships, documentModels);
      if (info == null) {
        continue;
      }
      List<String> childDocuments = TreeHeterogeneity.allDocuments(documentModels, info, false);
      List<TreeNode> childNodes = nodes.stream()
          .filter(candidate -> candidate.getDocumentModelRef() != null && childDocuments.contains(candidate.getDocumentModelRef()))
          .toList();
      for (TreeNodeColumn mapping : configuration.getColumns()) {
        String columnRef = mapping.getColumnRef();
        if (columnRef != null && childNodes.stream().anyMatch(child -> child.getColumns().stream()
            .anyMatch(column -> columnRef.equals(column.getColumnRef())))) {
          errors.add(error(model, "validation.treeNodeStructure.columnUsedInChild", Severity.ERROR, columnRef,
              configuration.getRelationshipModelRef(), TreeValidationSupport.name(node)));
        }
      }
    }
  }

  private static ModelValidationError error(A12Model<?> model, String key, Severity severity, Object... arguments) {
    return new ModelValidationError(model, ELEMENT_ID, ValidationMessages.get(key, arguments), severity.name());
  }
}
