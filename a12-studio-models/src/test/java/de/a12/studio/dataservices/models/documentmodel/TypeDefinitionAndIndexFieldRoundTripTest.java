package de.a12.studio.dataservices.models.documentmodel;

import de.a12.studio.models.documentmodel.*;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Regression test for a bug where {@link DocumentModelContent} had no field for the document's top-level
 * {@code typeDefinitions} array and {@link GroupConfig} had none for a group's {@code indexFieldName}, so
 * both were silently dropped whenever a loaded {@link DocumentModel} was re-serialized (e.g. for project-wide
 * validation in ProjectTreeController). That produced false-positive "Missing Type Definition" and "no
 * repeatable group with index field" kernel validation errors, even though the source file was fine.
 */
class TypeDefinitionAndIndexFieldRoundTripTest {

  @Test
  void preservesTypeDefinitionsAndIndexFieldNameThroughLoadAndReserialize() throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream("/documentmodel/Order_DM.json")) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    JsonMapper mapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    DocumentModel model = mapper.readValue(json, DocumentModel.class);

    assertEquals(1, model.getContent().getTypeDefinitions().size());
    TypeDefinition productType = model.getContent().getTypeDefinitions().get(0);
    assertEquals("typedef_1eeadc7f-65fb-4802-85a2-c055acfa64cd", productType.getId());
    assertEquals("ProductType", productType.getName());
    assertTrue(productType.getFieldType() instanceof EnumerationFieldType);

    GroupElement orderInformation = (GroupElement) model.getContent().getModelRoot().getRootGroups().get(0)
        .getGroup().getElements().stream()
        .filter(e -> "OrderInformation".equals(e.getName()))
        .findFirst().orElseThrow();
    assertEquals("OrderNumber", orderInformation.getGroup().getIndexFieldName());

    // Re-serializing an already-loaded model (as the project-wide validation path does) must not lose either.
    String reserialized = mapper.writeValueAsString(model);
    DocumentModel reloaded = mapper.readValue(reserialized, DocumentModel.class);
    assertEquals(1, reloaded.getContent().getTypeDefinitions().size());
    GroupElement reloadedOrderInformation = (GroupElement) reloaded.getContent().getModelRoot().getRootGroups().get(0)
        .getGroup().getElements().stream()
        .filter(e -> "OrderInformation".equals(e.getName()))
        .findFirst().orElseThrow();
    assertEquals("OrderNumber", reloadedOrderInformation.getGroup().getIndexFieldName());
  }
}
