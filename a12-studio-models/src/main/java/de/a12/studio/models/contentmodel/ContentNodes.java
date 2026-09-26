package de.a12.studio.models.contentmodel;

import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The parts of an element that are elements or element references themselves but do not sit in its {@code children}:
 * the event nodes of its click settings ({@code onClick}, a table's {@code onRowClick}: a nested Save, Commit, Cancel,
 * Add Row or Delete Row action) and the field/group references inside the Lexical tree of its text.
 */
public final class ContentNodes {

  /** The props that can hold an event node instead of an event name. */
  public static final List<String> EVENT_KEYS = List.of("onClick", "onRowClick");

  /** The type of the Lexical node SME's editor inserts for a field or group reference inside a text. */
  public static final String LEXICAL_FIELD_REFERENCE = "ce-field-reference";

  /**
   * A field ({@code group == false}) or group reference inside a text.
   *
   * @param fieldPath the path the reference showed when it was inserted, for messages
   */
  public record LexicalReference(String fieldId, String fieldPath, boolean group) {
  }

  /** An event node found in an element's props, with the key it is stored under. */
  public record EventNode(String key, ContentElement node) {
  }

  private ContentNodes() {
  }

  /** The event nodes among {@code element}'s click settings; event names (plain strings) are not nodes. */
  public static @NonNull List<EventNode> eventNodes(@NonNull ContentElement element) {
    List<EventNode> result = new ArrayList<>();
    if (element.getProps() == null) {
      return result;
    }
    for (String key : EVENT_KEYS) {
      if (element.getProps().get(key) instanceof Map<?, ?> map) {
        result.add(new EventNode(key, toElement(map)));
      }
    }
    return result;
  }

  /** Every field or group reference inside the Lexical tree of {@code element}'s text, in document order. */
  public static @NonNull List<LexicalReference> lexicalReferences(@NonNull ContentElement element) {
    List<LexicalReference> result = new ArrayList<>();
    if (element.getProps() != null && element.getProps().get("tree") instanceof Map<?, ?> tree) {
      collectReferences(tree.get("root"), result);
    }
    return result;
  }

  private static void collectReferences(Object node, List<LexicalReference> result) {
    if (!(node instanceof Map<?, ?> map)) {
      return;
    }
    if (LEXICAL_FIELD_REFERENCE.equals(map.get("type"))) {
      result.add(new LexicalReference(map.get("fieldId") instanceof String id ? id : null,
          map.get("fieldPath") instanceof String path ? path : null, Boolean.TRUE.equals(map.get("isGroup"))));
    }
    if (map.get("children") instanceof List<?> children) {
      children.forEach(child -> collectReferences(child, result));
    }
  }

  @SuppressWarnings("unchecked")
  private static ContentElement toElement(Map<?, ?> map) {
    ContentElement element = new ContentElement();
    element.setId(map.get("id") instanceof String id ? id : null);
    element.setType(map.get("type") instanceof String type ? type : null);
    element.setNamespace(map.get("namespace") instanceof String namespace ? namespace : null);
    if (map.get("props") instanceof Map<?, ?> props) {
      element.setProps((Map<String, Object>) props);
    }
    return element;
  }
}
