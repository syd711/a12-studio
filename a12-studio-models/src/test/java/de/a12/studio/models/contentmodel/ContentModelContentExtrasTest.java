package de.a12.studio.models.contentmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/** Keys of {@code content} that the studio does not know (a newer Content Engine might add some) must survive a save. */
class ContentModelContentExtrasTest {

  @Test
  void unknownContentKeysAreKeptOnSave() throws Exception {
    ContentModel model = JsonSettings.objectMapper.readValue("{\"header\": {\"id\": \"Test_CM\", \"modelType\": \"content\","
        + " \"modelVersion\": \"0.8.0\"}, \"content\": {\"root\": {\"id\": \"r\", \"type\": \"Box\", \"namespace\": \"n\","
        + " \"props\": {}}, \"futureThing\": {\"a\": [1, 2]}}}", ContentModel.class);

    JsonNode content = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("content");

    assertEquals(2, content.at("/futureThing/a").size());
    assertEquals("r", content.at("/root/id").asString());
  }

  @Test
  void aModelWithoutExtrasWritesNothingExtra() throws Exception {
    ContentModel model = JsonSettings.objectMapper.readValue("{\"header\": {\"id\": \"Test_CM\", \"modelType\": \"content\","
        + " \"modelVersion\": \"0.8.0\"}, \"content\": {\"root\": {\"id\": \"r\", \"type\": \"Box\", \"namespace\": \"n\","
        + " \"props\": {}}}}", ContentModel.class);

    JsonNode content = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("content");

    assertFalse(content.has("extras"));
    assertEquals(2, content.size(), "configuration and root only: " + content);
  }
}
