package de.a12.studio.models.formmodel;

import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

/**
 * A {@link Binding} only models the relationship linkage, but SME's I_Binding carries UI-component
 * configuration too; that must survive a load-then-save even though the editor cannot change it.
 */
class BindingRoundTripTest {

  @Test
  void bindingKeepsKeysItDoesNotModel() throws Exception {
    String json = """
        {
          "type": "Binding",
          "id": "binding_1",
          "binding": {
            "type": "relationship",
            "elementId": "el_1",
            "uiComponent": "dual_pane",
            "details": {
              "name": "Persons",
              "relationshipName": "PersonCompany",
              "targetRole": "Person",
              "metaInformation": {"version": "1", "generator": "sme"},
              "selection": {"mode": "dropdown", "editModal": {"enabled": true, "sizes": [1, 2]}},
              "childActivities": true
            }
          }
        }""";

    Binding binding = assertInstanceOf(Binding.class, JsonSettings.objectMapper.readValue(json, ScreenElement.class));
    assertEquals("PersonCompany", binding.getBinding().getDetails().getRelationshipName());

    JsonNode written = JsonSettings.objectMapper.readTree(JsonSettings.objectMapper.writeValueAsString(binding));
    JsonNode expected = JsonSettings.objectMapper.readTree(json);
    assertEquals(expected.get("binding"), written.get("binding"));
  }
}
