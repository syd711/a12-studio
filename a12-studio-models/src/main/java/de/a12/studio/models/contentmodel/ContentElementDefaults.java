package de.a12.studio.models.contentmodel;

import de.a12.studio.models.util.JsonSettings;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * The props SME's element library gives a freshly created element of each type ({@code propertiesCreator}), without
 * the child elements some types also get. Applied additively: {@link #applyMissing} only fills keys the element does
 * not have, so retyping an element never overwrites what the user already set.
 */
public final class ContentElementDefaults {

  public static final String DEFAULT_NAMESPACE = "com.mgmtp.a12.contentengine";

  private static final String LEXICAL_ROOT_TAIL = "\"direction\":\"ltr\",\"format\":\"\",\"indent\":0,\"type\":\"root\",\"version\":1}}";

  private static final Map<String, String> DEFAULTS = Map.ofEntries(
      Map.entry("Box", "{\"style\":{\"display\":\"flex\",\"width\":\"100%\",\"height\":\"fit-content\","
          + "\"flexDirection\":\"column\",\"flexWrap\":\"nowrap\",\"padding\":\"0px\",\"margin\":\"0px\","
          + "\"gap\":\"0px\",\"backgroundRepeat\":\"no-repeat\"}}"),
      Map.entry("GridRow", "{\"layoutConfig\":{\"layout\":{\"lg\":[4,4,4],\"md\":[4,4,4]}}}"),
      Map.entry("Grid", "{\"noGutter\":true}"),
      Map.entry("Paragraph", "{\"style\":{\"width\":\"100%\"},\"tree\":{\"root\":{\"children\":[{\"children\":["
          + "{\"detail\":0,\"format\":0,\"mode\":\"normal\",\"text\":\"Enter some text here...\",\"type\":\"text\","
          + "\"version\":1}],\"direction\":\"ltr\",\"format\":\"\",\"indent\":0,\"type\":\"paragraph\","
          + "\"version\":1}]," + LEXICAL_ROOT_TAIL + ","
          + "\"html\":\"<p class=\\\"editor-paragraph\\\" dir=\\\"ltr\\\"><span>Enter some text here...</span></p>\"}"),
      Map.entry("Heading", "{\"tree\":{\"root\":{\"children\":[{\"children\":[{\"detail\":0,\"format\":0,"
          + "\"mode\":\"normal\",\"style\":\"\",\"text\":\"New Heading\",\"type\":\"text\",\"version\":1}],"
          + "\"direction\":\"ltr\",\"format\":\"\",\"indent\":0,\"type\":\"heading\",\"version\":1,\"tag\":\"h1\"}],"
          + LEXICAL_ROOT_TAIL + ",\"html\":\"<h1><span>New Heading</span></h1>\"}"),
      Map.entry("Button", "{\"label\":\"Button label\"}"),
      Map.entry("Link", "{\"href\":\"https://example.com\",\"label\":\"Example Link\",\"style\":{\"width\":\"fit-content\"}}"),
      Map.entry("Icon", "{\"icon\":{\"name\":\"emoji_emotions\",\"theme\":\"filled\"}}"),
      Map.entry("Image", "{\"style\":{\"margin\":\"0\",\"height\":\"auto\",\"width\":\"auto\"},\"src\":{\"static\":\"\"},\"alt\":\"\"}"),
      Map.entry("Video", "{\"src\":\"\",\"style\":{\"width\":\"640px\",\"height\":\"360px\"}}"),
      Map.entry("MessageBox", "{\"label\":\"This is a message.\",\"focusOnMessage\":false}"),
      Map.entry("Tooltip", "{\"text\":\"This is a hint\",\"type\":\"hint\"}"),
      Map.entry("FieldOutput", "{\"fieldId\":\"\",\"displayOption\":\"label-value\",\"missingValueText\":\"\"}"),
      Map.entry("Group", "{\"groupId\":\"\"}"),
      Map.entry("AddRowAction", "{\"groupId\":\"\"}"),
      Map.entry("Conditional", "{\"conditions\":[]}"),
      Map.entry("MediaQuery", "{\"queries\":[{\"kind\":\"exact\",\"size\":\"md\"}],\"operator\":\"or\"}"),
      Map.entry("InteractiveListItem", "{\"text\":\"List Item\"}"),
      Map.entry("InteractiveList", "{\"style\":{\"width\":\"100%\"}}"),
      Map.entry("OrderedList", "{\"style\":{\"width\":\"100%\"}}"),
      Map.entry("UnorderedList", "{\"style\":{\"width\":\"100%\"}}"));

  private ContentElementDefaults() {
  }

  /** The default props of {@code type} (a fresh mutable tree), empty for types without any. */
  public static @NonNull Map<String, Object> defaultProps(String type) {
    if ("Table".equals(type)) {
      return tableDefaults();
    }
    String json = type == null ? null : DEFAULTS.get(type);
    if (json == null) {
      return new LinkedHashMap<>();
    }
    @SuppressWarnings("unchecked")
    Map<String, Object> props = JsonSettings.objectMapper.readValue(json, LinkedHashMap.class);
    return props;
  }

  /** Adds the default props of the element's type that the element does not have yet (deeply, for nested maps). */
  public static void applyMissing(@NonNull ContentElement element) {
    Map<String, Object> defaults = defaultProps(element.getType());
    if (defaults.isEmpty()) {
      return;
    }
    if (element.getProps() == null) {
      element.setProps(new LinkedHashMap<>());
    }
    mergeMissing(element.getProps(), defaults);
  }

  @SuppressWarnings("unchecked")
  private static void mergeMissing(Map<String, Object> target, Map<String, Object> defaults) {
    defaults.forEach((key, value) -> {
      Object existing = target.get(key);
      if (existing == null) {
        target.put(key, value);
      }
      else if (existing instanceof Map<?, ?> existingMap && value instanceof Map<?, ?> defaultMap) {
        mergeMissing((Map<String, Object>) existingMap, (Map<String, Object>) defaultMap);
      }
    });
  }

  /** A short random element id, like SME's {@code generateUid()}. */
  public static String newId() {
    return UUID.randomUUID().toString().replace("-", "").substring(0, 8);
  }

  private static Map<String, Object> tableDefaults() {
    List<Object> columns = new ArrayList<>();
    for (int i = 0; i < 5; i++) {
      Map<String, Object> column = new LinkedHashMap<>();
      column.put("id", newId());
      if (i == 0) {
        column.put("pinning", "left");
      }
      else if (i == 4) {
        column.put("pinning", "right");
      }
      columns.add(column);
    }
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("columns", columns);
    props.put("enableColumnsResizing", true);
    return props;
  }
}
