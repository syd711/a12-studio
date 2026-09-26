package de.a12.studio.models.contentmodel;

import de.a12.studio.models.contentmodel.ContentRuleEvaluator.Node;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Which element types may be inserted next to or into an element: SME's {@code insertableNodeTypes}. A type is
 * offered when the type's parent rule accepts the parent it would get and the parent's child rule accepts the list of
 * children the parent would have afterwards (order and instance limits included, so a Table that already has its head,
 * body and foot takes nothing more).
 * <p>
 * Repeatable Groups and Conditionals are looked through: their children count as children of the group's own parent,
 * and an element inserted next to a group's child gets that parent, not the group. The only exception is the table body,
 * whose child rule talks about the Group itself ({@link ContentModule#keepTransitiveChildren()}).
 */
public final class ContentInsertion {

  public enum Position {
    /** As the last child of the target. */
    AS_CHILD,
    /** Right before the target. */
    ABOVE,
    /** Right after the target. */
    BELOW
  }

  private ContentInsertion() {
  }

  /**
   * The types that may be inserted at {@code position} relative to {@code target}, in the order the insert dialog lists
   * them ({@link ContentElementLibrary#displayOrder()}). Empty when the target is not part of the tree below
   * {@code root}, or its parent is of a type this library does not know.
   */
  public static @NonNull List<ContentModule> insertableModules(@NonNull ContentElement root, @NonNull ContentElement target,
      @NonNull Position position) {
    List<ContentElement> path = pathTo(root, target);
    if (path.isEmpty() || (target == root && position != Position.AS_CHILD)) {
      return List.of();
    }
    ContentElement parent = parentForRules(path, position);
    Optional<ContentModule> parentModule = ContentElementLibrary.find(parent);
    if (parentModule.isEmpty()) {
      return List.of();
    }
    boolean keep = parentModule.get().keepTransitiveChildren();
    List<ContentModule> result = new ArrayList<>();
    for (ContentModule candidate : ContentElementLibrary.modules()) {
      if (!candidate.parentRule().matches(parentModule.get().id())) {
        continue;
      }
      List<Node> children = childrenWithInsertion(parent, keep, target, position, candidate.id());
      if (ContentRuleEvaluator.matches(children, parentModule.get().childRule())) {
        result.add(candidate);
      }
    }
    result.sort(ContentElementLibrary.displayOrder());
    return result;
  }

  public static boolean canInsert(@NonNull ContentElement root, @NonNull ContentElement target, @NonNull Position position) {
    return !insertableModules(root, target, position).isEmpty();
  }

  /** The closest {@code Table} at or above {@code element}, or null. */
  public static ContentElement closestTable(@NonNull ContentElement root, @NonNull ContentElement element) {
    List<ContentElement> path = pathTo(root, element);
    for (int i = path.size() - 1; i >= 0; i--) {
      ContentElement candidate = path.get(i);
      if ("Table".equals(candidate.getType()) && ContentElementLibrary.NAMESPACE.equals(namespaceOf(candidate))) {
        return candidate;
      }
    }
    return null;
  }

  /** The element the rules see as the parent: the target itself for a child, else its parent, without any group. */
  private static ContentElement parentForRules(List<ContentElement> path, Position position) {
    int index = path.size() - 1;
    if (position != Position.AS_CHILD || ContentElementLibrary.isTransitive(path.get(index))) {
      index--;
    }
    while (index >= 0 && ContentElementLibrary.isTransitive(path.get(index))) {
      index--;
    }
    if (index >= 0) {
      return path.get(index);
    }
    // Above the root there is nothing; SME assumes a Box there.
    ContentElement box = new ContentElement();
    box.setNamespace(ContentElementLibrary.NAMESPACE);
    box.setType("Box");
    box.setChildren(new ArrayList<>(List.of(path.get(0))));
    return box;
  }

  /** The children {@code parent} would have with the new element in place, reduced to module ids. */
  private static List<Node> childrenWithInsertion(ContentElement parent, boolean keepTransitive, ContentElement target,
      Position position, String newModuleId) {
    List<Node> result = new ArrayList<>();
    if (parent.getChildren() != null) {
      for (ContentElement child : parent.getChildren()) {
        if (position == Position.ABOVE && child == target) {
          result.add(new Node(newModuleId, List.of()));
        }
        if (ContentElementLibrary.isTransitive(child) && !keepTransitive) {
          result.addAll(childrenWithInsertion(child, false, target, position, newModuleId));
        }
        else {
          result.add(new Node(moduleId(child), childrenWithInsertion(child, keepTransitive, target, position, newModuleId)));
        }
        if (position == Position.BELOW && child == target) {
          result.add(new Node(newModuleId, List.of()));
        }
      }
    }
    if (position == Position.AS_CHILD && parent == target) {
      result.add(new Node(newModuleId, List.of()));
    }
    return result;
  }

  private static String moduleId(ContentElement element) {
    return ContentModule.moduleId(namespaceOf(element), String.valueOf(element.getType()));
  }

  private static String namespaceOf(ContentElement element) {
    return element.getNamespace() != null ? element.getNamespace() : ContentElementLibrary.NAMESPACE;
  }

  /** The elements from {@code root} down to {@code target}, empty when the target is not below the root. */
  private static List<ContentElement> pathTo(ContentElement root, ContentElement target) {
    List<ContentElement> path = new ArrayList<>();
    return collectPath(root, target, path) ? path : List.of();
  }

  private static boolean collectPath(ContentElement node, ContentElement target, List<ContentElement> path) {
    path.add(node);
    if (node == target) {
      return true;
    }
    if (node.getChildren() != null) {
      for (ContentElement child : node.getChildren()) {
        if (collectPath(child, target, path)) {
          return true;
        }
      }
    }
    path.remove(path.size() - 1);
    return false;
  }
}
