package de.a12.studio.models.util;

import de.a12.studio.models.querymodel.QueryLink;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.QuerySort;
import de.a12.studio.models.querymodel.operator.ExactMatchOperator;
import de.a12.studio.models.querymodel.operator.HasOperator;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Renaming a model file rewrites the Query Models that refer to it by id (see {@link ModelReferenceRewriter}):
 * the target Document Model, every relationship a hop / sort entry / {@code has} operator goes through (nested
 * hops included) and - since a filter is Query Language text, not JSON - the relationship named by a {@code
 * Has("<relationship>", ...)} call. The role and the field paths are not ids and stay.
 */
class ModelReferenceRewriterQueryTest {

  private static final String ROOT_FILTER = "[/Person/Name] == \"a\" and "
      + "Has(\"Owns\", \"Order\", Has(\"Owns\", \"Owner\", [/Person/Name] == \"b\"), Null)";

  @Test
  void aRenamedRelationshipIsFollowedEverywhereAQueryNamesIt() {
    QueryModel query = query();

    boolean changed = ModelReferenceRewriter.rewriteReferences(query, Map.of("Owns", "Ownership"));

    assertTrue(changed);
    QueryModelContent content = query.getContent();
    assertEquals("Person_DM", content.getTargetDocumentModel());
    assertEquals("Ownership", content.getSort().get(0).getRelationshipModel());
    assertEquals("Owner", content.getSort().get(0).getTargetRole());
    assertEquals("Ownership", ((HasOperator) content.getConstraint()).getRelationshipModel());
    assertEquals("Ownership", content.getLinks().get(0).getRelationshipModel());
    assertEquals("Ownership", content.getLinks().get(0).getLinks().get(0).getRelationshipModel());
    assertEquals("[/Person/Name] == \"a\" and "
        + "Has(\"Ownership\", \"Order\", Has(\"Ownership\", \"Owner\", [/Person/Name] == \"b\"), Null)",
        content.getFilterDefinition());
    assertEquals("Has(\"Ownership\", \"Order\", [/Person/Name] == \"c\")",
        content.getLinks().get(0).getFilterDefinition());
    assertEquals("Has(\"Other\", \"Order\", [/Person/Name] == \"c\")",
        content.getLinks().get(0).getLinks().get(0).getFilterDefinition());
  }

  @Test
  void aRenamedDocumentModelIsFollowedInTheTargetAndNothingElseIsTouched() {
    QueryModel query = query();

    boolean changed = ModelReferenceRewriter.rewriteReferences(query, Map.of("Person_DM", "Human_DM"));

    assertTrue(changed);
    assertEquals("Human_DM", query.getContent().getTargetDocumentModel());
    assertEquals(ROOT_FILTER, query.getContent().getFilterDefinition());
    assertEquals("Owns", query.getContent().getLinks().get(0).getRelationshipModel());
  }

  @Test
  void aFilterThatIsNotValidQueryLanguageOrNamesNoRenamedModelIsLeftAlone() {
    QueryModel query = query();
    query.getContent().setFilterDefinition("Has(\"Owns\", \"Order\", [/Person/Name] = 1");

    boolean changed = ModelReferenceRewriter.rewriteReferences(query, Map.of("Unrelated", "Renamed"));

    assertFalse(changed);
    assertEquals("Has(\"Owns\", \"Order\", [/Person/Name] = 1", query.getContent().getFilterDefinition());

    // Invalid text is skipped even when the id it names is renamed: there is no parse tree to find the literal in.
    assertTrue(ModelReferenceRewriter.rewriteReferences(query, Map.of("Owns", "Ownership")));
    assertEquals("Has(\"Owns\", \"Order\", [/Person/Name] = 1", query.getContent().getFilterDefinition());
  }

  /** Target Person_DM; a sort, a {@code has} constraint, and a hop with a nested hop, all through "Owns". */
  private static QueryModel query() {
    QueryModelContent content = new QueryModelContent();
    content.setTargetDocumentModel("Person_DM");
    content.setFilterDefinition(ROOT_FILTER);

    QuerySort sort = new QuerySort();
    sort.setRelationshipModel("Owns");
    sort.setTargetRole("Owner");
    sort.getSortBy().setField("/Person/Name");
    content.getSort().add(sort);

    ExactMatchOperator inner = new ExactMatchOperator();
    inner.setField("/Person/Name");
    HasOperator has = new HasOperator();
    has.setRelationshipModel("Owns");
    has.setTargetRole("Owner");
    has.setConstraint(inner);
    content.setConstraint(has);

    QueryLink hop = new QueryLink();
    hop.setRelationshipModel("Owns");
    hop.setTargetRole("Order");
    hop.setFilterDefinition("Has(\"Owns\", \"Order\", [/Person/Name] == \"c\")");
    QueryLink nested = new QueryLink();
    nested.setRelationshipModel("Owns");
    nested.setTargetRole("Owner");
    nested.setFilterDefinition("Has(\"Other\", \"Order\", [/Person/Name] == \"c\")");
    hop.getLinks().add(nested);
    content.getLinks().add(hop);

    QueryModel model = new QueryModel();
    model.setId("Q");
    model.setContent(content);
    return model;
  }
}
