package de.a12.studio.models.treemodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModelHeterogeneity;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.RelationshipModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * What the Tree Model rules and pickers need to know about the Document Models' super type / sub type graph and the
 * relationships between them: which Document Model a child relationship configuration leads to, which node types
 * cover it, whether the node types form a cycle, and which Document Models an insert action may create. A port of SME's
 * {@code childRelationshipConfigurationHelper.ts} and the candidate calculation of its {@code tmReferenceProvider.ts};
 * pure functions over the models, so validators and editor panels share them.
 */
public final class TreeHeterogeneity {

  private static final int MAX_SUPER_TYPE_LEVELS = 10;

  /** A Document Model with its direct sub types (SME's {@code DocumentSubTypesInfo}). */
  public record SubTypesInfo(String superType, boolean isAbstract, List<String> subTypes) {
  }

  private TreeHeterogeneity() {
  }

  /** {@code id}'s sub type info, or {@code null} if it is not one of {@code documentModels}. */
  public static SubTypesInfo info(Collection<? extends A12Model<?>> documentModels, String id) {
    if (id == null) {
      return null;
    }
    A12Model<?> model = documentModels.stream().filter(candidate -> id.equals(candidate.getId())).findFirst().orElse(null);
    if (model == null) {
      return null;
    }
    return new SubTypesInfo(id, DocumentModelHeterogeneity.isAbstract(model),
        DocumentModelHeterogeneity.directSubTypes(documentModels, id));
  }

  /** Whether {@code documentRef} is a (direct or indirect, up to ten levels) sub type of {@code superTypeRef}. */
  public static boolean isSubTypeOf(Collection<? extends A12Model<?>> documentModels, String documentRef, String superTypeRef) {
    return isSubTypeOf(documentModels, documentRef, superTypeRef, 1);
  }

  private static boolean isSubTypeOf(Collection<? extends A12Model<?>> documentModels, String documentRef, String superTypeRef, int level) {
    List<String> subTypes = DocumentModelHeterogeneity.directSubTypes(documentModels, superTypeRef);
    if (subTypes.contains(documentRef)) {
      return true;
    }
    return level < MAX_SUPER_TYPE_LEVELS && subTypes.stream()
        .anyMatch(subType -> isSubTypeOf(documentModels, documentRef, subType, level + 1));
  }

  /**
   * The Document Model {@code configuration} leads to: the one of the relationship's two entities that is not the
   * {@code parentRole}. {@code null} if the relationship, the role or the Document Model does not resolve.
   */
  public static SubTypesInfo childInfo(TreeChildRelationshipConfiguration configuration,
      Function<String, RelationshipModel> relationships, Collection<? extends A12Model<?>> documentModels) {
    String childDocumentModel = childDocumentModelId(configuration, relationships);
    return childDocumentModel == null ? null : info(documentModels, childDocumentModel);
  }

  /** The id of the Document Model on the child side of {@code configuration}'s relationship, or {@code null}. */
  public static String childDocumentModelId(TreeChildRelationshipConfiguration configuration,
      Function<String, RelationshipModel> relationships) {
    String relationshipRef = configuration.getRelationshipModelRef();
    if (relationshipRef == null || relationshipRef.isBlank()) {
      return null;
    }
    RelationshipModel relationship = relationships.apply(relationshipRef);
    if (relationship == null || relationship.getContent() == null) {
      return null;
    }
    return relationship.getContent().getEntityCharacteristics().stream()
        .filter(entity -> entity.getRole() == null || !entity.getRole().equals(configuration.getParentRole()))
        .map(EntityCharacteristic::getDocumentModel)
        .findFirst()
        .orElse(null);
  }

  /** {@code info}'s Document Model and all its sub types, recursively; {@code excludeAbstract} drops the abstract ones. */
  public static List<String> allDocuments(Collection<? extends A12Model<?>> documentModels, SubTypesInfo info, boolean excludeAbstract) {
    List<String> result = new ArrayList<>();
    collect(documentModels, info, excludeAbstract, result, 0);
    return result;
  }

  private static void collect(Collection<? extends A12Model<?>> documentModels, SubTypesInfo info, boolean excludeAbstract,
      List<String> result, int depth) {
    if (depth > 50) {
      return;
    }
    if (!excludeAbstract || !info.isAbstract()) {
      result.add(info.superType());
    }
    for (String subType : info.subTypes()) {
      SubTypesInfo subInfo = info(documentModels, subType);
      if (subInfo == null) {
        result.add(subType);
      }
      else {
        collect(documentModels, subInfo, excludeAbstract, result, depth + 1);
      }
    }
  }

