package de.a12.studio.dataservices.models.documentmodel;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for two bugs where kernel validation reported spurious errors after a model was
 * loaded and re-serialized (e.g. by the project-wide validation path in ProjectTreeController):
 * <ul>
 *   <li>{@link EnumerationTypeOptions} had no field for a type definition's {@code categories}, so a
 *   computation referencing {@code [Field -> CategoryName]} failed with "Category '...' is unknown".</li>
 *   <li>{@link NumberFieldType} had no field for {@code NumberType} at all, so every NumberType field's
 *   {@code minFractionalDigits}/{@code maxFractionalDigits}/{@code minValue}/{@code trait}/
 *   {@code zeroNotAllowed} were dropped, producing "MVK_INVALID_COMPARE_DEC_PLACES" for computations
 *   that relied on the configured fractional digits.</li>
 * </ul>
 */
class NumberTypeAndCategoryRoundTripTest {

  @Test
  void preservesCategoriesAndNumberTypeOptionsThroughLoadAndReserialize() throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream("/documentmodel/Order_DM.json")) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    JsonMapper mapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    DocumentModel model = mapper.readValue(json, DocumentModel.class);
    assertCategoryAndNumberType(model);

    // Re-serializing an already-loaded model (as the project-wide validation path does) must not lose either.
    String reserialized = mapper.writeValueAsString(model);
    DocumentModel reloaded = mapper.readValue(reserialized, DocumentModel.class);
    assertCategoryAndNumberType(reloaded);
  }

  private void assertCategoryAndNumberType(DocumentModel model) {
    TypeDefinition productType = model.getContent().getTypeDefinitions().get(0);
    EnumerationFieldType fieldType = (EnumerationFieldType) productType.getFieldType();
    Category category = fieldType.getEnumerationType().getCategories().get(0);
    assertEquals("Cat", category.getName());
    assertEquals(9, category.getValues().size());
    assertEquals("Food", category.getValues().get(0));
    assertEquals("Non-Food", category.getValues().get(1));

    GroupElement orderInformation = (GroupElement) model.getContent().getModelRoot().getRootGroups().get(0)
        .getGroup().getElements().stream()
        .filter(e -> "OrderInformation".equals(e.getName()))
        .findFirst().orElseThrow();
    FieldElement price = (FieldElement) orderInformation.getGroup().getElements().stream()
        .filter(e -> "Price".equals(e.getName()))
        .findFirst().orElseThrow();
    NumberTypeOptions numberType = ((NumberFieldType) price.getField().getFieldType()).getNumberType();

    assertEquals(2, numberType.getMinFractionalDigits());
    assertEquals(2, numberType.getMaxFractionalDigits());
    assertEquals(0.01, numberType.getMinValue());
    assertEquals("amount", numberType.getTrait());
  }
}
