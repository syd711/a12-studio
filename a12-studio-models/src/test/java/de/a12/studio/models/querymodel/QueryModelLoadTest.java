package de.a12.studio.models.querymodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class QueryModelLoadTest {

  @Test
  void loadsQueryModel() throws Exception {
    QueryModel model = ModelRoundTrip.load(getClass(), "/querymodel/QueryModel.json", QueryModel.class);

    assertEquals("QueryModel", model.getId());
    assertEquals(ModelType.QUERY, model.getModelType());
    assertEquals(1, model.getModelReferences().size());
    assertEquals("Person_DM", model.getModelReferences().get(0).getReference());
    assertEquals("document-model-for-query", model.getModelReferences().get(0).getPurpose());

    QueryModelContent content = model.getContent();
    assertNotNull(content);
    assertEquals("document", content.getProjectionName());
    assertEquals("Person_DM", content.getTargetDocumentModel());
    assertEquals(4, content.getFields().size());
    assertEquals("/People/Addresses/Street", content.getFields().get(0));
    assertNull(content.getFilterDefinition());
    assertNull(content.getAggregateResults());

    assertNotNull(content.getPaging());
    assertEquals(10, content.getPaging().getPageSize());
    assertEquals(1, content.getPaging().getPageNumber());

    assertEquals(1, content.getSort().size());
    QuerySort sort = content.getSort().get(0);
    assertEquals("PersonCompany", sort.getRelationshipModel());
    assertEquals("Company", sort.getTargetRole());
    assertNotNull(sort.getSortBy());
    assertEquals("/Company/CompanyDetails/CompanyLogo/original_filename", sort.getSortBy().getField());
    assertEquals(QuerySortBy.DIRECTION_ASC, sort.getSortBy().getDirection());
    assertEquals(QuerySortBy.NULLS_FIRST, sort.getSortBy().getNullHandling());
    assertFalse(sort.getSortBy().getIgnoreCase());
  }

  @Test
  void newQuerySortDefaultsToAnEmptySortByInsteadOfNull() {
    assertNotNull(new QuerySort().getSortBy());
  }

  @Test
  void roundTripsQueryModel() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/querymodel/QueryModel.json", QueryModel.class);
  }

  @Test
  void loadsQueryModelWithNestedLinks() throws Exception {
    QueryModel model = ModelRoundTrip.load(getClass(), "/querymodel/QueryModelWithLinks.json", QueryModel.class);

    QueryModelContent content = model.getContent();
    assertEquals(1, content.getLinks().size());

    QueryLink companyLink = content.getLinks().get(0);
    assertEquals("PersonCompany", companyLink.getRelationshipModel());
    assertEquals("Company", companyLink.getTargetRole());
    assertEquals(1, companyLink.getMaxDepth());
    assertEquals("/AdditionalFields/Position", companyLink.getLinkDocumentFields().get(0));
    assertEquals("/Company/Addresses/AddressType", companyLink.getFields().get(0));

    assertEquals(1, companyLink.getLinks().size());
    QueryLink industryLink = companyLink.getLinks().get(0);
    assertEquals("CompanyIndustry", industryLink.getRelationshipModel());
    assertEquals("Industry", industryLink.getTargetRole());
    assertEquals("/Industry/Name", industryLink.getFields().get(0));
    assertNull(industryLink.getMaxDepth());
    assertTrue(industryLink.getLinks().isEmpty());
  }

  @Test
  void roundTripsQueryModelWithNestedLinks() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/querymodel/QueryModelWithLinks.json", QueryModel.class);
  }

  @Test
  void newQueryModelHasNoLinks() {
    assertTrue(new QueryModelContent().getLinks().isEmpty());
  }
}
