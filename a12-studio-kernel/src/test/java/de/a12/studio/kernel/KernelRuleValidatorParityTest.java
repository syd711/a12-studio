package de.a12.studio.kernel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Pins the per-rule and per-computation checks as SME's editors show them. The golden file holds what the installed
 * SME 13.0.2 backend answered (validate-condition, format-condition and computation-rule/validate, recorded
 * 2026-10-02) for its own expansion of the basic workspace's {@code Invoice_DM}, after one rule condition or one
 * computation text was replaced by a broken or valid variant. The kernel facade must report exactly the same
 * lines, columns and messages, so a kernel bump that moves a position or rewords a message fails here.
 *
 * <p>Line numbers are 1-based and columns 0-based, both within the text of the checked condition.
 */
class KernelRuleValidatorParityTest {

  private static final ObjectMapper MAPPER = JsonMapper.builder().build();
  private static JsonNode golden;
  private static String expandedInvoice;

  @BeforeAll
  static void load() throws IOException {
    try (InputStream in = KernelRuleValidatorParityTest.class.getResourceAsStream("/sme-rule-validation-golden/Invoice_DM.json")) {
      assertNotNull(in, "missing golden file");
      golden = MAPPER.readTree(in);
    }
    KernelExpansion expansion = new KernelDocumentModelExpander().expand("Invoice_DM", TestWorkspaces.source("basic"));
    assertTrue(!expansion.hasErrors(), () -> "expansion findings: " + expansion.findings());
    expandedInvoice = expansion.expandedJson();
  }

  static Stream<JsonNode> conditionCases() {
    return golden.get("conditions").valueStream();
  }

  static Stream<JsonNode> computationCases() {
    return golden.get("computations").valueStream();
  }

  static Stream<JsonNode> formatCases() {
    return golden.get("formats").valueStream();
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("conditionCases")
  void conditionProblemsMatchSme(JsonNode testCase) throws IOException {
    String ruleId = golden.get("rule").asString();
    ObjectNode model = (ObjectNode) MAPPER.readTree(expandedInvoice);
    if (!testCase.get("condition").isNull()) {
      ((ObjectNode) element(model, ruleId).get("Rule")).put("errorCondition", testCase.get("condition").asString());
    }

    List<KernelProblem> actual = new KernelRuleValidator(MAPPER.writeValueAsString(model)).validateCondition(ruleId);

    List<KernelProblem> expected = new ArrayList<>();
    testCase.get("errors").forEach(e -> expected.add(new KernelProblem(e.get("line").asInt(), e.get("startCol").asInt(),
        e.get("endCol").asInt(), e.get("message").asString())));
    assertEquals(expected, actual, testCase.get("name").asString());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("computationCases")
  void computationProblemsMatchSme(JsonNode testCase) throws IOException {
    String computationId = golden.get("computation").asString();
    ObjectNode model = (ObjectNode) MAPPER.readTree(expandedInvoice);
    if (!testCase.get("alternative").isNull()) {
      ((ObjectNode) element(model, computationId).get("Computation").get("computationAlternatives").get(testCase.get("alternative").asInt()))
          .put(testCase.get("part").asString(), testCase.get("text").asString());
    }

    KernelComputationResult actual = new KernelRuleValidator(MAPPER.writeValueAsString(model)).validateComputation(computationId);

    JsonNode result = testCase.get("result");
    List<String> expectedSemantic = result.get("semanticErrors").valueStream().map(JsonNode::asString).toList();
    List<KernelComputationProblem> expected = new ArrayList<>();
    result.get("errors").forEach(e -> expected.add(new KernelComputationProblem(
        KernelComputationProblem.Part.valueOf(e.get("conditionType").asString().replaceAll("([A-Z])", "_$1").toUpperCase()),
        e.get("alternativeIndex").asInt(), e.get("line").asInt(), e.get("startCol").asInt(), e.get("endCol").asInt(),
        e.get("message").asString())));
    assertEquals(expectedSemantic, actual.semanticErrors(), testCase.get("name").asString() + " (semantic)");
    assertEquals(expected, actual.parserErrors(), testCase.get("name").asString());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("formatCases")
  void formattingMatchesSme(JsonNode testCase) throws IOException {
    String ruleId = golden.get("rule").asString();
    ObjectNode model = (ObjectNode) MAPPER.readTree(expandedInvoice);
    if (!testCase.get("condition").isNull()) {
      ((ObjectNode) element(model, ruleId).get("Rule")).put("errorCondition", testCase.get("condition").asString());
    }

    String actual = new KernelRuleValidator(MAPPER.writeValueAsString(model)).formatCondition(ruleId);

    assertEquals(testCase.get("formatted").asString(), actual, testCase.get("name").asString());
  }

  @Test
  void aModelWithUnexpandedIncludesIsRefused() throws IOException {
    KernelRuleValidator validator = new KernelRuleValidator(TestWorkspaces.read("basic/models/Invoice_DM.json"));

    assertThrows(KernelException.class, () -> validator.validateCondition("rule_that_does_not_matter"));
  }

  @Test
  void unknownIdsAndWrongElementTypesAreRejected() {
    KernelRuleValidator validator = new KernelRuleValidator(expandedInvoice);

    assertThrows(KernelException.class, () -> validator.validateCondition("no_such_id"));
    assertThrows(KernelException.class, () -> validator.validateComputation(golden.get("rule").asString()));
    assertThrows(KernelException.class, () -> validator.validateCondition(golden.get("computation").asString()));
  }

  private static JsonNode element(JsonNode model, String id) {
    for (JsonNode group : model.get("content").get("modelRoot").get("rootGroups")) {
      JsonNode found = find(group, id);
      if (found != null) {
        return found;
      }
    }
    throw new IllegalStateException("no element " + id);
  }

  private static JsonNode find(JsonNode node, String id) {
    if (id.equals(node.get("id").asString())) {
      return node;
    }
    JsonNode children = node.path(node.get("type").asString()).path("elements");
    for (JsonNode child : children) {
      JsonNode found = find(child, id);
      if (found != null) {
        return found;
      }
    }
    return null;
  }
}
