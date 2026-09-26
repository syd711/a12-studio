package de.a12.studio.modelsvalidation.validators.content;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.documentmodel.FieldElement;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.documentmodel.GroupConfig;
import de.a12.studio.models.documentmodel.GroupElement;
import de.a12.studio.modelsvalidation.ValidationContext;
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
 * prefixes). What a Content Model may reference, and from where, is decided on this tree:
 * <ul>
 *   <li>a group is a candidate when it lies below the <em>data context</em> of the referencing element - the base group
 *       or the closest enclosing Repeatable Group, or anywhere at the top (SME's {@code candidateGroups});</li>
 *   <li>a field is a candidate when it can be reached from the data context or one of the groups above it by
 *       descending through groups that are not repeated (SME's {@code candidateFields}).</li>
 * </ul>
 * Rules and computations are not part of it, nobody can reference them.
 */
final class DocumentStructure {

  /** A group or field of the expanded Document Model. */
  static final class Node {

    final String id;
    final Element element;
    final Node parent;
    final List<Node> children = new ArrayList<>();
    // The index of the model the element is defined in, which knows its type definitions.
    private final ElementIndex owner;

    private Node(String id, Element element, Node parent, ElementIndex owner) {
      this.id = id;
      this.element = element;
      this.parent = parent;
      this.owner = owner;
    }

    boolean isGroup() {
      return element instanceof GroupElement;
    }

    boolean isField() {
      return element instanceof FieldElement;
    }

    /** How often the group may occur; 1 for a group without a number and for fields. */
    int repeatability() {
      GroupConfig config = element instanceof GroupElement group ? group.getGroup() : null;
      return config != null && config.getRepeatability() != null ? config.getRepeatability() : 1;
    }

    @Nullable String usageType() {
      GroupConfig config = element instanceof GroupElement group ? group.getGroup() : null;
      return config != null && config.getUsageType() != null && !config.getUsageType().isEmpty() ? config.getUsageType() : null;
    }

    /** A group that repeats: repeatable more than once and not one of the technical repeats (attachment, multi-select). */
    boolean isRepeated() {
      return isGroup() && repeatability() > 1 && usageType() == null;
    }

    boolean isMultiSelectGroup() {
      return isGroup() && GroupConfig.USAGE_TYPE_MULTI_SELECT.equals(usageType());
    }

    /** The type of a field with type definitions resolved (e.g. {@code StringType}), null for a group. */
    @Nullable FieldType fieldType() {
      if (!(element instanceof FieldElement field) || field.getField() == null || field.getField().getFieldType() == null) {
        return null;
      }
      return owner.effectiveFieldType(field.getField().getFieldType());
    }

    /** "/Product/Common/Name": the names from the root down. */
    String path() {
      return (parent == null ? "" : parent.path()) + "/" + element.getName();
    }
  }

  private final Map<String, Node> byId = new HashMap<>();
  private final ValidationContext context;

  DocumentStructure(DocumentModel model, ValidationContext context) {
    this.context = context;
    if (model.getContent() == null || model.getContent().getModelRoot() == null
        || model.getContent().getModelRoot().getRootGroups() == null) {
      return;
    }
    ElementIndex owner = new ElementIndex(model, context.otherDocumentModels());
    Set<String> visited = new HashSet<>();
    visited.add(model.getId());
    for (GroupElement rootGroup : model.getContent().getModelRoot().getRootGroups()) {
      expand(rootGroup, null, "", owner, visited);
    }
  }

  @Nullable Node find(@Nullable String id) {
    return id == null ? null : byId.get(id);
  }

  private void expand(Element element, Node parent, String prefix, ElementIndex owner, Set<String> visited) {
    if (element.getId() == null || !(element instanceof GroupElement || element instanceof FieldElement)) {
      return;
    }
    Node node = new Node(prefix + element.getId(), element, parent, owner);
    byId.put(node.id, node);
    if (parent != null) {
      parent.children.add(node);
    }
    if (!(element instanceof GroupElement group) || group.getGroup() == null) {
      return;
    }
    if (group.getGroup().getIncludeConfig() != null && group.getGroup().getIncludeConfig().getReference() != null) {
      DocumentModel included = context.findOtherDocumentModel(group.getGroup().getIncludeConfig().getReference());
      if (included != null && included.getContent() != null && included.getContent().getModelRoot() != null
          && included.getContent().getModelRoot().getRootGroups() != null && !visited.contains(included.getId())) {
        Set<String> nested = new HashSet<>(visited);
        nested.add(included.getId());
        ElementIndex includedOwner = new ElementIndex(included, context.otherDocumentModels());
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

  /** The repeated groups on the way from the root to {@code node} (itself included), outermost first: SME's "granularity". */
  static List<Node> granularity(@Nullable Node node) {
    List<Node> result = new ArrayList<>();
    for (Node current = node; current != null; current = current.parent) {
      if (current.isRepeated()) {
        result.add(0, current);
      }
    }
    return result;
  }

  /** Whether {@code group} lies below {@code context}, at any depth (below the root when {@code context} is null). */
  static boolean isCandidateGroup(@Nullable Node context, Node group) {
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
  static boolean isCandidateField(@Nullable Node context, Node field) {
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
