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
 * Pins the whole Combination Model expansion (Addition, Selection and Decoration steps, several steps in one combination) as SME shows it. The golden
 * files are the output of the installed SME 13.0.2 backend ({@code POST /api/combination-model/expand}, recorded
 * 2026-10-02); the kernel facade must reproduce it exactly - elements, order, ids, every property, type
 * definitions, header and the joining warnings. Two SME-only additions are stripped from the golden files: the
 * {@code __meta} group and {@code header.modelReferences} (SME's own metadata enrichment, not the kernel).
 *
 * <p>{@code PersonSkills_Selection_Cm} (a Selection-only variant of the workspace's decoration combination) and
 * {@code PersonEmployeeFreelancer_Cm} (the Employee and the Freelancer addition in one combination) and
 * {@code PersonOverwrite_Cm} (an additive model that redefines the reference field {@code FirstName} with another label; SME and the kernel both
 * keep the reference's definition) and
 * {@code PersonGroups_Cm} (a DecorationForGroups step, with its own selection and decoration models) live
 * in the test resources because {@code testing/workspaces} is also the round-trip fixture set.
 */
class KernelCombinationParityTest {

  /** Combinations that live in the test resources instead of {@code testing/workspaces}. */
  private static final List<String> FIXTURES = List.of("PersonSkills_Selection_Cm", "PersonEmployeeFreelancer_Cm",
      "PersonOverwrite_Cm", "PersonOverwrite_Ad", "PersonGroups_Cm", "PersonGroups_Se", "PersonGroups_Dc");

  private static final ObjectMapper MAPPER = JsonMapper.builder().build();

  @ParameterizedTest
  @ValueSource(strings = {
      "PersonEmployee_Cm",           // Addition
      "PersonFreelancer_Cm",         // Addition
      "PersonSkills_Selection_Cm",   // Selection
      "PersonSkills_LinkFields_Cm",  // DecorationForFields (selection + decoration model)
      "PersonEmployeeFreelancer_Cm", // two Addition steps in one combination
      "PersonOverwrite_Cm",          // an additive model that redefines a reference field
      "PersonGroups_Cm"              // DecorationForGroups (selected group Person/Photo gets a PhotoNote sibling)
  })
  void kernelExpansionMatchesSme(String combinationId) throws IOException {
    JsonNode golden = resource("/sme-combination-golden/" + combinationId + ".json");
    KernelModelSource workspace = TestWorkspaces.source("advanced_new");
    KernelModelSource source = id -> FIXTURES.contains(id) ? text("/combination-fixtures/" + id + ".json") : workspace.load(id);

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
