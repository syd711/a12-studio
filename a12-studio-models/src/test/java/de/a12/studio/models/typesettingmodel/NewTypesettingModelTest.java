package de.a12.studio.models.typesettingmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.NewModelFactory;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.JsonNode;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NewTypesettingModelTest {

  @Test
  void aNewTypesettingModelMatchesTheOneSmeCreates(@TempDir Path dir) throws Exception {
    ProjectItem folder = new ProjectItem(dir.toFile());
    folder.setRoot(true);

    ProjectItem item = NewModelFactory.createModel(folder, ModelType.TYPESETTING, "Fresh_TSM");

    JsonNode json = JsonSettings.objectMapper.readTree(Files.readString(dir.resolve("Fresh_TSM.json")));
    assertEquals("Fresh_TSM", json.at("/header/id").asString());
    assertEquals("typesetting", json.at("/header/modelType").asString());
    assertEquals("3.2.0", json.at("/header/modelVersion").asString());
    assertFalse(json.get("header").has("locales"), "a typesetting header has no locales");
    assertFalse(json.get("header").has("labels"));
    assertTrue(json.at("/content/customHyphenationExclusions").isArray());
    assertEquals(0, json.at("/content/customHyphenationExclusions").size());
    assertTrue(json.at("/content/preventLineBreakRules").isArray());
    assertEquals(0, json.at("/content/preventLineBreakRules").size());
    assertTrue(json.at("/content/internal").isObject());
    assertEquals(2, json.at("/content/orphan").asInt());
    assertEquals(2, json.at("/content/widow").asInt());

    A12Model<?> reloaded = new ProjectItem(item.getFile()).getModel();
    assertInstanceOf(TypesettingModel.class, reloaded);
  }
}
