package de.a12.studio.models.contentmodel;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Typed access to the {@code props} of one {@link ContentElement} by dotted path ({@code "style.width"},
 * {@code "icons.collapsedIcon"}), the addressing SME's setting panel uses too. Writing {@code null} removes the key
 * and prunes maps that the removal leaves empty, so a setting reset to "unspecified" leaves no trace in the file
 * (SME's formatters do the same: unspecified keywords and {@code false} flags are omitted, not written).
 *
 * <p>Everything else in {@code props} (the Lexical {@code tree}/{@code html} pair, unknown keys) is never touched.
 */
public final class ContentProps {

  private final ContentElement element;

  public ContentProps(@NonNull ContentElement element) {
    this.element = element;
  }

  public @NonNull ContentElement getElement() {
    return element;
  }

  /** The raw value at {@code path}, or {@code null} when any segment is absent (or not a map on the way). */
  public @Nullable Object get(@NonNull String path) {
    Object current = element.getProps();
    for (String segment : segments(path)) {
      if (!(current instanceof Map<?, ?> map)) {
        return null;
      }
      current = map.get(segment);
    }
    return current;
  }

  /** The value at {@code path} as text; numbers and booleans are rendered, maps and lists count as absent. */
  public @Nullable String getString(@NonNull String path) {
    Object value = get(path);
    if (value instanceof String || value instanceof Number || value instanceof Boolean) {
      return String.valueOf(value);
    }
    return null;
  }

  public boolean getBoolean(@NonNull String path, boolean defaultValue) {
    Object value = get(path);
    if (value instanceof Boolean bool) {
      return bool;
    }
    if (value instanceof String text && ("true".equals(text) || "false".equals(text))) {
      return Boolean.parseBoolean(text);
    }
    return defaultValue;
  }

  /** The map at {@code path} (a live view of the model), or {@code null} when absent or not a map. */
  @SuppressWarnings("unchecked")
  public @Nullable Map<String, Object> getMap(@NonNull String path) {
    return get(path) instanceof Map<?, ?> map ? (Map<String, Object>) map : null;
  }

  /**
   * Writes {@code value} at {@code path}, creating the {@code props} map and any intermediate maps. A {@code null}
   * value removes the key like {@link #remove}. An intermediate value that is not a map is replaced by one, since no
   * typed setting could have produced it.
   */
  @SuppressWarnings("unchecked")
  public void set(@NonNull String path, @Nullable Object value) {
    if (value == null) {
      remove(path);
      return;
    }
    if (element.getProps() == null) {
      element.setProps(new LinkedHashMap<>());
    }
    String[] segments = segments(path);
    Map<String, Object> current = element.getProps();
    for (int i = 0; i < segments.length - 1; i++) {
      Object next = current.get(segments[i]);
      if (!(next instanceof Map<?, ?>)) {
        next = new LinkedHashMap<String, Object>();
        current.put(segments[i], next);
      }
      current = (Map<String, Object>) next;
    }
    current.put(segments[segments.length - 1], value);
  }

  /** Removes the key at {@code path} and every parent map (below {@code props} itself) that became empty. */
  public void remove(@NonNull String path) {
    Map<String, Object> props = element.getProps();
    if (props != null) {
      removeFrom(props, segments(path), 0);
    }
  }

  private static boolean removeFrom(Map<String, Object> map, String[] segments, int index) {
    String key = segments[index];
    if (index == segments.length - 1) {
      map.remove(key);
    }
    else if (map.get(key) instanceof Map<?, ?> child) {
      @SuppressWarnings("unchecked")
      Map<String, Object> childMap = (Map<String, Object>) child;
      if (removeFrom(childMap, segments, index + 1)) {
        map.remove(key);
      }
    }
    return map.isEmpty();
  }

  /**
   * Writes a flag the way SME does: {@code true} is stored, {@code false} is stored only when the setting's default
   * is {@code true} (otherwise a missing key already means {@code false}).
   */
  public void setFlag(@NonNull String path, boolean value, boolean defaultValue) {
    if (value || defaultValue) {
      set(path, value);
    }
    else {
      remove(path);
    }
  }

  private static String[] segments(String path) {
    return path.split("\\.");
  }
}
