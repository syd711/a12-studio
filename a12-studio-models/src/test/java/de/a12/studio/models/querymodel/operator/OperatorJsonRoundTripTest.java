package de.a12.studio.models.querymodel.operator;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.StringNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * Verifies the {@link Operator} tagged union round-trips the real-world constraint JSON shapes documented in the
 * platform's Data Services API doc (data_services-dataservices-documentation-src.md) and SME's own
 * HighExperienceInterns_QeM.json example fixture - see docs/sme-reference-comparison.md "Query Model" section.
 */
class OperatorJsonRoundTripTest {

  @ParameterizedTest
  @ValueSource(strings = {
      "/querymodel/operator/and-double_range-exact_match-has.json",
      "/querymodel/operator/has-with-nested-constraints.json",
      "/querymodel/operator/or-not-exact_match.json",
      "/querymodel/operator/ranges-and-search.json"
  })
  void roundTripsWithoutLosingOrAlteringAnyField(String resourcePath) throws Exception {
    String original = ModelRoundTrip.readResource(getClass(), resourcePath);
    Operator operator = JsonSettings.objectMapper.readValue(original, Operator.class);
    String resaved = JsonSettings.objectMapper.writeValueAsString(operator);

    JsonNode originalTree = JsonSettings.objectMapper.readTree(original);
    JsonNode resavedTree = JsonSettings.objectMapper.readTree(resaved);
    assertEquals(originalTree, resavedTree, "Re-serialized operator must be semantically identical to " + resourcePath);
  }

  @Test
  void deserializesEachSubtypeToItsConcreteJavaClass() throws Exception {
    Operator root = JsonSettings.objectMapper.readValue(
        ModelRoundTrip.readResource(getClass(), "/querymodel/operator/has-with-nested-constraints.json"), Operator.class);

    AndOperator and = assertInstanceOf(AndOperator.class, root);
    assertEquals(2, and.getOperands().size());

    ExactMatchOperator name = assertInstanceOf(ExactMatchOperator.class, and.getOperands().get(0));
    assertEquals("/BusinessPartner/Name", name.getField());
    assertEquals("Tomas", name.getValue().asString());

    HasOperator has = assertInstanceOf(HasOperator.class, and.getOperands().get(1));
    assertEquals("PolicyHolder", has.getRelationshipModel());
    assertEquals("Contract", has.getTargetRole());

    ExactMatchOperator insurerName = assertInstanceOf(ExactMatchOperator.class, has.getConstraint());
    assertEquals("/HomeInsurance/Insurer/Name", insurerName.getField());
    assertEquals("ING", insurerName.getValue().asString());

    UndefinedMatchOperator terminatedAt = assertInstanceOf(UndefinedMatchOperator.class, has.getLinkDocumentConstraint());
    assertEquals("/InsuranceLinkFields/TerminatedAt", terminatedAt.getField());
  }

  @Test
  void discriminatorIsSetByTheConstructorAndSerializedExactlyOnce() throws Exception {
    ExactMatchOperator operator = new ExactMatchOperator();
    operator.setField("/Fields/sport");
    operator.setValue(StringNode.valueOf("basketball"));

    assertEquals("exact_match", operator.getOperator());

    JsonNode tree = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(operator));
    assertEquals("exact_match", tree.get("operator").asString());
    assertEquals(3, tree.propertyNames().size(), "operator must appear exactly once, not duplicated by visible=true");
  }
}
