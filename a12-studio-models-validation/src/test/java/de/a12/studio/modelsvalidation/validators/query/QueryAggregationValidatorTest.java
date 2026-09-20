package de.a12.studio.modelsvalidation.validators.query;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.FieldType;
import de.a12.studio.models.querymodel.QueryAggregation;
import de.a12.studio.models.querymodel.QueryAggregationEntry;
import de.a12.studio.models.querymodel.QueryAggregationGroup;
import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.TestModels;
import de.a12.studio.modelsvalidation.services.QueryModelValidationService;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Runs against {@code /documentmodel/Aggregation_DM.json}: number/date/date-time/time/string fields, a type
 * definition, an {@code indexed = false} field, a repeatable and a non-repeatable group. */
class QueryAggregationValidatorTest {

  private static final DocumentModel DM = TestModels.load("/documentmodel/Aggregation_DM.json", DocumentModel.class);

  @Test
  void aValidAggregationHasNoFindings() {
    QueryModel query = query(group("/Contract/Type"), entry("sum", "/Contract/Liability"), entry("count", "/Contract/Type"),
        entry("max", "/Contract/Signed"), entry("min", "/Contract/SignedAt"), entry("max", "/Contract/Opening"),
        entry("avg", "/Contract/Amount"));

    assertEquals(List.of(), validate(query));
  }

  @Test
  void aQueryWithoutAggregationIsNotTouched() {
    QueryModel query = query();
    query.getContent().setAggregation(null);

    assertEquals(List.of(), validate(query));
  }

  @Test
  void aGroupFieldOfAnyTypeInANonRepeatableGroupIsFine() {
    assertEquals(List.of(), validate(query(group("/Contract/Details/Note"), entry("count", "/Contract/Type"))));
  }

  @Test
  void unknownFieldsAreNamedInTheMessage() {
    List<ModelValidationError> errors = validate(query(group("/Contract/Nope"), entry("sum", "/Contract/AlsoNope")));

    assertEquals(2, errors.size());
    assertTrue(errors.get(0).message().contains("\"/Contract/Nope\""), errors.get(0).message());
    assertEquals(QueryAggregationValidator.GROUP_ELEMENT_ID, errors.get(0).elementId());
    assertTrue(errors.get(1).message().contains("\"/Contract/AlsoNope\""), errors.get(1).message());
    assertEquals(QueryAggregationValidator.AGGREGATIONS_ELEMENT_ID, errors.get(1).elementId());
    assertTrue(errors.get(1).message().contains("Aggregation_DM"), errors.get(1).message());
  }

  @Test
  void aGroupIsNotAField() {
    List<ModelValidationError> errors = validate(query(group("/Contract/Details"), entry("count", "/Contract/Type")));

    assertEquals(1, errors.size());
    assertTrue(errors.get(0).message().contains("not a field"), errors.get(0).message());
  }

  @Test
  void anIndexedFalseFieldCannotBeGroupedOrAggregated() {
    List<ModelValidationError> errors = validate(query(group("/Contract/Secret"), entry("sum", "/Contract/Secret")));

    assertEquals(2, errors.size());
    errors.forEach(error -> assertTrue(error.message().contains("indexed = false"), error.message()));
  }

  @Test
  void aFieldInARepeatableGroupCannotBeGroupedOrAggregatedAndTheMessageNamesTheGroup() {
    List<ModelValidationError> errors = validate(query(group("/Contract/Items/Price"), entry("sum", "/Contract/Items/Price")));

    assertEquals(2, errors.size());
    errors.forEach(error -> {
      assertTrue(error.message().contains("\"/Contract/Items/Price\""), error.message());
      assertTrue(error.message().contains("\"/Contract/Items\""), error.message());
    });
  }

