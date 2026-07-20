package de.a12.studio.models.documentmodel;

import de.a12.studio.models.Label;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Regression test for a bug where {@link StringTypeOptions} had no field for a {@code StringType}
 * field's {@code errorMessage} (the pattern/length validation message), so it was silently dropped
 * whenever a loaded {@link DocumentModel} was re-serialized, just like the {@code typeDefinitions}
 * / {@code indexFieldName} bug covered by {@link TypeDefinitionAndIndexFieldRoundTripTest}.
 */
class StringTypeErrorMessageRoundTripTest {

  @Test
  void preservesStringTypeErrorMessageThroughLoadAndReserialize() throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream("/documentmodel/PaymentInfo_DM.json")) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    JsonMapper mapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    DocumentModel model = mapper.readValue(json, DocumentModel.class);
    assertErrorMessage(model);

    // Re-serializing an already-loaded model (as the project-wide validation path does) must not lose it.
    String reserialized = mapper.writeValueAsString(model);
    DocumentModel reloaded = mapper.readValue(reserialized, DocumentModel.class);
    assertErrorMessage(reloaded);
  }

  private void assertErrorMessage(DocumentModel model) {
    GroupElement creditCard = (GroupElement) model.getContent().getModelRoot().getRootGroups().get(0)
        .getGroup().getElements().stream()
        .filter(e -> "CreditCard".equals(e.getName()))
        .findFirst().orElseThrow();
    FieldElement number = (FieldElement) creditCard.getGroup().getElements().stream()
        .filter(e -> "Number".equals(e.getName()))
        .findFirst().orElseThrow();
    StringFieldType fieldType = (StringFieldType) number.getField().getFieldType();
    List<Label> errorMessage = fieldType.getStringType().getErrorMessage();

    assertEquals(2, errorMessage.size());
    assertEquals("en", errorMessage.get(0).getLocale());
    assertEquals("The credit card number must contain 16 digits.", errorMessage.get(0).getText());
    assertEquals("de", errorMessage.get(1).getLocale());
    assertEquals("Die Kreditkartennummer muss aus 16 Ziffern bestehen.", errorMessage.get(1).getText());
  }
}
