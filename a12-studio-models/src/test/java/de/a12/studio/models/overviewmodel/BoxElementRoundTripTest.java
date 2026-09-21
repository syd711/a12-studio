package de.a12.studio.models.overviewmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * SME gives every Subheader element (button, search, filter, multi-selection) the same fields, so the three
 * non-button types have to keep them - and stay a bare {@code {"type": ...}} when they never had any.
 */
class BoxElementRoundTripTest {

  @Test
  void bareMarkerElementsStayBareAfterLoadAndSave() throws Exception {
    for (String type : new String[] {"search", "filter", "multi_selection"}) {
      BoxElement element = JsonSettings.objectMapper.readValue("{\"type\":\"" + type + "\"}", BoxElement.class);

      JsonNode written = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(element));

      assertEquals(1, written.size(), type + " must not gain keys: " + written);
      assertEquals(type, written.get("type").asText());
    }
  }

  @Test
  void searchElementKeepsItsLabelStylesAndAnnotations() throws Exception {
    String json = """
        {"type": "search", "label": [{"locale": "en", "text": "Find"}], "labelHidden": true,
         "styles": ["wide"], "annotations": [{"name": "a", "value": "b"}]}""";

    SearchElement element = assertInstanceOf(SearchElement.class, JsonSettings.objectMapper.readValue(json, BoxElement.class));
    assertEquals("Find", element.getLabel().get(0).getText());
    assertTrue(element.getLabelHidden());

    JsonNode written = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(element));
    assertEquals(JsonSettings.objectMapper.readTree(json), written);
  }
}
