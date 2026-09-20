package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.validators.query.QueryFilterReferenceChecker.Models;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Semantic checks on a Query Language filter (see {@link QueryFilterReferenceChecker}): field paths against the
 * Document Model a scope is evaluated in, and relationship/role/link-model references of {@code Has(...)} with the
 * scope switches its constraints imply. Every filter here is syntactically valid, so anything reported is a
 * reference problem. The fixtures: {@code Ref_DM} (fields {@code /Root/Name}, {@code /Root/NoLabel} and the
 * non-indexed {@code /Root/Hidden}), {@code RefRef} (self-referencing Parent/Child, no link model), {@code
 * RefRefLinked} (same, link model {@code Ref_DM}), {@code RefRefLinkMissing} (link model that does not exist) and
 * {@code PersonRef} (PersonSide is a {@code Person_DM}, which is deliberately not part of the project here).
 */
class QueryFilterReferenceCheckerTest {

  private static final DocumentModel REF = TestModels.load("/documentmodel/Ref_DM.json", DocumentModel.class);
  private static final DocumentModel WITH_INCLUDE = TestModels.load("/documentmodel/RefWithInclude_DM.json", DocumentModel.class);
  private static final DocumentModel INCLUDED = TestModels.load("/documentmodel/RefIncluded_DM.json", DocumentModel.class);

  private final QueryFilterReferenceChecker checker = new QueryFilterReferenceChecker(new Models(
      List.of(REF, WITH_INCLUDE, INCLUDED),
      List.of(relationship("RefRef"), relationship("RefRefLinked"), relationship("RefRefLinkMissing"), relationship("PersonRef"))));

  private static RelationshipModel relationship(String id) {
    return TestModels.load("/relationshipmodel/" + id + ".json", RelationshipModel.class);
  }

  private List<String> check(String filter) {
    return checker.check(filter, REF);
  }

  @Test
  void validReferencesProduceNoMessages() {
    assertEquals(List.of(), check("[/Root/Name] == \"a\" and [/Root/NoLabel] ~ \"b\""));
    assertEquals(List.of(), check("Has(\"RefRef\", \"Child\", [/Root/Name] == \"a\")"));
    assertEquals(List.of(), check("Has(\"RefRefLinked\", \"Child\", Null, [/Root/Name] == \"a\")"));
    assertEquals(List.of(), check("Match([/Root/Name], \"abc\") or InRange([/Root/NoLabel], 1, 2)"));
  }

  @Test
  void blankAndSyntacticallyInvalidFiltersAreNotReportedHere() {
    assertEquals(List.of(), check(null));
    assertEquals(List.of(), check("  "));
    assertEquals(List.of(), check("[/Root/DoesNotExist] ==="));
  }

  @Test
  void reportsUnknownFieldNotAFieldAndNonIndexedFieldByName() {
    List<String> messages = check("[/Root/DoesNotExist] == \"a\" and [/Root] == \"b\" and [/Root/Hidden] == \"c\"");

    assertEquals(3, messages.size());
    assertTrue(messages.get(0).contains("\"/Root/DoesNotExist\"") && messages.get(0).contains("Ref_DM"), messages.get(0));
    assertTrue(messages.get(1).contains("\"/Root\"") && messages.get(1).contains("not a field"), messages.get(1));
    assertTrue(messages.get(2).contains("\"/Root/Hidden\"") && messages.get(2).contains("indexed = false"), messages.get(2));
  }

  @Test
  void findsFieldsInsideNegationsParenthesesAndFunctionArguments() {
    assertEquals(1, check("!([/Root/Nope] == \"a\")").size());
    assertEquals(1, check("([/Root/Nope] == \"a\" or [/Root/Name] == \"b\")").size());
    assertEquals(2, check("Match([/Root/Nope1], [/Root/Name], \"abc\") and InRange([/Root/Nope2], 1, 2)").size());
  }

  @Test
  void frameworkMetaPathsAreNotDocumentModelElements() {
    assertEquals(List.of(), check("[/__meta/docRef] == \"x\""));
  }

  @Test
  void followsAnIncludeIntoTheIncludedModel() {
    assertEquals(List.of(), checker.check("[/Included/IncludedField] == \"a\"", WITH_INCLUDE));
    assertEquals(1, checker.check("[/Included/Nope] == \"a\"", WITH_INCLUDE).size());
  }

  @Test
  void unknownRelationshipAndUnknownRoleAreReported() {
    List<String> unknownRelationship = check("Has(\"NoSuchRelationship\", \"Child\")");
    assertEquals(1, unknownRelationship.size());
    assertTrue(unknownRelationship.get(0).contains("NoSuchRelationship"), unknownRelationship.get(0));

    List<String> unknownRole = check("Has(\"RefRef\", \"Sibling\")");
    assertEquals(1, unknownRole.size());
    assertTrue(unknownRole.get(0).contains("Sibling") && unknownRole.get(0).contains("RefRef"), unknownRole.get(0));
  }

