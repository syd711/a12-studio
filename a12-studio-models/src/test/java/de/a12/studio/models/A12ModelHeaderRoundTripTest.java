package de.a12.studio.models;

import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Pins the header's absent-vs-explicit-empty behaviour for locales/labels/modelReferences: files that omit
// a key must not gain "[]" on save, while files that write "[]" must keep it (some fixtures rely on that).
class A12ModelHeaderRoundTripTest {

  private static final String[] OPTIONAL_KEYS = {"locales", "labels", "modelReferences"};

  @Test
  void absentKeysStayAbsent() throws Exception {
    JsonNode header = resave(headerJson(""));

    for (String key : OPTIONAL_KEYS) {
      assertFalse(header.has(key), "Absent '" + key + "' must not be written on save");
    }
  }

  @Test
  void explicitEmptyArraysStayExplicit() throws Exception {
    JsonNode header = resave(headerJson(", \"locales\": [], \"labels\": [], \"modelReferences\": []"));

    for (String key : OPTIONAL_KEYS) {
      assertTrue(header.has(key), "Explicit '" + key + "' must be written on save");
      assertTrue(header.get(key).isArray() && header.get(key).isEmpty(), "'" + key + "' must stay an empty array");
    }
  }

  @Test
  void keysAddedAfterLoadAreWritten() throws Exception {
    ContentModel model = JsonSettings.objectMapper.readValue(headerJson(""), ContentModel.class);
    Label label = new Label();
    label.setText("Added");
    model.getLabels().add(label);

    JsonNode header = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("header");

    assertEquals(1, header.get("labels").size());
    assertFalse(header.has("locales"));
    assertFalse(header.has("modelReferences"));
  }

  private static JsonNode resave(String json) throws Exception {
    ContentModel model = JsonSettings.objectMapper.readValue(json, ContentModel.class);
    return JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("header");
  }

  private static String headerJson(String extraHeaderFields) {
    return "{\"header\": {\"id\": \"Test_CM\", \"modelType\": \"content\", \"modelVersion\": \"0.8.0\", "
        + "\"annotations\": []" + extraHeaderFields + "}, \"content\": {}}";
  }
}
