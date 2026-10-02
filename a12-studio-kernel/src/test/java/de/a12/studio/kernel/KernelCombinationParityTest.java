package de.a12.studio.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Pins the whole Combination Model expansion (Addition, Selection and Decoration steps) as SME shows it. The golden
 * files are the output of the installed SME 13.0.2 backend ({@code POST /api/combination-model/expand}, recorded
 * 2026-10-02); the kernel facade must reproduce it exactly - elements, order, ids, every property, type
 * definitions, header and the joining warnings. Two SME-only additions are stripped from the golden files: the
 * {@code __meta} group and {@code header.modelReferences} (SME's own metadata enrichment, not the kernel).
 *
 * <p>{@code PersonSkills_Selection_Cm} is a Selection-only variant of the workspace's decoration combination; it lives
 * in the test resources because {@code testing/workspaces} is also the round-trip fixture set.
 */
class KernelCombinationParityTest {

  private static final ObjectMapper MAPPER = JsonMapper.builder().build();

  @ParameterizedTest
  @ValueSource(strings = {
      "PersonEmployee_Cm",           // Addition
      "PersonFreelancer_Cm",         // Addition
      "PersonSkills_Selection_Cm",   // Selection
      "PersonSkills_LinkFields_Cm"   // DecorationForFields (selection + decoration model)
  })
  void kernelExpansionMatchesSme(String combinationId) throws IOException {
    JsonNode golden = resource("/sme-combination-golden/" + combinationId + ".json");
    KernelModelSource workspace = TestWorkspaces.source("advanced_new");
    KernelModelSource source = id -> id.equals("PersonSkills_Selection_Cm")
        ? text("/combination-fixtures/PersonSkills_Selection_Cm.json") : workspace.load(id);

    KernelExpansion expansion = new KernelDocumentModelExpander().expand(combinationId, source);

    ObjectNode kernel = (ObjectNode) MAPPER.readTree(expansion.expandedJson());
    ((ObjectNode) kernel.get("header")).remove("modelReferences");
    assertEquals(golden.get("documentModel"), kernel, "expanded Document Model");

    List<String> warnings = expansion.findings().stream().map(f -> f.severity() + ": " + f.message()).sorted().toList();
    List<String> expectedWarnings = golden.get("warnings").valueStream().map(JsonNode::asString).toList();
    assertEquals(expectedWarnings, warnings, "joining warnings");
  }

  private static JsonNode resource(String path) throws IOException {
    return MAPPER.readTree(text(path));
  }

  private static String text(String path) {
    try (InputStream in = KernelCombinationParityTest.class.getResourceAsStream(path)) {
      assertNotNull(in, "missing test resource " + path);
      return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }
    catch (IOException e) {
      throw new java.io.UncheckedIOException(e);
    }
  }
}
