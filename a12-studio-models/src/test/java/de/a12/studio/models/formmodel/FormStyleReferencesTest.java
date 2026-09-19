package de.a12.studio.models.formmodel;

import de.a12.studio.models.TestHelper;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FormStyleReferencesTest {

  // Style holders of every kind: screen element (section), control, row, repeat header + body, column header, a
  // button in the footer, a row action with its own styling. Model-level styles: a, b, c.
  private static final String JSON = """
      {
        "header": {"id": "Styles_FM", "modelType": "form", "modelVersion": "39.0.0"},
        "content": {
          "styles": [{"name": "a"}, {"name": "b"}, {"name": "c"}],
          "defaults": {}, "fieldConfiguration": {}, "groupConfiguration": {},
          "footerBox": {"id": "footer", "majorButtons": {"button": [
            {"type": "EVENT", "id": "button1", "name": "save", "event": "save", "buttonStyling": {"style": [{"name": "a"}]}}]}},
          "screens": [{
            "id": "screen1", "name": "Screen1",
            "screenElements": [
              {"type": "Section", "id": "section1", "name": "Section", "style": [{"name": "a"}, {"name": "b"}],
               "screenElements": [
                 {"type": "ControlGrid", "id": "grid1", "name": "Grid", "row": [
                   {"id": "row1", "name": "Row", "style": [{"name": "c"}],
                    "cell": [{"type": "Control", "id": "control1", "elementRef": "field_x", "style": [{"name": "b"}]}]}]}]},
              {"type": "InlineRepeat", "id": "repeat1", "name": "Items", "groupRef": "group_items",
               "style": [{"name": "a"}], "headerStyle": [{"name": "b"}],
               "repeatOverviewColumn": [
                 {"type": "FieldBasedRepeatOverviewColumn", "id": "column1", "elementRef": "field_y", "headerStyle": [{"name": "c"}]}],
               "rowActionGroup": {"action": [
                 {"event": "remove", "scope": "ALWAYS", "style": [{"name": "a"}], "buttonStyling": {"style": [{"name": "b"}]}}]}}
            ]
          }]
        }
      }
      """;

  private static FormModelContent load() throws Exception {
    return JsonSettings.objectMapper.readValue(JSON, FormModel.class).getContent();
  }

  private static List<String> names(FormModelContent content) {
    List<String> names = new ArrayList<>();
    for (FormStyleReferences.Usage usage : FormStyleReferences.usages(content)) {
      usage.styles().forEach(style -> names.add(usage.ownerId() + ":" + style.getName()));
    }
    return names;
  }

  @Test
  void findsEveryStyleListWithItsOwner() throws Exception {
    List<String> found = new ArrayList<>(names(load()));
    List<String> expected = new ArrayList<>(List.of(
        "button1:a",
        "section1:a", "section1:b",
        "row1:c",
        "control1:b",
        "repeat1:a", "repeat1:b",
        "column1:c",
        // the row action has no id of its own: its style and its buttonStyling's style belong to the repeat
        "repeat1:a", "repeat1:b"));
    found.sort(null);
    expected.sort(null);

    // The model-level list (a, b, c) is not a usage.
    assertEquals(expected, found);
  }

  @Test
  void definedNamesAreTheModelLevelStyles() throws Exception {
    assertEquals(List.of("a", "b", "c"), List.copyOf(FormStyleReferences.definedNames(load())));
  }

  @Test
  void removingAStyleRemovesItEverywhereInPlace() throws Exception {
    FormModelContent content = load();
    List<Style> sectionStyles = FormStyleReferences.usages(content).stream()
        .filter(u -> "section1".equals(u.ownerId())).findFirst().orElseThrow().styles();

    int removed = FormStyleReferences.remove(content, "a");

    assertEquals(4, removed);
    assertTrue(FormStyleReferences.usages(content).stream().flatMap(u -> u.styles().stream()).noneMatch(s -> "a".equals(s.getName())));
    assertEquals(1, sectionStyles.size(), "the list object itself is kept, so an open editor still sees it");
    assertEquals("b", sectionStyles.get(0).getName());
    assertEquals(3, content.getStyles().size(), "the model-level list is left to the caller");
  }

  @Test
  void renamingAStyleRenamesItEverywhere() throws Exception {
    FormModelContent content = load();

    assertEquals(4, FormStyleReferences.rename(content, "b", "bee"));

    assertEquals(4, FormStyleReferences.usages(content).stream().flatMap(u -> u.styles().stream())
        .filter(s -> "bee".equals(s.getName())).count());
    assertTrue(FormStyleReferences.usages(content).stream().flatMap(u -> u.styles().stream()).noneMatch(s -> "b".equals(s.getName())));
  }

  @Test
  void aSwapIsAppliedInOnePass() throws Exception {
    FormModelContent content = load();
    Map<String, String> swap = new LinkedHashMap<>();
    swap.put("a", "b");
    swap.put("b", "a");

    FormStyleReferences.apply(content, swap);

    // section1 had [a, b]; a chain of renames would have turned both into the same name.
    List<Style> sectionStyles = FormStyleReferences.usages(content).stream()
        .filter(u -> "section1".equals(u.ownerId())).findFirst().orElseThrow().styles();
    assertEquals(List.of("b", "a"), sectionStyles.stream().map(Style::getName).toList());
  }

  @Test
  void aNullTargetRemovesWhileOthersRename() throws Exception {
    FormModelContent content = load();
    Map<String, String> changes = new LinkedHashMap<>();
    changes.put("a", null);
    changes.put("c", "cee");

    FormStyleReferences.apply(content, changes);

    List<String> remaining = FormStyleReferences.usages(content).stream().flatMap(u -> u.styles().stream()).map(Style::getName).toList();
    assertTrue(remaining.stream().noneMatch("a"::equals));
    assertTrue(remaining.contains("cee"));
    assertEquals(0, FormStyleReferences.rename(content, "does-not-exist", "x"));
    assertTrue(FormStyleReferences.usages(null).isEmpty());
  }

  // ---- applyPresetEdits: the model-level list was edited, the elements follow ----

  private static Map<Style, String> snapshot(FormModelContent content) {
    Map<Style, String> originals = new IdentityHashMap<>();
    content.getStyles().forEach(style -> originals.put(style, style.getName()));
    return originals;
  }

  private static Style style(FormModelContent content, String name) {
    return content.getStyles().stream().filter(style -> name.equals(style.getName())).findFirst().orElseThrow();
  }

  private static List<String> allUses(FormModelContent content) {
    return FormStyleReferences.usages(content).stream().flatMap(u -> u.styles().stream()).map(Style::getName).sorted().toList();
  }

  @Test
  void renamingAPresetRenamesItsUses() throws Exception {
    FormModelContent content = load();
    Map<Style, String> originals = snapshot(content);

    style(content, "b").setName("bee");

    assertEquals(4, FormStyleReferences.applyPresetEdits(content, originals));
    assertEquals(List.of("a", "a", "a", "a", "bee", "bee", "bee", "bee", "c", "c"), allUses(content));
  }

  @Test
  void deletingAPresetRemovesItsUses() throws Exception {
    FormModelContent content = load();
    Map<Style, String> originals = snapshot(content);

    content.getStyles().remove(style(content, "a"));

    assertEquals(4, FormStyleReferences.applyPresetEdits(content, originals));
    assertEquals(List.of("b", "b", "b", "b", "c", "c"), allUses(content));
  }

  @Test
  void clearingAPresetsNameRemovesItsUses() throws Exception {
    FormModelContent content = load();
    Map<Style, String> originals = snapshot(content);

    style(content, "c").setName("  ");

    assertEquals(2, FormStyleReferences.applyPresetEdits(content, originals));
    assertEquals(List.of("a", "a", "a", "a", "b", "b", "b", "b"), allUses(content));
  }

  @Test
  void aChainOfRenamesIsAppliedAsOne() throws Exception {
    FormModelContent content = load();
    Map<Style, String> originals = snapshot(content);

    // a -> b and b -> c at the same time: what used to be "a" must not end up as "c".
    style(content, "b").setName("c");
    Style first = style(content, "a");
    first.setName("b");

    FormStyleReferences.applyPresetEdits(content, originals);

    // The 4 uses of a became b, the 4 of b became c, and the 2 of c - a preset that was not touched - stay c.
    assertEquals(List.of("b", "b", "b", "b", "c", "c", "c", "c", "c", "c"), allUses(content));
  }

  @Test
  void aNameThatIsStillCarriedKeepsItsUses() throws Exception {
    FormModelContent content = load();
    Map<Style, String> originals = snapshot(content);

    // "a" is deleted and added again: the uses stay. A second entry named "b" doesn't change b either.
    content.getStyles().remove(style(content, "a"));
    Style again = new Style();
    again.setName("a");
    content.getStyles().add(again);
    Style duplicate = new Style();
    duplicate.setName("b");
    content.getStyles().add(duplicate);
    style(content, "c").setName("cee");

    FormStyleReferences.applyPresetEdits(content, originals);

    assertEquals(List.of("a", "a", "a", "a", "b", "b", "b", "b", "cee", "cee"), allUses(content));
  }

  @Test
  void nothingChangesWhenThePresetsWereNotEdited() throws Exception {
    FormModelContent content = load();

    assertEquals(0, FormStyleReferences.applyPresetEdits(content, snapshot(content)));
    assertEquals(10, allUses(content).size());
  }

  // The reflective walk must see exactly the style names the raw JSON of every fixture form model contains -
  // the guarantee that no element type with a style list is missed.
  @Test
  void seesEveryStyleUseInTheFixtureWorkspaces() throws Exception {
    int formModels = 0;
    int usesInJson = 0;
    int usesFound = 0;
    for (Path workspace : List.of(TestHelper.resolveTestingBasicDir(), TestHelper.resolveTestingAdvancedNewDir(),
        TestHelper.resolveTestingCommerceDir())) {
      for (Path file : jsonFiles(workspace)) {
        JsonNode tree = JsonSettings.objectMapper.readTree(Files.readString(file, StandardCharsets.UTF_8));
        JsonNode header = tree.get("header");
        if (header == null || !"form".equals(header.path("modelType").asString())) {
          continue;
        }
        formModels++;
        int inJson = countStyleUses(tree.get("content"), true);
        FormModel model = JsonSettings.objectMapper.readValue(Files.readString(file, StandardCharsets.UTF_8), FormModel.class);
        int found = FormStyleReferences.usages(model.getContent()).stream().mapToInt(u -> u.styles().size()).sum();
        assertEquals(inJson, found, "style uses in " + file);
        usesInJson += inJson;
        usesFound += found;
      }
    }
    assertTrue(formModels > 0, "no fixture form models found");
    assertTrue(usesInJson > 0, "the fixtures no longer contain any style use - this test would prove nothing");
    assertEquals(usesInJson, usesFound);
  }

  private static List<Path> jsonFiles(Path workspace) throws IOException {
    try (Stream<Path> walk = Files.walk(workspace)) {
      return walk.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".json")).sorted().toList();
    }
  }

  // Counts entries of every "style"/"headerStyle" array in the JSON, except the model-level "styles" list.
  private static int countStyleUses(JsonNode node, boolean top) {
    int count = 0;
    if (node.isObject()) {
      for (Map.Entry<String, JsonNode> entry : node.properties()) {
        if (("style".equals(entry.getKey()) || "headerStyle".equals(entry.getKey())) && entry.getValue().isArray()) {
          count += entry.getValue().size();
        }
        else {
          count += countStyleUses(entry.getValue(), false);
        }
      }
    }
    else if (node.isArray()) {
      for (JsonNode item : node) {
        count += countStyleUses(item, false);
      }
    }
    return count;
  }
}
