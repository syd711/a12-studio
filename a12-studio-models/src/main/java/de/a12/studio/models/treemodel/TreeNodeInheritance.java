package de.a12.studio.models.treemodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModelHeterogeneity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * SME's node type inheritance: a node type whose Document Model is a sub type of another node type's Document Model
 * may inherit some of that node type's configuration instead of defining its own. Which parts are inherited is
 * recorded in {@code configuration.inherit} as {@code {"<part>": true}} (see {@link Part}); an inherited part is
 * always empty on the node itself - SME clears it when the flag is set - because the Tree Engine takes it from the
 * super type. Mirrors SME's {@code handleInheritedNodes.ts}.
 */
public final class TreeNodeInheritance {

  private static final String INHERIT = "inherit";

  /** The parts of a node type that can be inherited, with the {@code inherit} key SME writes for each. */
  public enum Part {
    COLUMNS("columns"),
    CHILD_RELATIONSHIP_CONFIGURATIONS("childRelationshipConfigurations"),
    ICON("icon"),
    ACTIONS("actions"),
    CONTEXT_MENU("contextMenu"),
    ROW_ACTIVATION("rowActivation"),
    ROW_TITLE("rowTitle"),
    STYLES("styles");

    private final String key;

    Part(String key) {
      this.key = key;
    }

    public String getKey() {
      return key;
    }
  }

  private TreeNodeInheritance() {
  }

  /**
   * Whether {@code node}'s Document Model is a (direct or indirect) sub type of the Document Model of another node
   * in {@code nodes}, so it has something to inherit from.
   */
  public static boolean isSubTypeNode(TreeNode node, List<TreeNode> nodes, Collection<? extends A12Model<?>> documentModels) {
    if (node.getDocumentModelRef() == null) {
      return false;
    }
    List<String> superTypes = DocumentModelHeterogeneity.reachableSuperTypes(documentModels, node.getDocumentModelRef());
    return nodes.stream()
        .filter(other -> other != node && other.getDocumentModelRef() != null && !other.getDocumentModelRef().equals(node.getDocumentModelRef()))
        .anyMatch(other -> superTypes.contains(other.getDocumentModelRef()));
  }

  public static boolean isInherited(TreeNode node, Part part) {
    Map<String, Object> inherit = inheritMap(node);
    return inherit != null && Boolean.TRUE.equals(inherit.get(part.getKey()));
  }

  /** Whether any part is inherited - such a node keeps offering the inheritance settings even without a super type. */
  public static boolean hasInheritedConfig(TreeNode node) {
    Map<String, Object> inherit = inheritMap(node);
    return inherit != null && inherit.values().stream().anyMatch(Boolean.TRUE::equals);
  }

  /**
   * Marks {@code part} as inherited (or not). Does not touch the node's own content, see {@link #clearContent}. An
   * {@code inherit} object emptied by this is kept, as SME's own files have {@code "inherit": {}}.
   */
  public static void setInherited(TreeNode node, Part part, boolean inherited) {
    Map<String, Object> inherit = inheritMap(node);
    if (inherit == null) {
      if (!inherited) {
        return;
      }
      if (node.getConfiguration() == null) {
        node.setConfiguration(new LinkedHashMap<>());
      }
      inherit = new LinkedHashMap<>();
      node.getConfiguration().put(INHERIT, inherit);
    }
    if (inherited) {
      inherit.put(part.getKey(), Boolean.TRUE);
    }
    else {
      inherit.remove(part.getKey());
    }
  }

  /** Whether {@code node} defines {@code part} itself. */
  public static boolean hasContent(TreeNode node, Part part) {
    return switch (part) {
      case COLUMNS -> !node.getColumns().isEmpty();
      case CHILD_RELATIONSHIP_CONFIGURATIONS -> !node.getChildRelationshipConfigurations().isEmpty();
      case ICON -> node.getIcon() != null && node.getIcon().getName() != null;
      case ACTIONS -> !node.getActions().isEmpty();
      case CONTEXT_MENU -> node.getContextMenu() != null && !node.getContextMenu().getGroups().isEmpty();
      case ROW_ACTIVATION -> node.getRowActivation() != null;
      case ROW_TITLE -> !node.getRowTitle().isEmpty();
      case STYLES -> !node.getStyles().isEmpty();
    };
  }

  /** Empties {@code part} on {@code node}, as SME does once the part is inherited. */
  public static void clearContent(TreeNode node, Part part) {
    switch (part) {
      case COLUMNS -> node.setColumns(new ArrayList<>());
      case CHILD_RELATIONSHIP_CONFIGURATIONS -> node.setChildRelationshipConfigurations(new ArrayList<>());
      case ICON -> node.setIcon(null);
      case ACTIONS -> node.setActions(new ArrayList<>());
      case CONTEXT_MENU -> node.setContextMenu(null);
      case ROW_ACTIVATION -> node.setRowActivation(null);
      case ROW_TITLE -> node.setRowTitle(new ArrayList<>());
      case STYLES -> node.setStyles(new ArrayList<>());
    }
  }

  @SuppressWarnings("unchecked")
  private static Map<String, Object> inheritMap(TreeNode node) {
    if (node.getConfiguration() != null && node.getConfiguration().get(INHERIT) instanceof Map<?, ?> inherit) {
      return (Map<String, Object>) inherit;
    }
    return null;
  }
}
