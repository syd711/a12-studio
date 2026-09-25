package de.a12.studio.models.typesettingmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelFactory;
import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypesettingModelLoadTest {

  private static final String FIXTURE = "/typesettingmodel/Example_TSM.json";

  @Test
  void loadsTypesettingModel() throws Exception {
    TypesettingModel model = ModelRoundTrip.load(getClass(), FIXTURE, TypesettingModel.class);

    assertEquals("Example_TSM", model.getId());
    assertEquals(ModelType.TYPESETTING, model.getModelType());
    assertEquals("3.2.0", model.getModelVersion());
    assertEquals("roles", model.getAnnotations().get(0).getName());
    assertEquals("admin,guest", model.getAnnotations().get(0).getValue());
    // A typesetting header has no locales/labels/modelReferences; they must stay absent.
    assertTrue(model.getLocales().isEmpty());
    assertFalse(model.isLocalesExplicit());

    TypesettingModelContent content = model.getContent();
    assertEquals(3, content.getOrphan());
    assertEquals(2, content.getWidow());
    assertEquals(6, content.getPreventLineBreakRules().size());
    assertEquals("Beispiel", content.getCustomHyphenationExclusions().get(0).get("word").asString());
    assertTrue(content.getInternal().isObject());
  }

  @Test
  void loadsThroughTheModelFactoryByItsHeaderModelType() throws Exception {
    File file = new File(getClass().getResource(FIXTURE).toURI());

    A12Model<?> model = ModelFactory.load(new ProjectItem(file));

    assertInstanceOf(TypesettingModel.class, model);
  }

  @Test
  void roundTripsEveryRuleKindAndTheHyphenationContainers() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), FIXTURE, TypesettingModel.class);
  }

  @Test
  void aMissingOptionalKeyStaysMissing() throws Exception {
    String json = """
        {"header": {"id": "Bare_TSM", "modelType": "typesetting", "modelVersion": "3.2.0", "annotations": []},
         "content": {"preventLineBreakRules": []}}
        """;

    TypesettingModel model = JsonSettings.objectMapper.readValue(json, TypesettingModel.class);
    JsonNode content = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(model)).get("content");

    assertNull(model.getContent().getOrphan());
    assertFalse(content.has("customHyphenationExclusions"));
    assertFalse(content.has("internal"));
    assertFalse(content.has("orphan"));
    assertFalse(content.has("widow"));
  }
}