  @Test
  void aRoleOfTheCurrentDocumentModelIsNotReachableUnlessTheRelationshipIsSelfReferencing() {
    // PersonRef connects Person_DM and Ref_DM: from Ref_DM only the Person side can be traversed to.
    List<String> messages = check("Has(\"PersonRef\", \"RefSide\")");

    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("\"RefSide\"") && messages.get(0).contains("\"PersonSide\""), messages.get(0));
    // RefRef is self-referencing, so both of its roles are fine (see validReferencesProduceNoMessages).
    assertEquals(List.of(), check("Has(\"RefRef\", \"Parent\")"));
  }

  @Test
  void aRoleWhoseDocumentModelDoesNotExistIsReportedAndItsConstraintIsNotChecked() {
    List<String> messages = check("Has(\"PersonRef\", \"PersonSide\", [/Person/Anything] == \"a\")");

    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("Person_DM"), messages.get(0));
  }

  @Test
  void aConstraintIsCheckedInTheDocumentModelOfTheRole() {
    // /Person/Name would exist in Person_DM, but the Child role is a Ref_DM: /Root/Name is what resolves.
    assertEquals(1, check("Has(\"RefRef\", \"Child\", [/Person/Name] == \"a\")").size());
    assertEquals(List.of(), check("Has(\"RefRef\", \"Child\", [/Root/Name] == \"a\")"));
  }

  @Test
  void nestedHasCallsAreCheckedInTheirOwnScope() {
    List<String> messages = check("Has(\"RefRef\", \"Child\", Has(\"RefRef\", \"Parent\", [/Root/Nope] == \"a\") and [/Root/Name] == \"b\")");

    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("/Root/Nope"), messages.get(0));
  }

  @Test
  void aBrokenHopDoesNotHideProblemsOfHasCallsNestedInIt() {
    List<String> messages = check("Has(\"NoSuchRelationship\", \"Child\", [/Root/Skipped] == \"a\" and Has(\"AlsoNoSuchRelationship\", \"X\"))");

    assertEquals(2, messages.size());
    assertTrue(messages.get(0).contains("NoSuchRelationship"), messages.get(0));
    assertTrue(messages.get(1).contains("AlsoNoSuchRelationship"), messages.get(1));
  }

  @Test
  void aLinkConstraintNeedsALinkDocumentModelThatExistsAndIsCheckedAgainstIt() {
    List<String> noLinkModel = check("Has(\"RefRef\", \"Child\", Null, [/Root/Name] == \"a\")");
    assertEquals(1, noLinkModel.size());
    assertTrue(noLinkModel.get(0).contains("RefRef") && noLinkModel.get(0).contains("no link Document Model"), noLinkModel.get(0));

    List<String> missingLinkModel = check("Has(\"RefRefLinkMissing\", \"Child\", Null, [/Root/Name] == \"a\")");
    assertEquals(1, missingLinkModel.size());
    assertTrue(missingLinkModel.get(0).contains("NoSuchLink_DM"), missingLinkModel.get(0));

    List<String> unknownLinkField = check("Has(\"RefRefLinked\", \"Child\", Null, [/Root/Nope] == \"a\")");
    assertEquals(1, unknownLinkField.size());
    assertTrue(unknownLinkField.get(0).contains("/Root/Nope"), unknownLinkField.get(0));
  }

  @Test
  void aScopeWithoutADocumentModelSkipsFieldPathsButStillChecksRelationships() {
    List<String> messages = checker.check("[/Root/Nope] == \"a\" and Has(\"NoSuchRelationship\", \"Child\")", null);

    assertEquals(1, messages.size());
    assertTrue(messages.get(0).contains("NoSuchRelationship"), messages.get(0));
  }

  @Test
  void validatorReportsRootAndHopFiltersEachAgainstTheirOwnDocumentModel() {
    QueryModel model = TestModels.load("/querymodel/QueryFilterDefinitionReferenceValidator_invalid.json", QueryModel.class);
    List<ModelValidationError> errors = new QueryFilterDefinitionReferenceValidator().validate(model,
        TestModels.contextWithOtherModels(model, REF, relationship("RefRef")));

    // Root: unknown field. First hop: the non-indexed field. Second-level hop: unknown field. The sibling hop with
    // an unresolvable relationship is the link validator's business; its filter has no DM to be checked against.
    assertEquals(3, errors.size());
    assertEquals(QueryFilterDefinitionReferenceValidator.ELEMENT_ID, errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("/Root/DoesNotExist"), errors.get(0).message());
    assertEquals(QueryFilterDefinitionReferenceValidator.LINK_ELEMENT_ID, errors.get(1).elementId());
    assertTrue(errors.get(1).message().contains("/Root/Hidden"), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("/Root/AlsoMissing"), errors.get(2).message());
  }

  @Test
  void validatorIgnoresASyntacticallyInvalidFilterAndAQueryWithoutAResolvableTarget() {
    QueryModel invalidSyntax = TestModels.load("/querymodel/QueryFilterDefinitionSyntaxValidator_invalid.json", QueryModel.class);
    assertEquals(List.of(), new QueryFilterDefinitionReferenceValidator().validate(invalidSyntax,
        TestModels.contextWithOtherModels(invalidSyntax, REF)));

    QueryModel unresolved = TestModels.load("/querymodel/QueryFilterDefinitionReferenceValidator_invalid.json", QueryModel.class);
    // No Ref_DM in the project: nothing to resolve the root filter against, so nothing to report for it.
    List<ModelValidationError> errors = new QueryFilterDefinitionReferenceValidator().validate(unresolved,
        TestModels.context(unresolved));
    assertEquals(List.of(), errors);
  }
}
