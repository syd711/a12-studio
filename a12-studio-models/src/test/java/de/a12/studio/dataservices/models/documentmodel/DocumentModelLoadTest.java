package de.a12.studio.dataservices.models.documentmodel;

import de.a12.studio.dataservices.models.ModelType;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DocumentModelLoadTest {

  @Test
  void loadsCompanyDocumentModel() throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream("/documentmodel/Company_DM.json")) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    JsonMapper mapper = JsonMapper.builder()
        .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        .build();

    DocumentModel model = mapper.readValue(json, DocumentModel.class);

    assertEquals("Company_DM", model.getId());
    assertEquals(ModelType.DOCUMENT, model.getModelType());
    assertEquals("28.4.0", model.getModelVersion());
    assertEquals(2, model.getLocales().size());
    assertEquals("en", model.getLocales().get(0).getCode());
    assertEquals(2, model.getLabels().size());
    assertEquals("Company", model.getLabels().get(0).getText());
    assertEquals(1, model.getAnnotations().size());
    assertEquals("roles", model.getAnnotations().get(0).getName());
    assertTrue(model.getModelReferences().isEmpty());

    assertNotNull(model.getContent());
    assertEquals("Company_DM", model.getContent().getModelInfo().getName());
    assertEquals("UTC", model.getContent().getModelConfig().getTimeZone());
    assertEquals("en_US", model.getContent().getModelConfig().getConditionLanguage().getCode());

    assertEquals(1, model.getContent().getModelRoot().getRootGroups().size());
    GroupElement companyGroup = model.getContent().getModelRoot().getRootGroups().get(0);
    assertEquals("Group", companyGroup.getType());
    assertEquals("Company", companyGroup.getName());
    assertEquals(1, companyGroup.getGroup().getRepeatability());
    assertEquals(3, companyGroup.getGroup().getElements().size());

    GroupElement companyDetails = (GroupElement) companyGroup.getGroup().getElements().get(0);
    assertEquals("CompanyDetails", companyDetails.getName());

    FieldElement companyName = (FieldElement) companyDetails.getGroup().getElements().get(0);
    assertEquals("CompanyName", companyName.getName());
    assertEquals("StringType", companyName.getField().getFieldType().getType());
    assertTrue(companyName.getField().getFieldType() instanceof StringFieldType);
    assertEquals("absoluteOrRelativeToNextRepAncestor", companyName.getField().getRequirednessConfig().getMode());
    assertEquals("Company Name", companyName.getField().getLabel().get(0).getText());

    GroupElement companyLogo = (GroupElement) companyDetails.getGroup().getElements().get(3);
    assertEquals("attachment", companyLogo.getGroup().getUsageType());

    FieldElement sizeField = (FieldElement) companyLogo.getGroup().getElements().stream()
        .filter(e -> e instanceof FieldElement fe && "size".equals(fe.getName()))
        .findFirst().orElseThrow();
    assertTrue(sizeField.getField().getFieldType() instanceof NumberFieldType);

    RuleElement rule = (RuleElement) companyLogo.getGroup().getElements().stream()
        .filter(e -> e instanceof RuleElement)
        .findFirst().orElseThrow();
    assertEquals("ERROR", rule.getRule().getSeverity());
    assertEquals("Errorrule_b3f12", rule.getRule().getErrorCode());

    GroupElement addresses = (GroupElement) companyGroup.getGroup().getElements().get(1);
    FieldElement addressType = (FieldElement) addresses.getGroup().getElements().get(0);
    assertTrue(addressType.getField().getFieldType() instanceof EnumerationFieldType);
    EnumerationFieldType enumType = (EnumerationFieldType) addressType.getField().getFieldType();
    assertEquals(5, enumType.getEnumerationType().getValues().size());
    assertEquals("RegAddress", enumType.getEnumerationType().getValues().get(0).getValue());
    assertEquals("Registered address", enumType.getEnumerationType().getValues().get(0).getLabel().get(0).getText());
  }
}
