package de.a12.studio.models.formmodel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * The style names a Form Model's elements use ({@code style}/{@code headerStyle}: Control, Row, every screen
 * element, repeat and overview column header, button styling, row action ...) and the model-level list of
 * defined styles ({@link FormModelContent#getStyles()}) they refer to - SME's "style presets": a style is
 * picked from that list, and deleting one removes it from every element that uses it (SME docs, refactoring
 * table "Deletion of a Style - Styles in all Elements").
 * <p>
 * The places that hold a style list are found by walking the model reflectively for {@code List<Style>}
 * fields, not by a hand-written traversal per element type, so a style list added to some element class later
 * is covered without touching this class; {@code FormStyleReferencesTest} checks the walk against the JSON of
 * every fixture form model.
 */
public final class FormStyleReferences {

  private static final String MODEL_PACKAGE = FormModelContent.class.getPackageName();

  private FormStyleReferences() {
  }

  /**
   * One style list of an element.
   *
   * @param ownerId   id of the nearest element that has one (a row action or button styling is owned by its
   *                  repeat/button), for reporting
   * @param ownerName that element's name, or {@code null}
   * @param styles    the live list
   */
  public record Usage(String ownerId, String ownerName, List<Style> styles) {
  }

  /** Every element-level style list of the model (not {@link FormModelContent#getStyles()} itself). */
  public static List<Usage> usages(FormModelContent content) {
    List<Usage> usages = new ArrayList<>();
    if (content != null) {
      walk(content, null, null, content.getStyles(), new IdentityHashMap<>(), usages);
    }
    return usages;
  }

  /** The names in the model-level style list (blank ones left out). */
  public static Set<String> definedNames(FormModelContent content) {
    Set<String> names = new LinkedHashSet<>();
    if (content != null) {
      for (Style style : content.getStyles()) {
        if (style.getName() != null && !style.getName().isBlank()) {
          names.add(style.getName());
        }
      }
    }
    return names;
  }

  /**
   * Renames and removes style names on every element in one pass. {@code changes} maps an old name to its
   * new name, or to {@code null} to remove it from the elements that use it. One pass matters: with A->B and
   * B->C applied one after the other, what used to be A would end up as C.
   *
   * @return how many style entries were changed or removed
   */
  public static int apply(FormModelContent content, Map<String, String> changes) {
    int changed = 0;
    for (Usage usage : usages(content)) {
      List<Style> rewritten = new ArrayList<>();
      boolean touched = false;
      for (Style style : usage.styles()) {
        if (style.getName() != null && changes.containsKey(style.getName())) {
          touched = true;
          changed++;
          String newName = changes.get(style.getName());
          if (newName != null) {
            style.setName(newName);
            rewritten.add(style);
          }
        }
        else {
          rewritten.add(style);
        }
      }
      if (touched) {
        // In place: the list itself may be held by an open editor.
        usage.styles().clear();
        usage.styles().addAll(rewritten);
      }
    }
    return changed;
  }

  /**
   * The refactoring behind editing the model-level style list (SME: deleting a style removes it from every
   * element, renaming one updates them): compares {@code originalNames} - the name each live {@link Style} of
   * {@code content.getStyles()} had before the edit, by identity - with the list as it is now. A style that
   * is gone is removed from the elements, a renamed one renamed on them (or removed if it now has no name).
   * A name that some entry still carries unchanged, or that a new entry took (a duplicate; deleted and added
   * again), keeps its references. Everything is applied in one pass, so a swap of two names or a chain (a to
   * b while b to c) doesn't collapse them.
   *
   * @return how many style entries on elements were changed or removed
   */
  public static int applyPresetEdits(FormModelContent content, Map<Style, String> originalNames) {
    Set<String> keptNames = new HashSet<>();
    for (Style style : content.getStyles()) {
      if (!originalNames.containsKey(style) || Objects.equals(originalNames.get(style), style.getName())) {
        keptNames.add(style.getName());
      }
    }
    Map<String, String> changes = new LinkedHashMap<>();
    for (Map.Entry<Style, String> original : originalNames.entrySet()) {
      String oldName = original.getValue();
      if (oldName == null || oldName.isBlank() || keptNames.contains(oldName)) {
        continue;
      }
      boolean deleted = content.getStyles().stream().noneMatch(style -> style == original.getKey());
      String newName = deleted ? null : original.getKey().getName();
      changes.put(oldName, newName == null || newName.isBlank() ? null : newName);
    }
    return changes.isEmpty() ? 0 : apply(content, changes);
  }

  /** Removes {@code name} from every element that uses it. */
  public static int remove(FormModelContent content, String name) {
    return apply(content, Collections.singletonMap(name, null));
  }

  /** Renames {@code oldName} to {@code newName} on every element that uses it. */
  public static int rename(FormModelContent content, String oldName, String newName) {
    return apply(content, Collections.singletonMap(oldName, newName));
  }

  private static void walk(Object node, String ownerId, String ownerName, List<Style> skip,
      IdentityHashMap<Object, Boolean> seen, List<Usage> out) {
    if (node == null || seen.put(node, Boolean.TRUE) != null) {
      return;
    }
    if (node instanceof Collection<?> collection) {
      for (Object item : collection) {
        walk(item, ownerId, ownerName, skip, seen, out);
      }
      return;
    }
    if (node instanceof Map<?, ?> map) {
      for (Object value : map.values()) {
        walk(value, ownerId, ownerName, skip, seen, out);
      }
      return;
    }
    if (node instanceof Enum<?> || !isModelType(node.getClass())) {
      return;
    }
    String id = stringProperty(node, "getId");
    String currentId = id != null ? id : ownerId;
    String currentName = id != null ? stringProperty(node, "getName") : ownerName;
    for (Class<?> type = node.getClass(); type != null && isModelType(type); type = type.getSuperclass()) {
      for (Field field : type.getDeclaredFields()) {
        if (Modifier.isStatic(field.getModifiers())) {
          continue;
        }
        Object value = read(field, node);
        if (value == null) {
          continue;
        }
        if (isStyleList(field)) {
          if (value != skip) {
            @SuppressWarnings("unchecked")
            List<Style> styles = (List<Style>) value;
            out.add(new Usage(currentId, currentName, styles));
          }
        }
        else {
          walk(value, currentId, currentName, skip, seen, out);
        }
      }
    }
  }

  // Only the model's own classes are opened up; JDK types (enums, collections ...) are not reflectable.
  private static boolean isModelType(Class<?> type) {
    return type.getName().startsWith(MODEL_PACKAGE);
  }

  private static boolean isStyleList(Field field) {
    Type type = field.getGenericType();
    return type instanceof ParameterizedType parameterized
        && parameterized.getRawType() == List.class
        && parameterized.getActualTypeArguments().length == 1
        && parameterized.getActualTypeArguments()[0] == Style.class;
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

  private static String stringProperty(Object node, String getter) {
    try {
      Method method = node.getClass().getMethod(getter);
      return method.invoke(node) instanceof String value ? value : null;
    }
    catch (NoSuchMethodException e) {
      return null;
    }
    catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot call " + getter + " on " + node.getClass(), e);
    }
  }
}
