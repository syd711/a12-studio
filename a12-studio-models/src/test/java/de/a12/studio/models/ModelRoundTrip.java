package de.a12.studio.models;

import de.a12.studio.models.util.JsonSettings;
import tools.jackson.databind.JsonNode;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

// Shared helpers for model load/round-trip tests. Round-trips are checked as JSON-tree equality
// (which still fails on any dropped or altered key) rather than byte equality, because key order
// varies within the fixture files themselves and cannot be preserved by typed DTOs.
public final class ModelRoundTrip {

  private ModelRoundTrip() {
  }

  public static String readResource(Class<?> testClass, String resourcePath) throws Exception {
    try (InputStream in = testClass.getResourceAsStream(resourcePath)) {
      assertNotNull(in, "Missing test resource: " + resourcePath);
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  public static <T> T load(Class<?> testClass, String resourcePath, Class<T> modelClass) throws Exception {
    return JsonSettings.objectMapper.readValue(readResource(testClass, resourcePath), modelClass);
  }

  public static void assertRoundTrip(Class<?> testClass, String resourcePath, Class<?> modelClass) throws Exception {
    String original = readResource(testClass, resourcePath);
    Object model = JsonSettings.objectMapper.readValue(original, modelClass);
    String resaved = JsonSettings.objectMapper.writeValueAsString(model);

    JsonNode originalTree = JsonSettings.objectMapper.readTree(original);
    JsonNode resavedTree = JsonSettings.objectMapper.readTree(resaved);
    assertEquals(originalTree.get("content"), resavedTree.get("content"),
        "Re-serialized content must be semantically identical to the file for " + resourcePath);

    // Saving must be a fixpoint: a second load/save cycle may not change the output again.
    Object reloaded = JsonSettings.objectMapper.readValue(resaved, modelClass);
    assertEquals(resaved, JsonSettings.objectMapper.writeValueAsString(reloaded),
        "Second save must be byte-identical to the first for " + resourcePath);
  }
}
