package de.a12.studio.models.relationshipmodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.ModelType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RelationshipModelLoadTest {

  @Test
  void loadsPersonCompanyRelationship() throws Exception {
    RelationshipModel model = ModelRoundTrip.load(getClass(), "/relationshipmodel/PersonCompany.json", RelationshipModel.class);

    assertEquals("PersonCompany", model.getId());
    assertEquals(ModelType.RELATIONSHIP, model.getModelType());
    assertEquals("4.0.0", model.getModelVersion());
    assertEquals(2, model.getModelReferences().size());
    assertEquals("Person_DM", model.getModelReferences().get(0).getReference());

    RelationshipModelContent content = model.getContent();
    assertNotNull(content);
    assertFalse(content.getDuplicatesAllowed());
    // The file has an explicit "linkDocumentModel": null which must be preserved as such.
    assertNotNull(content.getLinkDocumentModel());
    assertTrue(content.getLinkDocumentModel().isNull());
    assertNull(content.getLinkDocumentModelValue());

    assertEquals(2, content.getEntityCharacteristics().size());
    EntityCharacteristic person = content.getEntityCharacteristics().get(0);
    assertEquals("Person", person.getRole());
    assertEquals("Person_DM", person.getDocumentModel());
    assertFalse(person.getOrdered());
    assertEquals("Employees", person.getLabels().get(1).getText());
    assertTrue(person.getLinkConstraints().getMultiplicity().getUnbounded());
    assertNull(person.getLinkConstraints().getMultiplicity().getUpperLimit());

    EntityCharacteristic company = content.getEntityCharacteristics().get(1);
    assertEquals("Company", company.getRole());
    assertFalse(company.getLinkConstraints().getMultiplicity().getUnbounded());
    assertEquals(1, company.getLinkConstraints().getMultiplicity().getUpperLimit());
  }

  @Test
  void loadsRelationshipModelWithoutLinkDocumentModelKey() throws Exception {
    RelationshipModel model = ModelRoundTrip.load(getClass(), "/relationshipmodel/RelationshipModel.json", RelationshipModel.class);

    RelationshipModelContent content = model.getContent();
    // Key absent in the file: must stay absent, not become an explicit null.
    assertNull(content.getLinkDocumentModel());
    assertEquals(1, content.getEntityCharacteristics().size());
    assertEquals(123, content.getEntityCharacteristics().get(0).getLinkConstraints().getMultiplicity().getUpperLimit());
  }

  @Test
  void roundTripsPersonCompany() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/relationshipmodel/PersonCompany.json", RelationshipModel.class);
  }

  @Test
  void roundTripsRelationshipModel() throws Exception {
    ModelRoundTrip.assertRoundTrip(getClass(), "/relationshipmodel/RelationshipModel.json", RelationshipModel.class);
  }
}
