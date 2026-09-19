package de.a12.studio.models.formmodel;

import de.a12.studio.models.TestHelper;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormModelWalkerTest {

  @Test
  void findsNothingInNoContent() {
    assertEquals(List.of(), FormModelWalker.find(null, Control.class));
    assertEquals(List.of(), FormModelWalker.find(new FormModelContent(), Control.class));
  }

  // The reflective walk must find exactly as many Controls / overview columns as there are objects of that
  // "type" in the raw JSON of every fixture form model - the guarantee that no container type is missed
  // (control grids inside sections, embedded repeats, detail screens ...).
  @Test
  void findsEveryControlAndColumnInTheFixtureWorkspaces() throws Exception {
    int controlsInJson = 0;
    int columnsInJson = 0;
    int controlsFound = 0;
    int columnsFound = 0;
    for (Path workspace : List.of(TestHelper.resolveTestingBasicDir(), TestHelper.resolveTestingAdvancedNewDir(),
        TestHelper.resolveTestingCommerceDir())) {
      for (Path file : jsonFiles(workspace)) {
        String json = Files.readString(file, StandardCharsets.UTF_8);
        JsonNode tree = JsonSettings.objectMapper.readTree(json);
        if (!"form".equals(tree.path("header").path("modelType").asString())) {
          continue;
        }
        FormModelContent content = JsonSettings.objectMapper.readValue(json, FormModel.class).getContent();
        int controls = count(tree.get("content"), "Control");
        int columns = count(tree.get("content"), "FieldBasedRepeatOverviewColumn")
            + count(tree.get("content"), "ExpressionRepeatOverviewColumn");
        assertEquals(controls, FormModelWalker.find(content, Control.class).size(), "controls in " + file);
        assertEquals(columns, FormModelWalker.find(content, RepeatOverviewColumn.class).size(), "columns in " + file);
        controlsInJson += controls;
        columnsInJson += columns;
        controlsFound += FormModelWalker.find(content, Control.class).size();
        columnsFound += FormModelWalker.find(content, RepeatOverviewColumn.class).size();
      }
    }
    assertTrue(controlsInJson > 0 && columnsInJson > 0, "the fixtures no longer contain controls and columns");
    assertEquals(controlsInJson, controlsFound);
    assertEquals(columnsInJson, columnsFound);
  }

  private static List<Path> jsonFiles(Path workspace) throws IOException {
    try (Stream<Path> walk = Files.walk(workspace)) {
      return walk.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
    }
  }

  // Number of JSON objects whose "type" is the given value.
  private static int count(JsonNode node, String type) {
    int count = 0;
    if (node.isObject()) {
      if (type.equals(node.path("type").asString())) {
        count++;
      }
      for (Map.Entry<String, JsonNode> entry : node.properties()) {
        count += count(entry.getValue(), type);
      }
    }
    else if (node.isArray()) {
      for (JsonNode item : node) {
        count += count(item, type);
      }
    }
    return count;
  }
}
