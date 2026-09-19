package de.a12.studio.models.formmodel;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Finds every object of a given type anywhere in a Form Model's content - Controls (which live in the rows of
 * control grids in sections, embedded repeats and detail screens), overview columns, buttons ... - without a
 * hand-written traversal per container type, so a container added later is covered automatically. The walk is
 * reflective over the model's own classes and their collections, like {@link FormStyleReferences}; its
 * completeness is checked against fixtures in {@code FormModelWalkerTest}.
 */
public final class FormModelWalker {

  private static final String MODEL_PACKAGE = FormModelContent.class.getPackageName();

  private FormModelWalker() {
  }

  /** All instances of {@code type} reachable from {@code content}, in document order. */
  public static <T> List<T> find(FormModelContent content, Class<T> type) {
    return find(content, type, node -> true);
  }

  /**
   * All instances of {@code type} reachable from {@code root} (any model object, e.g. a single {@link Screen}),
   * in document order, without looking inside the nodes {@code descend} rejects - a rejected node is still
   * reported itself when it is an instance of {@code type}.
   */
  public static <T> List<T> find(Object root, Class<T> type, Predicate<Object> descend) {
    List<T> found = new ArrayList<>();
    if (root != null) {
      walk(root, type, descend, new IdentityHashMap<>(), found);
    }
    return found;
  }

  private static <T> void walk(Object node, Class<T> type, Predicate<Object> descend,
      IdentityHashMap<Object, Boolean> seen, List<T> found) {
    if (node == null || seen.put(node, Boolean.TRUE) != null) {
      return;
    }
    if (node instanceof Collection<?> collection) {
      for (Object item : collection) {
        walk(item, type, descend, seen, found);
      }
      return;
    }
    if (node instanceof Map<?, ?> map) {
      for (Object value : map.values()) {
        walk(value, type, descend, seen, found);
      }
      return;
    }
    if (node instanceof Enum<?> || !isModelType(node.getClass())) {
      return;
    }
    if (type.isInstance(node)) {
      found.add(type.cast(node));
    }
    if (!descend.test(node)) {
      return;
    }
    for (Class<?> current = node.getClass(); current != null && isModelType(current); current = current.getSuperclass()) {
      for (Field field : current.getDeclaredFields()) {
        if (!Modifier.isStatic(field.getModifiers())) {
          walk(read(field, node), type, descend, seen, found);
        }
      }
    }
  }

  // Only the model's own classes are opened up; JDK types (enums, collections ...) are not reflectable.
  private static boolean isModelType(Class<?> type) {
    return type.getName().startsWith(MODEL_PACKAGE);
  }

  private static Object read(Field field, Object target) {
    try {
      field.setAccessible(true);
      return field.get(target);
    }
    catch (ReflectiveOperationException | RuntimeException e) {
      throw new IllegalStateException("Cannot read " + field, e);
    }
  }
}
