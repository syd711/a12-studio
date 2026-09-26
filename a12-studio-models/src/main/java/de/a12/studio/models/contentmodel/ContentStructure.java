package de.a12.studio.models.contentmodel;

import de.a12.studio.models.contentmodel.ContentInsertion.Position;
import de.a12.studio.models.contentmodel.ContentRuleEvaluator.Node;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Whether an element tree keeps to the parent and child rules of its element types ({@link ContentModule}), and which
 * edits keep it that way: the checks SME's editor makes before it enables Move, Cut, Duplicate or Paste ({@code
 * isMovable}, {@code isCuttable}, {@code isDuplicable}, {@code isPastable}). {@link ContentInsertion} answers the same
 * question for adding a new element; this class answers it for every other change of the tree.
 * <p>
 * An edit is allowed when it introduces no violation that was not there before: a tree that already breaks a rule (a
 * hand-edited file) can still be edited elsewhere, only the edit that makes it worse is refused. The tree is not
 * touched; every check works on a copy of its shape that knows the element types only. Repeatable Groups and
 * Conditionals are looked through like {@link ContentInsertion} does, elements of types the library does not know
 * (plugin libraries) are not checked, and neither are the children of table rows (see {@link #ROWS}).
 */
public final class ContentStructure {

  public enum Kind {
    /** The element's type is not allowed inside the element it sits in. */
    PARENT_NOT_ALLOWED,
    /** The element's children do not match the child rule of its type (wrong types, order or number). */
    CHILDREN_NOT_ALLOWED
  }

  /**
   * A broken rule.
   *
   * @param parentType the type of the parent the rules see (groups looked through) for {@link Kind#PARENT_NOT_ALLOWED}
   * @param childTypes the types of the element's children for {@link Kind#CHILDREN_NOT_ALLOWED}
   */
  public record Violation(@NonNull ContentElement element, @NonNull Kind kind, @Nullable String parentType,
                          @NonNull List<String> childTypes) {
  }

  // SME declares the three table row types as childless (their rule is the same as a Heading's), which keeps "Add child"
  // from offering anything on a row; their cells are created and kept in line with the table's columns by the editor
  // itself. Real tables have cells in their rows, so the rule is not applied to a row's children.
  private static final Set<String> ROWS = Set.of(
      ContentModule.moduleId(ContentElementLibrary.NAMESPACE, "TableHeadRow"),
      ContentModule.moduleId(ContentElementLibrary.NAMESPACE, "TableBodyRow"),
      ContentModule.moduleId(ContentElementLibrary.NAMESPACE, "TableFootRow"));

  private ContentStructure() {
  }

  /** Every rule the tree below {@code root} breaks, parents before children. */
  public static @NonNull List<Violation> violations(@NonNull ContentElement root) {
    return check(mirror(root, null));
  }

  /** Whether {@code element} can be removed (deleted or cut) without breaking a rule that held before. */
  public static boolean canRemove(@NonNull ContentElement root, @NonNull ContentElement element) {
    Shape shape = new Shape(root);
    Shape.Item item = shape.find(element);
    if (item == null || item.parent == null) {
      return false;
    }
    item.parent.children.remove(item);
    return shape.isNotWorse();
  }

  /** Whether {@code element} can move one place up ({@code delta < 0}) or down ({@code delta > 0}) among its siblings. */
  public static boolean canMove(@NonNull ContentElement root, @NonNull ContentElement element, int delta) {
    Shape shape = new Shape(root);
    Shape.Item item = shape.find(element);
    if (item == null || item.parent == null || delta == 0) {
      return false;
    }
    int from = item.parent.children.indexOf(item);
    int to = from + (delta < 0 ? -1 : 1);
    if (to < 0 || to >= item.parent.children.size()) {
      return false;
    }
    java.util.Collections.swap(item.parent.children, from, to);
    return shape.isNotWorse();
  }

  /** Whether a copy of {@code element} can be inserted right after it. */
  public static boolean canDuplicate(@NonNull ContentElement root, @NonNull ContentElement element) {
    Shape shape = new Shape(root);
    Shape.Item item = shape.find(element);
    if (item == null || item.parent == null) {
      return false;
    }
    item.parent.children.add(item.parent.children.indexOf(item) + 1, item.copy(item.parent));
    return shape.isNotWorse();
  }

  /**
   * Whether {@code source} (an element that is not in the tree, e.g. the clone made from the clipboard) can be pasted
   * at {@code position} relative to {@code target}.
   */
  public static boolean canPaste(@NonNull ContentElement root, @NonNull ContentElement source, @NonNull ContentElement target,
      @NonNull Position position) {
    Shape shape = new Shape(root);
    Shape.Item targetItem = shape.find(target);
    if (targetItem == null) {
      return false;
    }
    return shape.insert(mirror(source, null), targetItem, position) && shape.isNotWorse();
  }

  /** Whether {@code element}, with everything below it, can be moved to {@code position} relative to {@code target}. */
  public static boolean canRelocate(@NonNull ContentElement root, @NonNull ContentElement element, @NonNull ContentElement target,
      @NonNull Position position) {
    Shape shape = new Shape(root);
    Shape.Item item = shape.find(element);
    Shape.Item targetItem = shape.find(target);
    if (item == null || targetItem == null || item.parent == null || item == targetItem || item.contains(targetItem)) {
      return false;
    }
    item.parent.children.remove(item);
    return shape.insert(item, targetItem, position) && shape.isNotWorse();
  }

  // ---- the shape ----

  /** The tree as it is and as an edit would leave it, to compare the violations of both. */
  private static final class Shape {

    private final Item root;
    private final Set<Key> before;

    Shape(ContentElement rootElement) {
      this.root = mirror(rootElement, null);
      this.before = keys(check(root));
    }

    Item find(ContentElement element) {
      return find(root, element);
    }

    private static Item find(Item item, ContentElement element) {
      if (item.element == element) {
        return item;
      }
      for (Item child : item.children) {
        Item found = find(child, element);
        if (found != null) {
          return found;
        }
      }
      return null;
    }

    /** Puts {@code item} at {@code position} relative to {@code target}; false if there is no such place (above the root). */
    boolean insert(Item item, Item target, Position position) {
      if (position == Position.AS_CHILD) {
        item.parent = target;
        target.children.add(item);
        return true;
      }
      if (target.parent == null) {
        return false;
      }
      item.parent = target.parent;
      int index = target.parent.children.indexOf(target);
      target.parent.children.add(position == Position.ABOVE ? index : index + 1, item);
      return true;
    }

    boolean isNotWorse() {
      return before.containsAll(keys(check(root)));
    }

    /** An element of the tree reduced to what the rules look at. */
    private static final class Item {

      final ContentElement element;
      final ContentModule module;
      final String moduleId;
      final boolean transitive;
      Item parent;
      final List<Item> children = new ArrayList<>();

      Item(ContentElement element, Item parent) {
        this.element = element;
        this.parent = parent;
        this.module = ContentElementLibrary.find(element).orElse(null);
        this.moduleId = ContentModule.moduleId(namespaceOf(element), String.valueOf(element.getType()));
        this.transitive = ContentElementLibrary.isTransitive(element);
      }

      Item copy(Item newParent) {
        Item copy = new Item(element, newParent);
        children.forEach(child -> copy.children.add(child.copy(copy)));
        return copy;
      }

      boolean contains(Item other) {
        for (Item item = other; item != null; item = item.parent) {
          if (item == this) {
            return true;
          }
        }
        return false;
      }
    }
  }

  private record Key(ContentElement element, Kind kind) {
  }

  private static Set<Key> keys(List<Violation> violations) {
    Set<Key> keys = new HashSet<>();
    violations.forEach(violation -> keys.add(new Key(violation.element(), violation.kind())));
    return keys;
  }

  private static Shape.Item mirror(ContentElement element, Shape.Item parent) {
    Shape.Item item = new Shape.Item(element, parent);
    if (element.getChildren() != null) {
      element.getChildren().forEach(child -> item.children.add(mirror(child, item)));
    }
    return item;
  }

  // ---- the rules ----

  private static List<Violation> check(Shape.Item root) {
    List<Violation> result = new ArrayList<>();
    check(root, result);
    return result;
  }

  private static void check(Shape.Item item, List<Violation> result) {
    if (item.module != null) {
      Shape.Item parent = effectiveParent(item);
      if (parent != null && !item.module.parentRule().matches(parent.moduleId)) {
        result.add(new Violation(item.element, Kind.PARENT_NOT_ALLOWED, String.valueOf(parent.element.getType()), List.of()));
      }
      if (!item.transitive && !ROWS.contains(item.moduleId)
          && !ContentRuleEvaluator.matches(nodes(item, item.module.keepTransitiveChildren()), item.module.childRule())) {
        result.add(new Violation(item.element, Kind.CHILDREN_NOT_ALLOWED, null,
            item.children.stream().map(child -> String.valueOf(child.element.getType())).toList()));
      }
    }
    item.children.forEach(child -> check(child, result));
  }

  /** The parent the rules see: the closest ancestor that is not a Repeatable Group or Conditional. */
  private static Shape.Item effectiveParent(Shape.Item item) {
    Shape.Item parent = item.parent;
    while (parent != null && parent.transitive) {
      parent = parent.parent;
    }
    return parent;
  }

  /** The children of {@code parent} as the rules see them, see {@link ContentInsertion}. */
  private static List<Node> nodes(Shape.Item parent, boolean keepTransitive) {
    List<Node> result = new ArrayList<>();
    for (Shape.Item child : parent.children) {
      if (child.transitive && !keepTransitive) {
        result.addAll(nodes(child, false));
      }
      else {
        result.add(new Node(child.moduleId, nodes(child, keepTransitive)));
      }
    }
    return result;
  }

  private static String namespaceOf(ContentElement element) {
    return element.getNamespace() != null ? element.getNamespace() : ContentElementLibrary.NAMESPACE;
  }
}
