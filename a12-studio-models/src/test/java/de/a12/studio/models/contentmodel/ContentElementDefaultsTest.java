package de.a12.studio.models.contentmodel;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentElementDefaultsTest {

  private static ContentElement element(String type) {
    ContentElement element = new ContentElement();
    element.setType(type);
    return element;
  }

  @Test
  void aNewBoxGetsSmesBoxStyle() {
    ContentElement box = element("Box");

    ContentElementDefaults.applyMissing(box);

    ContentProps props = new ContentProps(box);
    assertEquals("flex", props.getString("style.display"));
    assertEquals("column", props.getString("style.flexDirection"));
    assertEquals("fit-content", props.getString("style.height"));
  }

  @Test
  void retypingKeepsExistingValuesAndAddsOnlyMissingOnes() {
    ContentElement image = element("Image");
    Map<String, Object> style = new LinkedHashMap<>();
    style.put("width", "50%");
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("style", style);
    image.setProps(props);

    ContentElementDefaults.applyMissing(image);

    ContentProps result = new ContentProps(image);
    assertEquals("50%", result.getString("style.width"));
    assertEquals("auto", result.getString("style.height"));
    assertEquals("", result.getString("src.static"));
    assertEquals("", result.getString("alt"));
  }

  @Test
  void paragraphsAndHeadingsGetALexicalPayload() {
    for (String type : List.of("Paragraph", "Heading")) {
      ContentElement element = element(type);
      ContentElementDefaults.applyMissing(element);
      assertNotNull(element.getProps().get("tree"), type);
      assertTrue(String.valueOf(element.getProps().get("html")).contains("<span>"), type);
    }
  }

  @Test
  void aNewTableGetsFiveColumnsWithFreshIds() {
    ContentElement table = element("Table");

    ContentElementDefaults.applyMissing(table);

    List<Map<String, Object>> columns = ContentTableColumns.columns(table);
    assertEquals(5, columns.size());
    assertEquals("left", columns.get(0).get("pinning"));
    assertEquals("right", columns.get(4).get("pinning"));
    assertEquals(5, columns.stream().map(column -> column.get("id")).distinct().count());
  }

  @Test
  void unknownTypesAreLeftAlone() {
    ContentElement custom = element("com.acme.Widget");

    ContentElementDefaults.applyMissing(custom);

    assertEquals(null, custom.getProps());
  }
}
