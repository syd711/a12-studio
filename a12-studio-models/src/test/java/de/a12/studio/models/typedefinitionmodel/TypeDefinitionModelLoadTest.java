package de.a12.studio.models.typedefinitionmodel;

import de.a12.studio.models.Annotation;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.EnumerationFieldType;
import de.a12.studio.models.documentmodel.EnumerationValue;
import de.a12.studio.models.documentmodel.StringFieldType;
import de.a12.studio.models.documentmodel.TypeDefinition;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeDefinitionModelLoadTest {

  private static final JsonMapper MAPPER = JsonMapper.builder()
      .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
      .build();

  @Test
  void loadsBasicTypeDefinitionModel() throws Exception {
    TypeDefinitionModel model = load("/typedefinitionmodel/Basic_TDM.json");

    assertEquals("Basic_TDM", model.getId());
    assertEquals(ModelType.DOCUMENT, model.getModelType());
    assertEquals("28.4.0", model.getModelVersion());
    assertEquals(2, model.getLocales().size());
    assertTrue(model.getAnnotations().stream().anyMatch(a -> "tdonly".equals(a.getName()) && "true".equals(a.getValue())));

    assertEquals("Basic_TDM", model.getContent().getModelInfo().getName());
    assertEquals("UTC", model.getContent().getModelConfig().getTimeZone());
    assertTrue(model.getContent().getModelRoot().getRootGroups().isEmpty());

    assertEquals(1, model.getContent().getTypeDefinitions().size());
    TypeDefinition typeDefinition = model.getContent().getTypeDefinitions().get(0);
    assertEquals("abc", typeDefinition.getName());
    assertInstanceOf(StringFieldType.class, typeDefinition.getFieldType());
  }

  @Test
  void loadsEnumerationTypeDefinitions() throws Exception {
    TypeDefinitionModel model = load("/typedefinitionmodel/CommonFieldDefinitions_TDM.json");

    List<TypeDefinition> typeDefinitions = model.getContent().getTypeDefinitions();
    assertEquals(2, typeDefinitions.size());

    TypeDefinition location = typeDefinitions.get(0);
    assertEquals("Location", location.getName());
    EnumerationFieldType locationType = assertInstanceOf(EnumerationFieldType.class, location.getFieldType());
    assertEquals(2, locationType.getEnumerationType().getValues().size());
    EnumerationValue munich = locationType.getEnumerationType().getValues().get(0);
    assertEquals("Munich", munich.getValue());
    assertEquals("München", munich.getLabel().get(1).getText());

    TypeDefinition personType = typeDefinitions.get(1);
    assertEquals("PersonType", personType.getName());
    Annotation rolesAnnotation = model.getAnnotations().stream()
        .filter(a -> "roles".equals(a.getName()))
        .findFirst().orElseThrow();
    assertEquals("tester,reviewer", rolesAnnotation.getValue());
  }

  private TypeDefinitionModel load(String resourcePath) throws Exception {
    String json;
    try (InputStream in = getClass().getResourceAsStream(resourcePath)) {
      json = new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    return MAPPER.readValue(json, TypeDefinitionModel.class);
  }
}
