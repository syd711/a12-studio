package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The groups and fields of a Document Model as SME's Content Engine sees them: with every Include expanded, an element
 * of an included model under the id {@code <include group id>_<id in the included model>} (nested includes chain the
 * prefixes). What a Content Model may reference, and from where, is decided on this tree - by the validators and by the
 * editor's pickers, which is why both use this class:
 * <ul>
 *   <li>a group is a candidate when it lies below the <em>data context</em> of the referencing element - the base group
 *       or the closest enclosing Repeatable Group, or anywhere at the top (SME's {@code candidateGroups});</li>
 *   <li>a field is a candidate when it can be reached from the data context or one of the groups above it by
 *       descending through groups that are not repeated (SME's {@code candidateFields}).</li>
 * </ul>
 * Rules and computations are not part of it, nobody can reference them.
 */
public final class DocumentStructure {

  /** A group or field of the expanded Document Model. */
  public static final class Node {

    public final String id;
    public final Element element;
    public final @Nullable Node parent;
    public final List<Node> children = new ArrayList<>();
    // The index of the model the element is defined in, which knows its type definitions.
    private final ElementIndex owner;

    private Node(String id, Element element, Node parent, ElementIndex owner) {
      this.id = id;
      this.element = element;
      this.parent = parent;
      this.owner = owner;
    }

    public boolean isGroup() {
      return element instanceof GroupElement;
    }

    public boolean isField() {
      return element instanceof FieldElement;
    }

    /** How often the group may occur; 1 for a group without a number and for fields. */
    public int repeatability() {
      GroupConfig config = element instanceof GroupElement group ? group.getGroup() : null;
      return config != null && config.getRepeatability() != null ? config.getRepeatability() : 1;
    }

    public @Nullable String usageType() {
      GroupConfig config = element instanceof GroupElement group ? group.getGroup() : null;
      return config != null && config.getUsageType() != null && !config.getUsageType().isEmpty() ? config.getUsageType() : null;
    }

    /** A group that repeats: repeatable more than once and not one of the technical repeats (attachment, multi-select). */
    public boolean isRepeated() {
      return isGroup() && repeatability() > 1 && usageType() == null;
    }

    public boolean isMultiSelectGroup() {
      return isGroup() && GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(usageType());
    }

    /** The type of a field with type definitions resolved (e.g. {@code StringType}), null for a group. */
    public @Nullable FieldType fieldType() {
      if (!(element instanceof FieldElement field) || field.getField() == null || field.getField().getFieldType() == null) {
        return null;
      }
      return owner.effectiveFieldType(field.getField().getFieldType());
    }

    /** "/Product/Common/Name": the names from the root down. */
    public String path() {
      return (parent == null ? "" : parent.path()) + "/" + element.getName();
    }
  }

  private final Map<String, Node> byId = new HashMap<>();
  private final List<Node> all = new ArrayList<>();
  private final List<DocumentModel> documentModels;

  /**
   * @param documentModels every Document Model of the project, to resolve the Includes of {@code model} (and of what it
   *                       includes)
   */
  public DocumentStructure(DocumentModel model, List<DocumentModel> documentModels) {
    this.documentModels = documentModels;
    if (model.getContent() == null || model.getContent().getModelRoot() == null
        || model.getContent().getModelRoot().getRootGroups() == null) {
      return;
    }
    ElementIndex owner = new ElementIndex(model, documentModels);
    Set<String> visited = new HashSet<>();
    visited.add(model.getId());
    for (GroupElement rootGroup : model.getContent().getModelRoot().getRootGroups()) {
      expand(rootGroup, null, "", owner, visited);
    }
  }

  public @Nullable Node find(@Nullable String id) {
    return id == null ? null : byId.get(id);
  }

  /** Every group and field, in document order. */
  public List<Node> all() {
    return all;
  }

  private DocumentModel findModel(String id) {
    return documentModels.stream().filter(candidate -> id.equals(candidate.getId())).findFirst().orElse(null);
  }

  private void expand(Element element, Node parent, String prefix, ElementIndex owner, Set<String> visited) {
    if (element.getId() == null || !(element instanceof GroupElement || element instanceof FieldElement)) {
      return;
    }
    Node node = new Node(prefix + element.getId(), element, parent, owner);
    byId.put(node.id, node);
    all.add(node);
    if (parent != null) {
      parent.children.add(node);
    }
    if (!(element instanceof GroupElement group) || group.getGroup() == null) {
      return;
    }
    if (group.getGroup().getIncludeConfig() != null && group.getGroup().getIncludeConfig().getReference() != null) {
      DocumentModel included = findModel(group.getGroup().getIncludeConfig().getReference());
      if (included != null && included.getContent() != null && included.getContent().getModelRoot() != null
          && included.getContent().getModelRoot().getRootGroups() != null && !visited.contains(included.getId())) {
        Set<String> nested = new HashSet<>(visited);
        nested.add(included.getId());
        ElementIndex includedOwner = new ElementIndex(included, documentModels);
        for (GroupElement includedRoot : included.getContent().getModelRoot().getRootGroups()) {
          expand(includedRoot, node, node.id + "_", includedOwner, nested);
        }
      }
    }
    if (group.getGroup().getElements() != null) {
      for (Element child : group.getGroup().getElements()) {
        expand(child, node, prefix, owner, visited);
      }
    }
  }

  /** The groups an element with data context {@code context} may reference, in document order. */
  public List<Node> candidateGroups(@Nullable Node context) {
    return all.stream().filter(node -> isCandidateGroup(context, node)).toList();
  }

  /** The fields an element with data context {@code context} may reference, in document order. */
  public List<Node> candidateFields(@Nullable Node context) {
    return all.stream().filter(node -> isCandidateField(context, node)).toList();
  }

  /**
   * The groups whose index a text may show at a position with data context {@code context}: the context and the groups
   * above it that repeat, outermost first (SME's candidates for the group references inside a text). None without a
   * context.
   */
  public List<Node> candidateIndexGroups(@Nullable Node context) {
    List<Node> result = new ArrayList<>();
    for (Node current = context; current != null; current = current.parent) {
      if (current.isGroup() && current.repeatability() > 1) {
        result.add(0, current);
      }
    }
    return result;
  }

  /**
   * The fields that lie below the topmost group above {@code context} at any depth, repeated groups included (SME's
   * {@code candidateFields} with {@code traverseRepeatableGroups}): what an element may list that only reports about
   * fields, like the Message Group Container. Every field with no context.
   */
  public List<Node> candidateFieldsThroughRepeatedGroups(@Nullable Node context) {
    Node top = context;
    while (top != null && top.parent != null) {
      top = top.parent;
    }
    Node from = top;
    return all.stream().filter(node -> node.isField() && (from == null || isBelow(from, node))).toList();
  }

  private static boolean isBelow(Node ancestor, Node node) {
    for (Node current = node.parent; current != null; current = current.parent) {
      if (current == ancestor) {
        return true;
      }
    }
    return false;
  }

  /** The repeated groups on the way from the root to {@code node} (itself included), outermost first: SME's "granularity". */
  public static List<Node> granularity(@Nullable Node node) {
    List<Node> result = new ArrayList<>();
    for (Node current = node; current != null; current = current.parent) {
      if (current.isRepeated()) {
        result.add(0, current);
      }
    }
    return result;
  }

  /** Whether {@code group} lies below {@code context}, at any depth (below the root when {@code context} is null). */
  public static boolean isCandidateGroup(@Nullable Node context, Node group) {
    if (!group.isGroup()) {
      return false;
    }
    if (context == null) {
      return true;
    }
    for (Node current = group.parent; current != null; current = current.parent) {
      if (current == context) {
        return true;
      }
    }
    return false;
  }

  /**
   * Whether {@code field} can be reached from {@code context} or a group above it by descending through groups that
   * occur once. With no context (the top of the Content Model) the whole tree is searched from its root.
   */
  public static boolean isCandidateField(@Nullable Node context, Node field) {
    if (!field.isField()) {
      return false;
    }
    if (context == null) {
      return onlySingleGroupsBetween(null, field);
    }
    for (Node start = context; start != null; start = start.parent) {
      if (onlySingleGroupsBetween(start, field)) {
        return true;
      }
    }
    return false;
  }

  /** {@code field} is below {@code start} (anywhere when null) and every group in between occurs once. */
  private static boolean onlySingleGroupsBetween(@Nullable Node start, Node field) {
    for (Node current = field.parent; current != null; current = current.parent) {
      if (current == start) {
        return true;
      }
      if (current.repeatability() != 1) {
        return false;
      }
    }
    return start == null;
  }
}
