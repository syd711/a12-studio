package de.a12.studio.models.contentmodel;

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContentPropsTest {

  private static ContentElement element(Map<String, Object> props) {
    ContentElement element = new ContentElement();
    element.setType("Box");
    element.setProps(props);
    return element;
  }

  @Test
  void readsNestedValuesByDottedPath() {
    Map<String, Object> style = new LinkedHashMap<>();
    style.put("width", "100%");
    style.put("gap", 4);
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("style", style);
    props.put("noGutter", true);
    ContentProps p = new ContentProps(element(props));

    assertEquals("100%", p.getString("style.width"));
    assertEquals("4", p.getString("style.gap"));
    assertNull(p.getString("style.height"));
    assertNull(p.getString("style.width.deeper"));
    assertNull(p.getString("missing.path"));
    assertTrue(p.getBoolean("noGutter", false));
    assertFalse(p.getBoolean("fitToParent", false));
    assertTrue(p.getBoolean("fitToParent", true));
  }

  @Test
  void listsAndMapsAreNotStrings() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("columns", List.of());
    props.put("style", new LinkedHashMap<>());
    ContentProps p = new ContentProps(element(props));

    assertNull(p.getString("columns"));
    assertNull(p.getString("style"));
  }

  @Test
  void writingCreatesPropsAndIntermediateMaps() {
    ContentElement element = new ContentElement();
    ContentProps p = new ContentProps(element);

    p.set("style.padding", "8px");
    p.set("icons.size", "big");

    assertEquals("8px", p.getString("style.padding"));
    assertEquals("big", element.getProps().get("icons") instanceof Map<?, ?> icons ? icons.get("size") : null);
  }

  @Test
  void removingPrunesEmptiedParentsButNeverProps() {
    Map<String, Object> style = new LinkedHashMap<>();
    style.put("width", "100%");
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("style", style);
    props.put("noGutter", true);
    ContentElement element = element(props);
    ContentProps p = new ContentProps(element);

    p.remove("style.width");
    assertFalse(element.getProps().containsKey("style"));
    assertTrue(element.getProps().containsKey("noGutter"));

    p.remove("noGutter");
    assertTrue(element.getProps().isEmpty());
    assertEquals(Map.of(), element.getProps());
  }

  @Test
  void writingNullRemoves() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("label", "Hello");
    ContentElement element = element(props);

    new ContentProps(element).set("label", null);

    assertFalse(element.getProps().containsKey("label"));
  }

  @Test
  void unrelatedPropsSurviveEdits() {
    Map<String, Object> tree = new LinkedHashMap<>();
    tree.put("root", "lexical");
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("tree", tree);
    props.put("html", "<p>x</p>");
    ContentElement element = element(props);
    ContentProps p = new ContentProps(element);

    p.set("style.width", "100%");
    p.remove("style.width");

    assertEquals(tree, element.getProps().get("tree"));
    assertEquals("<p>x</p>", element.getProps().get("html"));
  }

  @Test
  void flagsAreStoredLikeSme() {
    ContentElement element = new ContentElement();
    ContentProps p = new ContentProps(element);

    p.setFlag("noGutter", true, false);
    assertEquals(true, element.getProps().get("noGutter"));
    p.setFlag("noGutter", false, false);
    assertFalse(element.getProps().containsKey("noGutter"));

    // A flag whose default is true must record an explicit false, or the default would win again on reload.
    p.setFlag("enableColumnsResizing", false, true);
    assertEquals(false, element.getProps().get("enableColumnsResizing"));
    assertFalse(p.getBoolean("enableColumnsResizing", true));
  }

  @Test
  void anIntermediateNonMapIsReplaced() {
    Map<String, Object> props = new LinkedHashMap<>();
    props.put("style", "oops");
    ContentElement element = element(props);

    new ContentProps(element).set("style.width", "1px");

    assertEquals("1px", new ContentProps(element).getString("style.width"));
  }
}
