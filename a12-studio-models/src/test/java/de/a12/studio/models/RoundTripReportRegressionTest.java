package de.a12.studio.models;

import de.a12.studio.models.documentmodel.ModelInfo;
import de.a12.studio.models.formmodel.Cell;
import de.a12.studio.models.formmodel.FieldConfiguration;
import de.a12.studio.models.formmodel.FormModelContent;
import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.models.formmodel.ScreenElement;
import de.a12.studio.models.overviewmodel.Alignment;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.models.relationshipmodel.RelationshipModelContent;
import de.a12.studio.models.treemodel.TreeConfiguration;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoundTripReportRegressionTest {

  private static void assertRoundTrip(String json, Class<?> type) throws Exception {
    Object model = JsonSettings.objectMapper.readValue(json, type);
    String saved = JsonSettings.objectMapper.writeValueAsString(model);
    JsonNode expected = JsonSettings.objectMapper.readTree(json);
    JsonNode actual = JsonSettings.objectMapper.readTree(saved);
    assertEquals(expected, actual, "Round-trip mismatch for " + type.getSimpleName() + ": " + saved);
    Object reloaded = JsonSettings.objectMapper.readValue(saved, type);
    assertEquals(saved, JsonSettings.objectMapper.writeValueAsString(reloaded), "Second save must be identical");
  }

  @Test
  void annotationWithoutValue() throws Exception {
    assertRoundTrip("{\"name\":\"a\"}", Annotation.class);
    assertRoundTrip("{\"name\":\"a\",\"value\":\"b\"}", Annotation.class);
  }

  @Test
  void multiplicityWithoutUpperLimit() throws Exception {
    assertRoundTrip("{\"unbounded\":true}", Multiplicity.class);
    assertRoundTrip("{\"unbounded\":true,\"upperLimit\":null}", Multiplicity.class);
    assertRoundTrip("{\"unbounded\":false,\"upperLimit\":3}", Multiplicity.class);
  }

  @Test
  void alignmentWithoutVertical() throws Exception {
    assertRoundTrip("{\"horizontal\":\"left\"}", Alignment.class);
  }

  @Test
  void multiSelectionWithoutSelectionArea() throws Exception {
    assertRoundTrip("{\"collapseOption\":\"non_collapsible\",\"counterOption\":\"simple\"}", MultiSelectionConfig.class);
  }

  @Test
  void treeConfigurationWithoutDnd() throws Exception {
    assertRoundTrip("{\"hierarchicalColumnRef\":\"c1\"}", TreeConfiguration.class);
  }

  @Test
  void modelInfoJoinedModelsInfo() throws Exception {
    assertRoundTrip("{\"name\":\"X_DM\",\"joinedModelsInfo\":\"Y_DM-document-meta-data+Add\"}", ModelInfo.class);
  }

  @Test
  void relationshipAssociationType() throws Exception {
    assertRoundTrip("{\"duplicatesAllowed\":false,\"associationType\":\"SHARED\",\"labels\":[],\"entityCharacteristics\":[]}",
        RelationshipModelContent.class);
  }

  @Test
  void formModelPreProcessingAndEmptyFieldConfiguration() throws Exception {
    FormModelContent content = JsonSettings.objectMapper.readValue(
        "{\"openNewDocumentPreProcessing\":\"COMPUTATIONS_AND_DEPENDENCIES\","
            + "\"openExistingDocumentPreProcessing\":\"COMPUTATIONS_AND_DEPENDENCIES\"}", FormModelContent.class);
    JsonNode saved = JsonSettings.objectMapper.valueToTree(content);
    assertEquals("COMPUTATIONS_AND_DEPENDENCIES", saved.get("openNewDocumentPreProcessing").asString());
    assertEquals("COMPUTATIONS_AND_DEPENDENCIES", saved.get("openExistingDocumentPreProcessing").asString());

    assertRoundTrip("{\"field\":[]}", FieldConfiguration.class);
    assertRoundTrip("{}", FieldConfiguration.class);
  }

  @Test
  void controlAutoExpand() throws Exception {
    assertRoundTrip("{\"type\":\"Control\",\"id\":\"c1\",\"elementRef\":\"/G/f\",\"autoExpand\":true}", Cell.class);
  }

  @Test
  void expressionCellLabel() throws Exception {
    assertRoundTrip("{\"type\":\"ExpressionCell\",\"id\":\"e1\",\"expression\":\"x\",\"label\":{\"type\":\"Multilingual\","
        + "\"multilingualText\":{\"text\":[{\"locale\":\"de\",\"text\":\"ICSMS-Link\"}]}}}", Cell.class);
  }

  @Test
  void textCellOffset() throws Exception {
    assertRoundTrip("{\"type\":\"TextCell\",\"id\":\"t1\",\"content\":{\"text\":[]},\"offset\":{\"lg\":1}}", Cell.class);
  }

  @Test
  void fieldBasedRepeatOverviewColumn() throws Exception {
    assertRoundTrip("{\"type\":\"FieldBasedRepeatOverviewColumn\",\"id\":\"col1\",\"elementRef\":\"/G/f\",\"autoExpand\":true,"
        + "\"readonlyPresentation\":\"TEXT\",\"style\":[{\"name\":\"h_leftAlign\"}]}", RepeatOverviewColumn.class);
  }

  @Test
  void repeatOverviewColumnAbsentOrEmpty() throws Exception {
    assertRoundTrip("{\"type\":\"DetachedRepeat\",\"id\":\"r1\",\"groupRef\":\"/G\"}", ScreenElement.class);
    assertRoundTrip("{\"type\":\"DetachedRepeat\",\"id\":\"r1\",\"groupRef\":\"/G\",\"repeatOverviewColumn\":[]}", ScreenElement.class);
  }

  @Test
  void inlineRepeatButtonLabels() throws Exception {
    assertRoundTrip("{\"type\":\"InlineRepeat\",\"id\":\"r1\",\"groupRef\":\"/G\",\"repeatOverviewColumn\":[],"
        + "\"buttonLabels\":{\"ADD\":{\"text\":[{\"locale\":\"de\",\"text\":\"HINZUFÜGEN\"}]}}}", ScreenElement.class);
  }

  @Test
  void elementBoxLegacyAndCurrentSlots() throws Exception {
    assertRoundTrip("{\"majorElements\":[{\"type\":\"search\"},{\"type\":\"filter\"}],\"minorElements\":[]}", ElementBox.class);
    assertRoundTrip("{\"leftSlot\":[],\"rightSlot\":[{\"type\":\"search\"}]}", ElementBox.class);
  }
}
