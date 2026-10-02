package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterCompletion.Proposal;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterCompletion.Result;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Context-aware completion of a Query Language filter (see {@link QueryFilterCompletion}), on the fixtures of
 * {@link QueryFilterReferenceCheckerTest}: {@code Ref_DM} (String fields {@code /Root/Name}, {@code
 * /Root/NoLabel}), the self-referencing {@code RefRef} (roles Parent/Child) and {@code PersonRef} (PersonSide is
 * a {@code Person_DM}, RefSide a {@code Ref_DM}).
 */
class QueryFilterCompletionTest {

  private static final DocumentModel REF = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);

  private final QueryFilterCompletion completion = new QueryFilterCompletion(new Models(List.of(REF),
      List.of(relationship("RefRef"), relationship("PersonRef"), relationship("RefRefLinked"))));

  private static RelationshipModel relationship(String id) {
    return TestModels.load("/relationshipmodel/" + id + ".json", RelationshipModel.class);
  }

  private Optional<Result> at(String text) {
    return completion.complete(text, text.length(), REF);
  }

  private static List<String> labels(Optional<Result> result) {
    return result.map(r -> r.proposals().stream().map(Proposal::label).toList()).orElse(List.of());
  }

  @Test
  void proposesNothingForAnEmptyFilter() {
    assertTrue(at("").isEmpty());
  }

  @Test
  void proposesFunctionsWhileTypingTheStartOfAnExpression() {
    Result result = at("Ha").orElseThrow();

    assertEquals(List.of("Has"), result.proposals().stream().map(Proposal::label).toList());
    assertEquals(0, result.replaceStart());
    assertEquals(2, result.replaceEnd());
  }

  @Test
  void proposesFunctionsAfterAndOrAndOpeningParenthesis() {
    assertTrue(labels(at("[/Root/Name] == \"a\" and ")).containsAll(List.of("Match", "Has", "InRange")));
    assertTrue(labels(at("!(")).containsAll(List.of("Match", "Has", "InRange")));
  }

  @Test
  void proposesTheOperatorsOfTheFieldsType() {
    List<String> operators = labels(at("[/Root/Name] "));

    assertTrue(operators.containsAll(List.of("==", "!=", "~", "!~")), operators.toString());
    assertFalse(operators.contains(">="), "a String field cannot be compared with >=: " + operators);
  }

  @Test
  void filtersOperatorsByWhatIsTypedAndReplacesIt() {
    Result result = at("[/Root/Name] !").orElseThrow();

    assertEquals(List.of("!=", "!~"), result.proposals().stream().map(Proposal::label).toList());
    assertEquals("[/Root/Name] ".length(), result.replaceStart());
  }

  @Test
  void proposesValuesFittingTheOperatorAndFieldType() {
    List<Proposal> values = at("[/Root/Name] == ").orElseThrow().proposals();

    assertTrue(values.stream().anyMatch(p -> p.label().equals("String") && p.insertText().equals("\"$0\"")));
    assertTrue(values.stream().anyMatch(p -> p.label().equals("Null")));
    assertFalse(values.stream().anyMatch(p -> p.label().equals("Date")), "a String field takes no Date");
  }

  @Test
  void proposesAndOrAfterACompleteExpression() {
    assertEquals(List.of("and", "or"), labels(at("[/Root/Name] == \"a\" ")));
    assertEquals(List.of("and", "or"), labels(at("(Match([/Root/Name], \"a\")) ")));
  }

  @Test
  void proposesNothingInsideAConstructorsArguments() {
    assertTrue(at("[/Root/Name] == Date(2020, ").isEmpty());
  }

  @Test
  void proposesTheRelationshipsConnectedToTheCurrentDocumentModelInsideHas() {
    List<String> relationships = labels(at("Has("));

    assertTrue(relationships.containsAll(List.of("\"RefRef\"", "\"PersonRef\"")), relationships.toString());
  }

  @Test
  void proposesTheRolesReachableThroughTheChosenRelationship() {
    // self-referencing: every role; otherwise the roles of the *other* Document Models
    assertEquals(List.of("\"Parent\"", "\"Child\""), labels(at("Has(\"RefRef\", ")));
    assertEquals(List.of("\"PersonSide\""), labels(at("Has(\"PersonRef\", ")));
    assertTrue(at("Has(\"Unknown\", ").isEmpty());
  }

  @Test
  void completesNamesInsideAnOpenStringAndReplacesTheQuotes() {
    String text = "Has(\"Ref\")";
    Result result = completion.complete(text, "Has(\"Ref".length(), REF).orElseThrow();

    assertEquals(List.of("\"RefRef\"", "\"RefRefLinked\""), result.proposals().stream().map(Proposal::label).toList());
    assertEquals("Has(".length(), result.replaceStart());
    assertEquals(text.length() - 1, result.replaceEnd(), "the closing quote is replaced too");
  }

  @Test
  void proposesFunctionsInTheConstraintArgumentsOfHas() {
    assertTrue(labels(at("Has(\"RefRef\", \"Child\", ")).containsAll(List.of("Match", "Has", "Null")));
  }

  @Test
  void resolvesTheFieldsOfANestedConstraintInTheRolesDocumentModel() {
    assertTrue(labels(at("Has(\"RefRef\", \"Child\", [/Root/Name] ")).contains("=="));
  }

  @Test
  void proposesNothingForAnUnknownScope() {
    assertTrue(completion.complete("[/Root/Name] ", "[/Root/Name] ".length(), null).isPresent(),
        "without a scope the field type is unknown, so every operator is offered");
  }
}