  @Test
  void functionMustBeAvailableForTheFieldType() {
    List<ModelValidationError> errors = validate(query(
        entry("sum", "/Contract/Type"),        // string
        entry("avg", "/Contract/Signed"),      // date
        entry("min", "/Contract/Type"),        // string
        entry("count", "/Contract/Type")));    // any type

    assertEquals(3, errors.size());
    assertTrue(errors.get(0).message().contains("\"sum\"") && errors.get(0).message().contains("\"/Contract/Type\"")
        && errors.get(0).message().contains("String") && errors.get(0).message().contains("Number"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("Date"), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("\"min\""), errors.get(2).message());
  }

  @Test
  void theTypeOfATypeDefinitionFieldIsResolved() {
    // Amount is a TypeDefType pointing at a NumberType: sum fits, so no finding; a Date function on it would too.
    assertEquals(List.of(), validate(query(entry("sum", "/Contract/Amount"))));
  }

  @Test
  void aMissingFunctionOrFieldIsReportedWithTheOtherHalf() {
    List<ModelValidationError> errors = validate(query(entry(null, "/Contract/Type"), entry("sum", null), entry("median", "/Contract/Liability")));

    assertEquals(3, errors.size());
    assertTrue(errors.get(0).message().contains("\"/Contract/Type\"") && errors.get(0).message().contains("needs a function"), errors.get(0).message());
    assertTrue(errors.get(1).message().contains("\"sum\"") && errors.get(1).message().contains("needs a field"), errors.get(1).message());
    assertTrue(errors.get(2).message().contains("\"median\""), errors.get(2).message());
  }

  @Test
  void aBlankGroupEntryIsReported() {
    List<ModelValidationError> errors = validate(query(group(" "), entry("count", "/Contract/Type")));

    assertEquals(1, errors.size());
    assertEquals(QueryAggregationValidator.GROUP_ELEMENT_ID, errors.get(0).elementId());
    assertTrue(errors.get(0).message().contains("group entry"), errors.get(0).message());
  }

  @Test
  void linksAndANonDocumentProjectionAreErrorsAndSortingIsOnlyAWarning() {
    QueryModel query = query(group("/Contract/Type"), entry("count", "/Contract/Type"));
    query.getContent().getLinks().add(new QueryLink());
    query.getContent().setProjectionName("summary");
    query.getContent().getSort().add(new QuerySort());

    List<ModelValidationError> errors = validate(query);

    assertEquals(3, errors.size());
    assertEquals(Severity.ERROR.name(), errors.get(0).severity());
    assertTrue(errors.get(0).message().contains("relationships"), errors.get(0).message());
    assertEquals(Severity.ERROR.name(), errors.get(1).severity());
    assertTrue(errors.get(1).message().contains("\"summary\""), errors.get(1).message());
    assertEquals(Severity.WARNING.name(), errors.get(2).severity());
    assertTrue(errors.get(2).message().contains("Sorting is ignored"), errors.get(2).message());
  }

  @Test
  void anAggregationThatOnlyGroupsIsAWarning() {
    List<ModelValidationError> errors = validate(query(group("/Contract/Type")));

    assertEquals(1, errors.size());
    assertEquals(Severity.WARNING.name(), errors.get(0).severity());
  }

  @Test
  void anUnresolvableTargetSkipsTheFieldChecks() {
    QueryModel query = query(group("/Anything"), entry("sum", "/Anything"));
    query.getContent().setTargetDocumentModel("Gone_DM");

    assertEquals(List.of(), validate(query));
  }

  @Test
  void metaPathsAreNotResolved() {
    assertEquals(List.of(), validate(query(entry("count", "/__meta/docRef"))));
  }

  @Test
  void theServiceRunsTheAggregationValidator() {
    QueryModel query = query(entry("sum", "/Contract/Type"));

    List<ModelValidationError> errors = new QueryModelValidationService().validate(query, TestModels.contextWithDocumentModels(query, DM));

    assertTrue(errors.stream().anyMatch(error -> error.message().contains("cannot be applied")), errors.toString());
  }

  // ---- QueryAggregationSupport ------------------------------------------------------------------------------

  @Test
  void candidatePathsAreTheNonRepeatableIndexedFieldsInTreeOrder() {
    assertEquals(List.of("/Contract/Type", "/Contract/Liability", "/Contract/Amount", "/Contract/Signed",
            "/Contract/SignedAt", "/Contract/Opening", "/Contract/Details/Note"),
        QueryAggregationSupport.candidatePaths(new ElementIndex(DM)));
  }

  @Test
  void functionsForATypeFollowDataServices() {
    ElementIndex index = new ElementIndex(DM);
    assertEquals(List.of("avg", "min", "max", "sum", "count"),
        QueryAggregationSupport.functionsFor(type(index, "/Contract/Liability")));
    assertEquals(List.of("min", "max", "count"), QueryAggregationSupport.functionsFor(type(index, "/Contract/Signed")));
    assertEquals(List.of("min", "max", "count"), QueryAggregationSupport.functionsFor(type(index, "/Contract/SignedAt")));
    assertEquals(List.of("min", "max", "count"), QueryAggregationSupport.functionsFor(type(index, "/Contract/Opening")));
    assertEquals(List.of("count"), QueryAggregationSupport.functionsFor(type(index, "/Contract/Type")));
    // unknown type: the benefit of the doubt
    assertEquals(QueryAggregationEntry.FUNCTIONS, QueryAggregationSupport.functionsFor(null));
    assertNull(QueryAggregationSupport.effectiveType(index, index.resolveAbsolutePath("/Contract/Details").orElseThrow()));
  }

  private static FieldType type(ElementIndex index, String path) {
    return QueryAggregationSupport.effectiveType(index, index.resolveAbsolutePath(path).orElseThrow());
  }

  // ---- helpers ----------------------------------------------------------------------------------------------

  private static List<ModelValidationError> validate(QueryModel query) {
    return new QueryAggregationValidator().validate(query, TestModels.contextWithDocumentModels(query, DM));
  }

  /** A query on {@code Aggregation_DM} with the given group ({@link QueryAggregationGroup}) and entry ({@link
   * QueryAggregationEntry}) items, in order. */
  private static QueryModel query(Object... items) {
    QueryAggregation aggregation = new QueryAggregation();
    for (Object item : items) {
      if (item instanceof QueryAggregationGroup group) {
        aggregation.getGroup().add(group);
      }
      else {
        aggregation.getAggregations().add((QueryAggregationEntry) item);
      }
    }
    QueryModelContent content = new QueryModelContent();
    content.setTargetDocumentModel("Aggregation_DM");
    content.setProjectionName("document");
    content.setAggregation(aggregation);
    QueryModel model = new QueryModel();
    model.setId("Q");
    model.setContent(content);
    return model;
  }

  private static QueryAggregationGroup group(String field) {
    return new QueryAggregationGroup(field);
  }

  private static QueryAggregationEntry entry(String function, String field) {
    QueryAggregationEntry entry = new QueryAggregationEntry();
    entry.setFunction(function);
    entry.setField(field);
    return entry;
  }
}
