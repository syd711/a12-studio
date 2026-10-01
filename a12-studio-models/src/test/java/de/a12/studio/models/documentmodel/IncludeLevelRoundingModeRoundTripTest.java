package de.a12.studio.models.documentmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Gaps 7 and 8 of "Document Model: gap review": {@code includeConfig.includeLevel} and a computation's
 * {@code roundingMode} have no UI on either side but must survive load/save, and stay absent when absent.
 */
class IncludeLevelRoundingModeRoundTripTest {

  @Test
  void includeLevelRoundTrips() throws Exception {
    String json = "{\"reference\":\"Base_DM\",\"excludeRules\":true,\"includeLevel\":\"MODEL_ROOT\"}";
    IncludeConfig config = JsonSettings.objectMapper.readValue(json, IncludeConfig.class);
    assertEquals("MODEL_ROOT", config.getIncludeLevel());
    assertSameTree(json, JsonSettings.objectMapper.writeValueAsString(config));
  }

  @Test
  void roundingModeRoundTrips() throws Exception {
    String json = "{\"computedFieldRelPath\":\"total\",\"computationAlternatives\":[{\"operation\":\"a + b\"}],"
        + "\"errorMessage\":[],\"roundingMode\":\"RoundUp\"}";
    ComputationConfig config = JsonSettings.objectMapper.readValue(json, ComputationConfig.class);
    assertEquals("RoundUp", config.getRoundingMode());
    assertSameTree(json, JsonSettings.objectMapper.writeValueAsString(config));
  }

  @Test
  void absentFieldsStayAbsent() throws Exception {
    String include = JsonSettings.objectMapper.writeValueAsString(
        JsonSettings.objectMapper.readValue("{\"reference\":\"Base_DM\"}", IncludeConfig.class));
    assertFalse(JsonSettings.objectMapper.readTree(include).has("includeLevel"));

    String computation = JsonSettings.objectMapper.writeValueAsString(JsonSettings.objectMapper.readValue(
        "{\"computedFieldRelPath\":\"total\",\"computationAlternatives\":[],\"errorMessage\":[]}", ComputationConfig.class));
    assertFalse(JsonSettings.objectMapper.readTree(computation).has("roundingMode"));
  }

  private static void assertSameTree(String expected, String actual) throws Exception {
    JsonNode expectedTree = JsonSettings.objectMapper.readTree(expected);
    JsonNode actualTree = JsonSettings.objectMapper.readTree(actual);
    assertEquals(expectedTree, actualTree);
  }
}
