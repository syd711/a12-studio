package de.a12.studio.models.contentmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LexicalTextTest {

  private static ContentElement element(String type, String json) {
    ContentElement element = new ContentElement();
    element.setType(type);
    @SuppressWarnings("unchecked")
    Map<String, Object> props = JsonSettings.objectMapper.readValue(json, LinkedHashMap.class);
    element.setProps(props);
    return element;
  }

  private static String run(int format, String style, String text) {
    return "{\"detail\":0,\"format\":" + format + ",\"mode\":\"normal\",\"style\":\"" + style + "\",\"text\":\"" + text
        + "\",\"type\":\"text\",\"version\":1}";
  }

  private static String block(String type, String... runs) {
    return "{\"children\":[" + String.join(",", runs) + "],\"direction\":\"ltr\",\"format\":\"\",\"indent\":0,\"type\":\""
        + type + "\",\"version\":1" + ("heading".equals(type) ? ",\"tag\":\"h2\"" : "") + "}";
  }

  private static ContentElement paragraph(String... blocks) {
    return element("Paragraph", "{\"style\":{\"width\":\"100%\"},\"tree\":{\"root\":{\"children\":["
        + String.join(",", blocks) + "],\"direction\":\"ltr\",\"format\":\"\",\"indent\":0,\"type\":\"root\",\"version\":1}},"
        + "\"html\":\"stale\"}");
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> blocksOf(ContentElement element) {
    Map<String, Object> tree = (Map<String, Object>) element.getProps().get("tree");
    return (List<Map<String, Object>>) ((Map<String, Object>) tree.get("root")).get("children");
  }

  @SuppressWarnings("unchecked")
  private static List<Map<String, Object>> runsOf(Map<String, Object> block) {
    return (List<Map<String, Object>>) block.get("children");
  }

  @Test
  void blocksAreTheLinesOfTheText() {
    ContentElement element = paragraph(block("paragraph", run(0, "", "one")), block("paragraph", run(0, "", "two")));

    assertEquals("one\ntwo", LexicalText.getText(element));
  }

  @Test
  void anUnchangedTextLeavesTheElementUntouched() {
    ContentElement element = paragraph(block("paragraph", run(0, "", "one")));

    assertTrue(LexicalText.setText(element, "one"));

    assertEquals("stale", element.getProps().get("html"));
  }

  @Test
  void editingKeepsTheFormattingOfTheRunsAroundTheChange() {
    ContentElement element = paragraph(block("paragraph", run(0, "font-size: 14px;", "Hello "), run(1, "font-weight: 700;", "bold"),
        run(0, "font-size: 14px;", " world")));

    LexicalText.setText(element, "Hello brave bold world");

    List<Map<String, Object>> runs = runsOf(blocksOf(element).get(0));
    assertEquals(3, runs.size());
    assertEquals("Hello ", runs.get(0).get("text"));
    assertEquals("brave bold", runs.get(1).get("text"));
    assertEquals(1, runs.get(1).get("format"));
    assertEquals(" world", runs.get(2).get("text"));
  }

  @Test
  void typingAtARunBoundaryContinuesTheEarlierRun() {
    ContentElement element = paragraph(block("paragraph", run(1, "", "bold"), run(0, "", " plain")));

    LexicalText.setText(element, "bold!! plain");

    List<Map<String, Object>> runs = runsOf(blocksOf(element).get(0));
    assertEquals("bold!!", runs.get(0).get("text"));
    assertEquals(" plain", runs.get(1).get("text"));
  }

  @Test
  void deletingAWholeRunRemovesIt() {
    ContentElement element = paragraph(block("paragraph", run(0, "", "a"), run(1, "", "b"), run(0, "", "c")));

    LexicalText.setText(element, "ac");

    assertEquals(2, runsOf(blocksOf(element).get(0)).size());
  }

  @Test
  void aNewLineBecomesANewBlockShapedLikeItsNeighbor() {
    ContentElement element = paragraph(block("paragraph", run(0, "font-size: 14px;", "first")));

    LexicalText.setText(element, "first\nsecond");

    List<Map<String, Object>> blocks = blocksOf(element);
    assertEquals(2, blocks.size());
    Map<String, Object> second = runsOf(blocks.get(1)).get(0);
    assertEquals("second", second.get("text"));
    assertEquals("font-size: 14px;", second.get("style"));
    assertEquals("first", runsOf(blocks.get(0)).get(0).get("text"));
  }

  @Test
  void removingAMiddleLineKeepsTheOtherBlocksUntouched() {
    ContentElement element = paragraph(block("paragraph", run(0, "", "one")), block("paragraph", run(1, "", "two")),
        block("paragraph", run(2, "", "three")));

    LexicalText.setText(element, "one\nthree");

    List<Map<String, Object>> blocks = blocksOf(element);
    assertEquals(2, blocks.size());
    assertEquals(2, runsOf(blocks.get(1)).get(0).get("format"));
  }

  @Test
  void clearingTheTextLeavesAnEmptyBlockThatRemembersItsStyle() {
    ContentElement element = paragraph(block("paragraph", run(1, "font-size: 20px;", "gone")));

    LexicalText.setText(element, "");
    assertEquals("", LexicalText.getText(element));
    assertTrue(runsOf(blocksOf(element).get(0)).isEmpty());
    assertTrue(((String) element.getProps().get("html")).contains("<br>"));

    LexicalText.setText(element, "back");
    Map<String, Object> run = runsOf(blocksOf(element).get(0)).get(0);
    assertEquals("back", run.get("text"));
    assertEquals("font-size: 20px;", run.get("style"));
    assertEquals(1, run.get("format"));
  }

  @Test
  void theHtmlIsRegeneratedInLexicalsShape() {
    ContentElement element = paragraph(block("paragraph", run(0, "font-size: 14px;font-weight: 400;", "a"), run(1, "", "b & c")));

    LexicalText.setText(element, "a & b & c");

    assertEquals("<p class=\"editor-paragraph\" dir=\"ltr\"><span style=\"font-size: 14px; font-weight: 400; white-space: pre-wrap;\">"
            + "a &amp; </span><b><strong class=\"editor-text-bold\" style=\"white-space: pre-wrap;\">b &amp; c</strong></b></p>",
        element.getProps().get("html"));
  }

  @Test
  void aHeadingKeepsItsTag() {
    ContentElement element = element("Heading", "{\"tree\":{\"root\":{\"children\":[" + block("heading", run(0, "", "Title"))
        + "],\"type\":\"root\",\"version\":1}},\"html\":\"stale\"}");

    LexicalText.setText(element, "Other title");

    assertEquals("<h2 class=\"editor-heading-h2\" dir=\"ltr\"><span style=\"white-space: pre-wrap;\">Other title</span></h2>",
        element.getProps().get("html"));
  }

  @Test
  void anElementWithoutATreeGetsADefaultOne() {
    ContentElement element = new ContentElement();
    element.setType("Paragraph");

    assertTrue(LexicalText.isEditable(element));
    assertEquals("", LexicalText.getText(element));
    LexicalText.setText(element, "Hello");

    assertEquals("Hello", LexicalText.getText(element));
    assertNotNull(element.getProps().get("html"));
    assertFalse(((String) element.getProps().get("html")).contains("Enter some text"));
  }

  @Test
  void aTreeWithLinksCanBeReadButNotEdited() {
    ContentElement element = paragraph(block("paragraph", run(0, "", "See "),
        "{\"children\":[" + run(0, "", "docs") + "],\"type\":\"link\",\"url\":\"https://a\",\"version\":1}",
        "{\"type\":\"ce-field-reference\",\"text\":\"\",\"fieldPath\":\"/Product/Name\",\"version\":1}"));

    assertEquals("See docs[/Product/Name]", LexicalText.getText(element));
    assertFalse(LexicalText.isEditable(element));
    assertFalse(LexicalText.setText(element, "changed"));
    assertEquals("stale", element.getProps().get("html"));
  }

  @Test
  void otherElementTypesAreNotSupported() {
    ContentElement element = new ContentElement();
    element.setType("Box");

    assertFalse(LexicalText.supports(element));
    assertFalse(LexicalText.isEditable(element));
  }

  // ---- references ----

  private static String ref(String id, String path, boolean group, int format) {
    return "{\"detail\":0,\"format\":" + format + ",\"mode\":\"normal\",\"style\":\"\",\"text\":\"\",\"type\":\"ce-field-reference\",\"version\":1,"
        + "\"fieldId\":\"" + id + "\",\"fieldPath\":\"" + path + "\"" + (group ? ",\"isGroup\":true" : "") + "}";
  }

  private static ContentElement priceLine() {
    return paragraph(block("paragraph", run(0, "", "Price: "), ref("price", "/P/Price", false, 0), run(0, "", " EUR")));
  }

  private static List<String> kinds(ContentElement element, int block) {
    return runsOf(blocksOf(element).get(block)).stream()
        .map(run -> "text".equals(run.get("type")) ? "text:" + run.get("text") : "ref:" + run.get("fieldId")).toList();
  }

  @Test
  void aReferenceShowsAsItsLabelAndDoesNotMakeTheTextReadOnly() {
    ContentElement element = paragraph(block("paragraph", run(0, "", "See "), ref("g", "/P/Items", true, 0), ref("f", "/P/Name", false, 0)));

    assertEquals("See IndexOf(/P/Items)[/P/Name]", LexicalText.getText(element));
    assertTrue(LexicalText.isEditable(element));
  }

  @Test
  void editingTheWordsAroundAReferenceKeepsTheReference() {
    ContentElement element = priceLine();
    Object node = runsOf(blocksOf(element).get(0)).get(1);

    assertTrue(LexicalText.setText(element, "Cost: [/P/Price] EUR!"));

    assertEquals(List.of("text:Cost: ", "ref:price", "text: EUR!"), kinds(element, 0));
    assertTrue(node == runsOf(blocksOf(element).get(0)).get(1), "the reference node itself is untouched");
    assertEquals("Cost: [/P/Price] EUR!", LexicalText.getText(element));
    assertTrue(((String) element.getProps().get("html")).contains("data-ce-field-ref-id=\"price\""), (String) element.getProps().get("html"));
  }

  @Test
  void deletingAllOfAReferencesLabelRemovesIt() {
    ContentElement element = priceLine();

    assertTrue(LexicalText.setText(element, "Price:  EUR"));

    assertEquals(List.of("text:Price: ", "text: EUR"), kinds(element, 0));
    assertEquals(List.of(), ContentNodes.lexicalReferences(element));
  }

  @Test
  void changingPartOfAReferencesLabelTurnsItIntoPlainText() {
    ContentElement element = priceLine();

    assertTrue(LexicalText.setText(element, "Price: [/P/Pxrice] EUR"));

    assertEquals(List.of(), ContentNodes.lexicalReferences(element));
    assertEquals("Price: [/P/Pxrice] EUR", LexicalText.getText(element));
    kinds(element, 0).forEach(kind -> assertTrue(kind.startsWith("text:"), kind));
  }

  @Test
  void typingRightNextToAReferenceGoesIntoTextOfItsOwn() {
    ContentElement element = paragraph(block("paragraph", ref("f", "/P/Name", false, 0)));

    assertTrue(LexicalText.setText(element, "[/P/Name]!"));
    assertTrue(LexicalText.setText(element, "?[/P/Name]!"));

    assertEquals(List.of("text:?", "ref:f", "text:!"), kinds(element, 0));
  }

  @Test
  void aNewLineAfterAReferenceStartsABlockWithoutIt() {
    ContentElement element = priceLine();

    assertTrue(LexicalText.setText(element, "Price: [/P/Price] EUR\nsecond"));

    assertEquals(2, blocksOf(element).size());
    assertEquals(List.of("text:second"), kinds(element, 1));
    assertEquals(List.of("text:Price: ", "ref:price", "text: EUR"), kinds(element, 0));
  }

  @Test
  void aReferenceIsInsertedIntoTheMiddleOfTheWords() {
    ContentElement element = paragraph(block("paragraph", run(1, "font-weight: 700;", "Hello world")));

    assertTrue(LexicalText.insertReference(element, 6, "f", "/P/Name", false));

    assertEquals(List.of("text:Hello ", "ref:f", "text:world"), kinds(element, 0));
    Map<String, Object> inserted = runsOf(blocksOf(element).get(0)).get(1);
    assertEquals("ce-field-reference", inserted.get("type"));
    assertEquals("/P/Name", inserted.get("fieldPath"));
    assertEquals(1, inserted.get("format"), "takes the formatting of what precedes it");
    assertFalse(inserted.containsKey("isGroup"));
    assertEquals("Hello [/P/Name]world", LexicalText.getText(element));
    assertEquals(List.of("f"), ContentNodes.lexicalReferences(element).stream().map(ContentNodes.LexicalReference::fieldId).toList());
    assertTrue(LexicalText.setText(element, LexicalText.getText(element)), "the label in the text does not double it");
    assertEquals(List.of("text:Hello ", "ref:f", "text:world"), kinds(element, 0));
  }

  @Test
  void aReferenceCanBeInsertedAtTheEndsAndIntoLaterBlocksAndAsAGroup() {
    ContentElement element = paragraph(block("paragraph", run(0, "", "one")), block("paragraph", run(0, "", "two")));

    assertTrue(LexicalText.insertReference(element, 0, "a", "/P/A", false));
    // "[/P/A]one" is 9 characters, the newline the tenth: 3 more put the caret into "two".
    assertTrue(LexicalText.insertReference(element, 9 + 1 + 3, "b", "/P/B", true));
    assertTrue(LexicalText.insertReference(element, 999, "c", "/P/C", false));

    assertEquals(List.of("ref:a", "text:one"), kinds(element, 0));
    assertEquals(List.of("text:two", "ref:b", "ref:c"), kinds(element, 1));
    assertEquals("[/P/A]one\ntwoIndexOf(/P/B)[/P/C]", LexicalText.getText(element));
    assertEquals(List.of(false, true, false), ContentNodes.lexicalReferences(element).stream().map(ContentNodes.LexicalReference::group).toList());
  }

  @Test
  void aReferenceIsInsertedIntoAnEmptyElementAndNotIntoOneWithLinks() {
    ContentElement empty = new ContentElement();
    empty.setType("Heading");
    assertTrue(LexicalText.insertReference(empty, 0, "f", "/P/Name", false));
    assertEquals("[/P/Name]", LexicalText.getText(empty));

    ContentElement withLink = paragraph(block("paragraph",
        "{\"children\":[" + run(0, "", "docs") + "],\"type\":\"link\",\"url\":\"https://a\",\"version\":1}"));
    assertFalse(LexicalText.insertReference(withLink, 0, "f", "/P/Name", false));
    assertEquals("stale", withLink.getProps().get("html"));
  }

  @Test
  void theDisplayOfAFieldReferenceCanBeSetAndItsDefaultTextRemoved() {
    ContentElement element = paragraph(block("paragraph", ref("a", "/P/A", false, 0), ref("g", "/P/G", true, 0), ref("b", "/P/B", false, 0)));

    assertEquals(List.of(new LexicalText.ReferenceOptions("[/P/A]", "value-only", ""), new LexicalText.ReferenceOptions("[/P/B]", "value-only", "")),
        LexicalText.fieldReferenceOptions(element), "a group reference has no options");

    assertTrue(LexicalText.setReferenceOptions(element, 1, "label-value", "n/a"));

    assertEquals("value-only", LexicalText.fieldReferenceOptions(element).get(0).displayOption());
    assertEquals(new LexicalText.ReferenceOptions("[/P/B]", "label-value", "n/a"), LexicalText.fieldReferenceOptions(element).get(1));
    assertEquals("label-value", runsOf(blocksOf(element).get(0)).get(2).get("displayOption"));
    assertTrue(((String) element.getProps().get("html")).contains("data-ce-field-ref-missing-value-text=\"n/a\""));

    assertTrue(LexicalText.setReferenceOptions(element, 1, "label-value", ""));
    assertFalse(runsOf(blocksOf(element).get(0)).get(2).containsKey("missingValueText"));
    assertFalse(LexicalText.setReferenceOptions(element, 2, "label-value", ""), "there are two field references");
  }

  @Test
  void aNewFieldReferenceShowsTheValueOnly() {
    ContentElement element = paragraph(block("paragraph", run(0, "", "x")));

    LexicalText.insertReference(element, 1, "f", "/P/F", false);
    LexicalText.insertReference(element, 0, "g", "/P/G", true);

    assertEquals("value-only", runsOf(blocksOf(element).get(0)).get(2).get("displayOption"));
    assertFalse(runsOf(blocksOf(element).get(0)).get(0).containsKey("displayOption"), "a group reference has none");
  }
}