  /** Whether no node type is the Document Model {@code superType} or one of its sub types. */
  public static boolean noNodeTypeIsAdded(Collection<? extends A12Model<?>> documentModels, String superType, List<TreeNode> nodes) {
    return nodes.stream().noneMatch(node -> node.getDocumentModelRef() != null
        && (superType.equals(node.getDocumentModelRef()) || isSubTypeOf(documentModels, node.getDocumentModelRef(), superType)));
  }

  /**
   * SME's "Missing node type for child role of relationship": the Document Model of {@code info} is neither a node type
   * itself nor - when it is abstract - covered by a node type (or, recursively, a covered sub type) for each of its sub
   * types, so the Tree Engine would meet a document it has no node type for.
   */
  public static boolean isMissingNodeType(Collection<? extends A12Model<?>> documentModels, SubTypesInfo info, List<TreeNode> nodes) {
    return isMissingNodeType(documentModels, info, nodes, 0);
  }

  private static boolean isMissingNodeType(Collection<? extends A12Model<?>> documentModels, SubTypesInfo info, List<TreeNode> nodes, int depth) {
    if (nodes.stream().anyMatch(node -> info.superType().equals(node.getDocumentModelRef()))) {
      return false;
    }
    if (!info.isAbstract() || depth > 50) {
      return true;
    }
    for (String subType : info.subTypes()) {
      if (nodes.stream().anyMatch(node -> subType.equals(node.getDocumentModelRef()))) {
        continue;
      }
      SubTypesInfo subInfo = info(documentModels, subType);
      if (subInfo == null || isMissingNodeType(documentModels, subInfo, nodes, depth + 1)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether {@code child}, a node type reached from {@code parent}, leads back to {@code parent}'s Document Model through
   * one of its own child relationship configurations (SME's {@code hasCircularRelationship}).
   */
  public static boolean leadsBackTo(TreeNode parent, TreeNode child, Function<String, RelationshipModel> relationships,
      Collection<? extends A12Model<?>> documentModels) {
    if (parent.getDocumentModelRef() == null || parent.getDocumentModelRef().equals(child.getDocumentModelRef())) {
      return false;
    }
    for (TreeChildRelationshipConfiguration configuration : child.getChildRelationshipConfigurations()) {
      SubTypesInfo info = childInfo(configuration, relationships, documentModels);
      if (info != null && allDocuments(documentModels, info, false).contains(parent.getDocumentModelRef())) {
        return true;
      }
    }
    return false;
  }

  /** Whether the child node types of one of {@code node}'s relationships lead back to {@code node} (a cycle). */
  public static boolean hasCircularRelationship(TreeNode node, List<TreeNode> nodes,
      Function<String, RelationshipModel> relationships, Collection<? extends A12Model<?>> documentModels) {
    for (TreeChildRelationshipConfiguration configuration : node.getChildRelationshipConfigurations()) {
      SubTypesInfo info = childInfo(configuration, relationships, documentModels);
      if (info == null) {
        continue;
      }
      List<String> childDocuments = new ArrayList<>();
      childDocuments.add(info.superType());
      childDocuments.addAll(info.subTypes());
      for (TreeNode child : nodes) {
        if (child.getDocumentModelRef() != null && childDocuments.contains(child.getDocumentModelRef())
            && leadsBackTo(node, child, relationships, documentModels)) {
          return true;
        }
      }
    }
    return false;
  }

  /** Whether {@code relationship} has an entity of {@code documentModelId} or of one of its super types. */
  public static boolean relationshipFits(RelationshipModel relationship, String documentModelId, Collection<? extends A12Model<?>> documentModels) {
    return relationship != null && relationship.getContent() != null && documentModelId != null
        && relationship.getContent().getEntityCharacteristics().stream()
        .anyMatch(entity -> entityFits(entity, documentModelId, documentModels));
  }

  /** The roles of {@code relationship} whose entity is {@code documentModelId} or one of its super types. */
  public static List<String> fittingRoles(RelationshipModel relationship, String documentModelId, Collection<? extends A12Model<?>> documentModels) {
    if (relationship == null || relationship.getContent() == null || documentModelId == null) {
      return List.of();
    }
    return relationship.getContent().getEntityCharacteristics().stream()
        .filter(entity -> entity.getRole() != null && entityFits(entity, documentModelId, documentModels))
        .map(EntityCharacteristic::getRole)
        .toList();
  }

  private static boolean entityFits(EntityCharacteristic entity, String documentModelId, Collection<? extends A12Model<?>> documentModels) {
    return documentModelId.equals(entity.getDocumentModel()) || isSubTypeOf(documentModels, documentModelId, entity.getDocumentModel());
  }

  /**
   * The Document Models an insert action of {@code node} may create, by its {@code position}: as child, those of the
   * node's child relationships (its own, or the inherited ones); above/below, the node's own Document Model with its sub
   * types and those of every sibling node type. Abstract Document Models are left out. Sorted by id.
   */
  public static List<String> insertCandidates(TreeModel model, TreeNode node, String position,
      Function<String, RelationshipModel> relationships, Collection<? extends A12Model<?>> documentModels) {
    Set<String> result = new LinkedHashSet<>();
    if (TreeNodeAction.POSITION_AS_CHILD.equals(position)) {
      for (TreeChildRelationshipConfiguration configuration : effectiveChildRelationships(model.getContent().getNodes(), node, documentModels)) {
        SubTypesInfo info = childInfo(configuration, relationships, documentModels);
        if (info != null) {
          result.addAll(allDocuments(documentModels, info, true));
        }
      }
    }
    else if (position != null && node.getDocumentModelRef() != null) {
      SubTypesInfo own = info(documentModels, node.getDocumentModelRef());
      if (own != null) {
        result.addAll(allDocuments(documentModels, own, true));
      }
      else {
        result.add(node.getDocumentModelRef());
      }
      for (TreeNode other : model.getContent().getNodes()) {
        for (TreeChildRelationshipConfiguration configuration : other.getChildRelationshipConfigurations()) {
          SubTypesInfo info = childInfo(configuration, relationships, documentModels);
          if (info == null) {
            continue;
          }
          List<String> siblings = allDocuments(documentModels, info, true);
          if (siblings.contains(node.getDocumentModelRef())) {
            result.addAll(siblings);
          }
        }
      }
    }
    return result.stream().sorted().toList();
  }

  /**
   * The Document Models an insert action of the Virtual Root may create: the Document Model of the node type that owns
   * the tree's Root relationship, and its direct sub types. Empty while there is no valid Root.
   */
  public static List<String> rootInsertCandidates(TreeModel model, Collection<? extends A12Model<?>> documentModels) {
    TreeConfiguration configuration = model.getContent().getConfiguration();
    String rootRef = configuration != null ? configuration.getRootRef() : null;
    if (rootRef == null || rootRef.isBlank()) {
      return List.of();
    }
    for (TreeNode node : model.getContent().getNodes()) {
      boolean ownsRoot = node.getChildRelationshipConfigurations().stream().anyMatch(candidate -> rootRef.equals(candidate.getId()));
      if (!ownsRoot || node.getDocumentModelRef() == null) {
        continue;
      }
      SubTypesInfo info = info(documentModels, node.getDocumentModelRef());
      List<String> result = new ArrayList<>(info != null ? info.subTypes() : List.of());
      if (!result.contains(node.getDocumentModelRef())) {
        result.add(0, node.getDocumentModelRef());
      }
      return result;
    }
    return List.of();
  }

  /** The child relationship configurations {@code node} uses: its own, or - when it inherits them - its super type's. */
  public static List<TreeChildRelationshipConfiguration> effectiveChildRelationships(List<TreeNode> nodes, TreeNode node,
      Collection<? extends A12Model<?>> documentModels) {
    return effectiveChildRelationships(nodes, node, documentModels, 0);
  }

  private static List<TreeChildRelationshipConfiguration> effectiveChildRelationships(List<TreeNode> nodes, TreeNode node,
      Collection<? extends A12Model<?>> documentModels, int depth) {
    if (!node.getChildRelationshipConfigurations().isEmpty()
        || !TreeNodeInheritance.isInherited(node, TreeNodeInheritance.Part.CHILD_RELATIONSHIP_CONFIGURATIONS)
        || node.getDocumentModelRef() == null || depth > 20) {
      return node.getChildRelationshipConfigurations();
    }
    for (TreeNode superNode : nodes) {
      if (superNode == node || superNode.getDocumentModelRef() == null
          || superNode.getDocumentModelRef().equals(node.getDocumentModelRef())) {
        continue;
      }
      if (!DocumentModelHeterogeneity.directSubTypes(documentModels, superNode.getDocumentModelRef()).contains(node.getDocumentModelRef())) {
        continue;
      }
      if (!superNode.getChildRelationshipConfigurations().isEmpty()) {
        return superNode.getChildRelationshipConfigurations();
      }
      if (TreeNodeInheritance.isInherited(superNode, TreeNodeInheritance.Part.CHILD_RELATIONSHIP_CONFIGURATIONS)) {
        return effectiveChildRelationships(nodes, superNode, documentModels, depth + 1);
      }
    }
    return List.of();
  }
}
