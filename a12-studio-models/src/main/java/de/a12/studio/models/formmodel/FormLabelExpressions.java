package de.a12.studio.models.formmodel;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Every {@link LocalizedText}-typed field reachable from a Form Model's content (SME's {@code I_Label} mixin,
 * composed into nearly every named element: {@code Screen}/{@code Row}/{@code ScreenElement} title, {@code
 * Control}/{@code ExpressionCell}/{@code RepeatOverviewColumn}/{@code FieldConfigEntry}/{@code
 * GroupConfigEntry}/{@code ButtonStyling} label, the model's own {@link FormModelContent#getSubtitle()}) -
 * found by walking the model reflectively for {@code LocalizedText} fields, the same approach {@link
 * FormStyleReferences} uses for style lists, so a label field added to some element class later is covered
 * without touching this class.
 */
public final class FormLabelExpressions {

  private static final String MODEL_PACKAGE = FormModelContent.class.getPackageName();

  private FormLabelExpressions() {
  }

  /**
   * One {@link LocalizedText}-typed field of the model.
   *
   * @param ownerId   id of the nearest element that has one, for reporting - {@code null} if none is reachable
   *                  (e.g. a model-level field, or a field-/group-configuration entry, which have no id of
   *                  their own)
   * @param ownerName that element's name, or {@code null}
   * @param text      the live value (may be {@code null} if never set)
   */
  public record Usage(String ownerId, String ownerName, LocalizedText text) {
  }

  /** Every reachable {@link LocalizedText} field of the model, including unset ({@code null}) ones. */
  public static List<Usage> usages(FormModelContent content) {
    List<Usage> usages = new ArrayList<>();
    if (content != null) {
      walk(content, null, null, new IdentityHashMap<>(), usages);
    }
    return usages;
  }

  private static void walk(Object node, String ownerId, String ownerName, IdentityHashMap<Object, Boolean> seen,
      List<Usage> out) {
    if (node == null || seen.put(node, Boolean.TRUE) != null) {
      return;
    }
    if (node instanceof Collection<?> collection) {
      for (Object item : collection) {
        walk(item, ownerId, ownerName, seen, out);
      }
      return;
    }
    if (node instanceof Map<?, ?> map) {
      for (Object value : map.values()) {
        walk(value, ownerId, ownerName, seen, out);
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
        if (field.getType() == LocalizedText.class) {
          out.add(new Usage(currentId, currentName, (LocalizedText) read(field, node)));
        }
        else {
          walk(read(field, node), currentId, currentName, seen, out);
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
