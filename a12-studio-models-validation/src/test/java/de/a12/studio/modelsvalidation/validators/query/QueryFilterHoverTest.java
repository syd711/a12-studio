package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterHover.Hover;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Hover documentation of a Query Language filter (see {@link QueryFilterHover}), on the fixtures of {@link QueryFilterCompletionTest}. */
class QueryFilterHoverTest {

  private static final DocumentModel REF = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);

  private final QueryFilterHover hover = new QueryFilterHover(new Models(List.of(REF),
      List.of(TestModels.load("/relationshipmodel/RefRef.json", RelationshipModel.class))));

  private Optional<Hover> at(String text, String marker) {
    return hover.hover(text, text.indexOf(marker), REF);
  }

  @Test
  void documentsAFunctionByItsCallee() {
    String text = "Has(\"RefRef\", \"Child\")";
    Hover result = at(text, "Has").orElseThrow();

    assertEquals("Has Function", result.title());
    assertEquals(0, result.start());
    assertEquals(3, result.end());
    assertTrue(result.sections().stream().anyMatch(section -> "Examples".equals(section.heading())));
  }

  @Test
  void documentsAnOperatorWithItsSignatures() {
    Hover result = at("[/Root/Name] == \"a\"", "==").orElseThrow();

    assertEquals("Equal Operator", result.title());
    assertTrue(result.sections().stream().anyMatch(section -> "Signatures".equals(section.heading())
        && section.lines().contains("StringField == String | Null")), result.toString());
  }

  @Test
  void documentsLogicalOperators() {
    assertEquals("And Operator", at("[/Root/Name] == \"a\" and [/Root/Name] == \"b\"", "and").orElseThrow().title());
  }

  @Test
  void describesAFieldReference() {
    Hover result = at("[/Root/Name] == \"a\"", "Root").orElseThrow();

    assertEquals("/Root/Name", result.title());
    assertTrue(result.sections().getFirst().lines().contains("Type: StringType"), result.toString());
  }

  @Test
  void describesTheRelationshipAndRoleArgumentsOfHas() {
    String text = "Has(\"RefRef\", \"Child\")";

    assertEquals("RefRef Relationship Model", at(text, "RefRef").orElseThrow().title());
    assertEquals("Child Entity Characteristic", at(text, "Child").orElseThrow().title());
  }

  @Test
  void saysNothingAboutWhitespaceOrPlainStrings() {
    assertTrue(at("[/Root/Name] == \"a\"", " ").isEmpty());
    assertTrue(at("[/Root/Name] == \"a\"", "\"a\"").isEmpty());
  }
}
