package de.a12.studio.modelsvalidation.validators.tree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.treemodel.ExpansionDepth;
import de.a12.studio.models.treemodel.ExpansionStrategy;
import de.a12.studio.models.treemodel.InitialExpansion;
import de.a12.studio.models.treemodel.TreeChildRelationshipConfiguration;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.treemodel.TreeNode;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The rules SME's Tree meta model has for the expansion strategy. "Tree": at least one expansion depth, each for a
 * relationship one of the node types' child relationship configurations uses, none twice. "Level by level": an initial
 * expansion (if present) has a type, a number of levels for {@code level_limit}, and node types that exist and are not
 * repeated; a page size (if present) is at least 1. A strategy without a type is not checked, as SME doesn't.
 */
public final class TreeExpansionStrategyValidator implements ModelValidator {

  public static final String DEPTHS_ELEMENT_ID = "content/configuration/expansionStrategy/expansionDepths";
  public static final String INITIAL_EXPANSION_ELEMENT_ID = "content/configuration/expansionStrategy/initialExpansion";
  public static final String PAGE_SIZE_ELEMENT_ID = "content/configuration/expansionStrategy/pageSize";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof TreeModel treeModel) || treeModel.getContent().getConfiguration() == null
        || treeModel.getContent().getConfiguration().getExpansionStrategy() == null) {
      return List.of();
    }
    ExpansionStrategy strategy = treeModel.getContent().getConfiguration().getExpansionStrategy();
    List<ModelValidationError> errors = new ArrayList<>();
    if (ExpansionStrategy.TREE.equals(strategy.getType())) {
      validateDepths(treeModel, strategy, errors);
    }
    else if (ExpansionStrategy.LEVEL_BY_LEVEL.equals(strategy.getType())) {
      validateInitialExpansion(treeModel, strategy.getInitialExpansion(), errors);
      if (strategy.getPageSize() != null && strategy.getPageSize() < 1) {
        errors.add(error(treeModel, PAGE_SIZE_ELEMENT_ID, "validation.treeExpansionStrategy.pageSizeTooLow"));
      }
    }
    return errors;
  }

  private static void validateDepths(TreeModel model, ExpansionStrategy strategy, List<ModelValidationError> errors) {
    List<ExpansionDepth> depths = strategy.getExpansionDepths() != null ? strategy.getExpansionDepths() : List.of();
    boolean anyRelationship = depths.stream().anyMatch(depth -> depth.getRelationshipModel() != null && !depth.getRelationshipModel().isBlank());
    if (!anyRelationship) {
      errors.add(error(model, DEPTHS_ELEMENT_ID, "validation.treeExpansionStrategy.depthsEmpty"));
      return;
    }
    Set<String> available = childRelationshipModels(model);
    Set<String> seen = new HashSet<>();
    for (ExpansionDepth depth : depths) {
      String relationship = depth.getRelationshipModel();
      if (relationship == null || relationship.isBlank()) {
        continue;
      }
      if (!available.contains(relationship)) {
        errors.add(error(model, DEPTHS_ELEMENT_ID, "validation.treeExpansionStrategy.depthRelationshipNotFound", relationship));
      }
      if (!seen.add(relationship)) {
        errors.add(error(model, DEPTHS_ELEMENT_ID, "validation.treeExpansionStrategy.depthRelationshipDuplicate", relationship));
      }
    }
  }

  private static void validateInitialExpansion(TreeModel model, InitialExpansion initialExpansion, List<ModelValidationError> errors) {
    if (initialExpansion == null) {
      return;
    }
    if (initialExpansion.getType() == null || initialExpansion.getType().isBlank()) {
      errors.add(error(model, INITIAL_EXPANSION_ELEMENT_ID, "validation.treeExpansionStrategy.initialExpansionTypeMissing"));
    }
    else if (InitialExpansion.LEVEL_LIMIT.equals(initialExpansion.getType())
        && (initialExpansion.getLevel() == null || initialExpansion.getLevel() < 1)) {
      errors.add(error(model, INITIAL_EXPANSION_ELEMENT_ID, "validation.treeExpansionStrategy.initialExpansionLevelMissing"));
    }
    if (initialExpansion.getAffectedNodeRefs() == null) {
      return;
    }
    Set<String> nodeIds = new HashSet<>();
    for (TreeNode node : model.getContent().getNodes()) {
      nodeIds.add(node.getId());
    }
    Set<String> seen = new HashSet<>();
    for (String nodeRef : initialExpansion.getAffectedNodeRefs()) {
      if (nodeRef == null || nodeRef.isBlank()) {
        continue;
      }
      if (!nodeIds.contains(nodeRef)) {
        errors.add(error(model, INITIAL_EXPANSION_ELEMENT_ID, "validation.treeExpansionStrategy.initialExpansionNodeTypeNotFound", nodeRef));
      }
      if (!seen.add(nodeRef)) {
        errors.add(error(model, INITIAL_EXPANSION_ELEMENT_ID, "validation.treeExpansionStrategy.initialExpansionNodeTypeDuplicate", nodeRef));
      }
    }
  }

  /** The relationship models the node types' child relationship configurations use, the choices of an expansion depth. */
  private static Set<String> childRelationshipModels(TreeModel model) {
    Set<String> relationships = new HashSet<>();
    for (TreeNode node : model.getContent().getNodes()) {
      for (TreeChildRelationshipConfiguration configuration : node.getChildRelationshipConfigurations()) {
        if (configuration.getRelationshipModelRef() != null) {
          relationships.add(configuration.getRelationshipModelRef());
        }
      }
    }
    return relationships;
  }

  private static ModelValidationError error(TreeModel model, String elementId, String messageKey, Object... arguments) {
    return new ModelValidationError(model, elementId, ValidationMessages.get(messageKey, arguments), Severity.ERROR.name());
  }
}
