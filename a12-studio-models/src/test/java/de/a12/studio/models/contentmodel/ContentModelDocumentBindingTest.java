package de.a12.studio.models.contentmodel;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The Document Model binding of a Content Model, which SME does not store as a field but derives from the header's
 * references on import and rebuilds on export ({@code ImportTransformations}/{@code ExportTransformations}), and the
 * base group that only makes sense while a Document Model is bound.
 */
class ContentModelDocumentBindingTest {

  @Test
  void theFirstDocumentReferenceIsTheBindingWhateverItsPurpose() throws Exception {
    ContentModel model = load(references(
        reference("form", "Some_FM", "data binding"),
        reference("document", "Product_DM", "data binding"),
        reference("document", "Other_DM", "document-model-for-content-model")), null);

    assertEquals("Product_DM", model.getDocumentModelId());
  }

  @Test
  void aModelWithoutADocumentReferenceIsNotBound() throws Exception {
    assertNull(load(references(reference("form", "Some_FM", "data binding")), null).getDocumentModelId());
    assertNull(load("", null).getDocumentModelId());
  }

  @Test
  void bindingWritesTheReferenceTheWayTheSmeExportDoes() throws Exception {
    ContentModel model = load(references(reference("form", "Some_FM", "data binding")), null);

    model.setDocumentModelId("Product_DM");

    JsonNode references = header(model).get("modelReferences");
    assertEquals(2, references.size());
    JsonNode binding = references.get(0);
    assertEquals("document-model-for-content-model", binding.get("purpose").asString());
    assertEquals("document", binding.get("modelType").asString());
    assertEquals("DM", binding.get("alias").asString());
    assertEquals("Product_DM", binding.get("reference").asString());
    assertEquals("Some_FM", references.get(1).get("reference").asString(), "references to other types are kept");
    assertEquals("Product_DM", model.getDocumentModelId());
  }

  @Test
  void bindingCollapsesSeveralDocumentReferencesIntoOne() throws Exception {
    ContentModel model = load(references(
        reference("document", "Old_DM", "data binding"),
        reference("form", "Some_FM", "data binding"),
        reference("document", "Older_DM", "whatever")), null);

    model.setDocumentModelId("New_DM");

    assertEquals(2, model.getModelReferences().size());
    assertEquals("New_DM", model.getModelReferences().get(0).getReference());
    assertEquals(ModelType.FORM, model.getModelReferences().get(1).getModelType());
  }

  @Test
  void bindingTheSameModelTwiceChangesNothing() throws Exception {
    ContentModel model = load(references(reference("document", "Product_DM", "document-model-for-content-model")), null);

    model.setDocumentModelId("Product_DM");
    String once = JsonSettings.objectMapper.writeValueAsString(model);
    model.setDocumentModelId("Product_DM");

    assertEquals(once, JsonSettings.objectMapper.writeValueAsString(model));
    assertEquals(1, model.getModelReferences().size());
  }

  @Test
  void unbindingRemovesTheReferenceAndTheBaseGroup() throws Exception {
    ContentModel model = load(references(
        reference("document", "Product_DM", "document-model-for-content-model"),
        reference("form", "Some_FM", "data binding")), "group_1");

    model.setDocumentModelId(null);

    assertNull(model.getDocumentModelId());
    assertEquals(1, model.getModelReferences().size());
    assertNull(model.getContent().getConfiguration().getBaseGroupId());
    assertFalse(JsonSettings.objectMapper.writeValueAsString(model).contains("baseGroupId"), "the key is dropped, not written as null");
  }

  @Test
  void aBlankIdUnbindsToo() throws Exception {
    ContentModel model = load(references(reference("document", "Product_DM", "document-model-for-content-model")), "group_1");

    model.setDocumentModelId(" ");

    assertNull(model.getDocumentModelId());
    assertNull(model.getContent().getConfiguration().getBaseGroupId());
  }

  @Test
  void switchingTheDocumentModelKeepsTheBaseGroup() throws Exception {
    ContentModel model = load(references(reference("document", "Product_DM", "document-model-for-content-model")), "group_1");

    model.setDocumentModelId("Order_DM");

    assertEquals("group_1", model.getContent().getConfiguration().getBaseGroupId(),
        "SME leaves it and reports it as not found in the new model");
  }

  @Test
  void unbindingAModelWithoutConfigurationDoesNotFail() throws Exception {
    ContentModel model = JsonSettings.objectMapper.readValue(
        "{\"header\": {\"id\": \"Test_CM\", \"modelType\": \"content\", \"modelVersion\": \"0.8.0\"}, \"content\": {}}",
        ContentModel.class);

    model.setDocumentModelId(null);

    assertNull(model.getDocumentModelId());
  }

  @Test
  void theBaseGroupRoundTripsAndStaysAbsentWhenUnset() throws Exception {
    ContentModel withGroup = load(references(reference("document", "Product_DM", "document-model-for-content-model")), "group_1");
    JsonNode configuration = content(withGroup).get("configuration");
    assertEquals("group_1", configuration.get("baseGroupId").asString());
    assertEquals("0.9.0", configuration.get("namespaceVersions").get("com.mgmtp.a12.contentengine").asString());

    ContentModel without = load("", null);
    assertFalse(content(without).get("configuration").has("baseGroupId"));
  }

  @Test
  void theDerivedBindingIsNotAJsonProperty() throws Exception {
    ContentModel model = load(references(reference("document", "Product_DM", "document-model-for-content-model")), null);

    assertFalse(JsonSettings.objectMapper.writeValueAsString(model).contains("documentModelId"));
    assertTrue(header(model).has("modelReferences"));
  }

  private static ContentModel load(String modelReferences, String baseGroupId) throws Exception {
    String configuration = "{\"namespaceVersions\": {\"com.mgmtp.a12.contentengine\": \"0.9.0\"}"
        + (baseGroupId != null ? ", \"baseGroupId\": \"" + baseGroupId + "\"" : "") + "}";
    return JsonSettings.objectMapper.readValue("{\"header\": {\"id\": \"Test_CM\", \"modelType\": \"content\", "
        + "\"modelVersion\": \"0.8.0\", \"annotations\": []" + modelReferences + "}, \"content\": {\"configuration\": "
        + configuration + "}}", ContentModel.class);
  }

  private static String references(String... references) {
    return ", \"modelReferences\": [" + String.join(", ", references) + "]";
  }

  private static String reference(String modelType, String id, String purpose) {
    return "{\"purpose\": \"" + purpose + "\", \"modelType\": \"" + modelType + "\", \"reference\": \"" + id + "\"}";
  }

  private static JsonNode header(ContentModel model) throws Exception {
    return JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("header");
  }

  private static JsonNode content(ContentModel model) throws Exception {
    return JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("content");
  }
}
