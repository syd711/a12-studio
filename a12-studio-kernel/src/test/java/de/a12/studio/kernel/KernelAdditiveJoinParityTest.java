package de.a12.studio.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

/**
 * Pins the additive join as SME shows it. The golden files are the normalized output of the installed SME 13.0.2
 * backend ({@code POST /api/additive-model/join}, recorded 2026-10-02) for the same base + additive model pair
 * that the kernel facade joins through a Combination Model. Element order, element types and every element
 * property must be identical; only the element ids differ by design (SME restores the additive model's original
 * ids, the kernel prefixes them with {@code md5(additive model id)_}).
 */
class KernelAdditiveJoinParityTest {

  private static final ObjectMapper MAPPER = JsonMapper.builder().build();

  @ParameterizedTest
  @ValueSource(strings = {"PersonEmployee_Cm", "PersonFreelancer_Cm"})
  void kernelJoinMatchesSmeJoin(String combinationId) throws IOException {
    JsonNode golden = golden(combinationId);
    KernelExpansion expansion = new KernelDocumentModelExpander().expand(combinationId, TestWorkspaces.source("advanced_new"));
    assertFalse(expansion.hasErrors(), () -> "expansion findings: " + expansion.findings());
    JsonNode kernel = MAPPER.readTree(expansion.expandedJson());

    List<JsonNode> actual = new ArrayList<>();
    for (JsonNode group : kernel.get("content").get("modelRoot").get("rootGroups")) {
      flatten(group, "", actual);
    }
    List<JsonNode> expected = new ArrayList<>();
    golden.get("elements").forEach(expected::add);

    assertEquals(expected.size(), actual.size(), "element count");
    for (int i = 0; i < expected.size(); i++) {
      assertEquals(expected.get(i), actual.get(i), "element #" + i + " (" + expected.get(i).get("path").asString() + ")");
    }
    // path(), not get(): a model without type definitions has no such key on either side.
    assertEquals(golden.path("typeDefinitions"), kernel.get("content").path("typeDefinitions"), "type definitions");
  }

  @ParameterizedTest
  @ValueSource(strings = {"PersonEmployee_Cm"})
  void additiveElementsGetTheMd5PrefixedIds(String combinationId) throws IOException {
    KernelExpansion expansion = new KernelDocumentModelExpander().expand(combinationId, TestWorkspaces.source("advanced_new"));

    // md5("PersonEmployee_Ad"); the studio's own approximate merge (CombinedDocumentModelElements) uses the same prefix.
    assertTrue(expansion.expandedJson().contains("\"3ebb47b738ad9c6e3c36113ff04df00d_"),
        "additive element ids must be prefixed with md5 of the additive model id");
  }

  private static void flatten(JsonNode element, String parentPath, List<JsonNode> out) {
    String type = element.get("type").asString();
    String path = parentPath + "/" + element.get("name").asString();
    var entry = MAPPER.createObjectNode();
    entry.put("path", path);
    entry.put("type", type);
    var props = MAPPER.createObjectNode();
    JsonNode body = element.get(type);
    JsonNode children = null;
    if (body != null) {
      for (var field : body.properties()) {
        if (field.getKey().equals("elements")) {
          children = field.getValue();
        }
        else {
          props.set(field.getKey(), field.getValue());
        }
      }
    }
    entry.set("props", props);
    out.add(entry);
    if (children != null) {
      children.forEach(child -> flatten(child, path, out));
    }
  }

  private static JsonNode golden(String combinationId) throws IOException {
    try (InputStream in = KernelAdditiveJoinParityTest.class.getResourceAsStream("/sme-join-golden/" + combinationId + ".json")) {
      assertNotNull(in, "missing golden file for " + combinationId);
      return MAPPER.readTree(in);
    }
  }
}
