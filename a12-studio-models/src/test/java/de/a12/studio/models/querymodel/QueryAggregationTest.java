package de.a12.studio.models.querymodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Wire shape of {@code content.aggregation}: Data Services' Query API documentation ("Aggregations"), plus SME's
 * optional {@code alias} (its {@code QMPostProcessingMetaModel}) - no real model file with an aggregation exists in
 * any fixture, so the fixture is built from those two sources.
 */
class QueryAggregationTest {

  @Test
  void loadsAggregationWithGroupAndAliasedEntry() throws Exception {
    QueryModel model = ModelRoundTrip.load(getClass(), "/querymodel/QueryModelWithAggregation.json", QueryModel.class);

    QueryAggregation aggregation = model.getContent().getAggregation();
    assertNotNull(aggregation);
    assertEquals(1, aggregation.getGroup().size());
    assertEquals("/ContractRoot/Type", aggregation.getGroup().get(0).getField());

    assertEquals(2, aggregation.getAggregations().size());
    assertEquals(QueryAggregationEntry.FUNCTION_SUM, aggregation.getAggregations().get(0).getFunction());
    assertEquals("/ContractRoot/Liability", aggregation.getAggregations().get(0).getField());
    assertNull(aggregation.getAggregations().get(0).getAlias());
    assertEquals("contracts", aggregation.getAggregations().get(1).getAlias());
  }

  @Test
  void roundTripsAggregation() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/querymodel/QueryModelWithAggregation.json", QueryModel.class);
  }

  @Test
  void aQueryWithoutAggregationStaysWithout() throws Exception {
    QueryModel model = ModelRoundTrip.load(getClass(), "/querymodel/QueryModel.json", QueryModel.class);
    assertNull(model.getContent().getAggregation());

    JsonNode saved = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model));
    assertFalse(saved.get("content").has("aggregation"));
    assertFalse(saved.get("content").has("aggregateResults"));
  }

  @Test
  void anAbsentGroupIsToldApartFromAnExplicitEmptyOne() throws Exception {
    String noGroup = query("{\"aggregations\": [{\"function\": \"count\", \"field\": \"/A/B\"}]}");
    String emptyGroup = query("{\"aggregations\": [{\"function\": \"count\", \"field\": \"/A/B\"}], \"group\": []}");

    assertFalse(resave(noGroup).get("content").get("aggregation").has("group"));
    JsonNode group = resave(emptyGroup).get("content").get("aggregation").get("group");
    assertNotNull(group);
    assertTrue(group.isArray());
    assertEquals(0, group.size());
  }

  @Test
  void anEmptyAggregationBlockKeepsItsPresenceBecauseItIsTheSwitch() throws Exception {
    QueryModel model = JsonSettings.objectMapper.readValue(query("{}"), QueryModel.class);

    assertNotNull(model.getContent().getAggregation());
    assertTrue(model.getContent().getAggregation().getGroup().isEmpty());
    assertTrue(model.getContent().getAggregation().getAggregations().isEmpty());
    assertTrue(resave(query("{}")).get("content").has("aggregation"));
  }

  @Test
  void aNewAggregationSerializesWithoutEmptyLists() throws Exception {
    QueryModel model = JsonSettings.objectMapper.readValue(query("{}"), QueryModel.class);
    model.getContent().setAggregation(new QueryAggregation());
    QueryAggregationEntry entry = new QueryAggregationEntry();
    entry.setFunction(QueryAggregationEntry.FUNCTION_MAX);
    entry.setField("/A/B");
    model.getContent().getAggregation().getAggregations().add(entry);

    JsonNode aggregation = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model))
        .get("content").get("aggregation");
    assertEquals(1, aggregation.get("aggregations").size());
    assertFalse(aggregation.has("group"));
    assertFalse(aggregation.get("aggregations").get(0).has("alias"));
  }

  private static String query(String aggregation) {
    return "{\"header\": {\"id\": \"Q\", \"modelType\": \"query\", \"modelVersion\": \"0.1.0\"},"
        + " \"content\": {\"projectionName\": \"document\", \"targetDocumentModel\": \"DM\", \"aggregation\": " + aggregation + "}}";
  }

  private static JsonNode resave(String json) throws Exception {
    QueryModel model = JsonSettings.objectMapper.readValue(json, QueryModel.class);
    return JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model));
  }
}
