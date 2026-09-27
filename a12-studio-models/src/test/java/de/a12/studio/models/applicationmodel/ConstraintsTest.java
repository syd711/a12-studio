package de.a12.studio.models.applicationmodel;

import org.junit.jupiter.api.Test;
import tools.jackson.databind.json.JsonMapper;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Pins the round-trip fix for gap 1 of "Application Model: gap review" in
 * {@code docs/sme-reference-comparison.md}: real SME {@code constraints} objects on a non-MasterDetail
 * {@code ViewAddDirective} carry arbitrary keys (e.g. Dashboard tile sizing/label config) that must survive a
 * load-then-save unharmed, even though the studio's editor only has UI for the MasterDetail shape.
 */
class ConstraintsTest {

  private static final JsonMapper mapper = JsonMapper.builder().build();

  @Test
  void roundTripsUnknownKeysAlongsideTypeAndPreferredWidth() {
    String json = "{\"type\":\"MasterDetail\",\"preferredWidth\":8,\"propertyA\":\"A\",\"propertyB\":\"B\"}";

    Constraints constraints = mapper.readValue(json, Constraints.class);
    assertEquals("MasterDetail", constraints.getType());
    assertEquals(8, constraints.getPreferredWidth());
    assertEquals("A", constraints.getExtras().get("propertyA"));
    assertEquals("B", constraints.getExtras().get("propertyB"));

    String reserialized = mapper.writeValueAsString(constraints);
    Constraints reloaded = mapper.readValue(reserialized, Constraints.class);
    assertEquals("MasterDetail", reloaded.getType());
    assertEquals(8, reloaded.getPreferredWidth());
    assertEquals("A", reloaded.getExtras().get("propertyA"));
    assertEquals("B", reloaded.getExtras().get("propertyB"));
  }

  @Test
  void roundTripsNonMasterDetailFreeFormShape() {
    // Mirrors a Dashboard tile's constraints - no "type"/"preferredWidth" at all, just free-form config
    // (SME's onConstraintTypeChangeMiddleware.ts never assumes the object has no other keys).
    String json = "{\"columns\":4,\"rows\":2,\"label\":[\"a\",\"b\"]}";

    Constraints constraints = mapper.readValue(json, Constraints.class);
    assertNull(constraints.getType());
    assertNull(constraints.getPreferredWidth());
    assertEquals(4, constraints.getExtras().get("columns"));
    assertEquals(2, constraints.getExtras().get("rows"));

    String reserialized = mapper.writeValueAsString(constraints);
    Constraints reloaded = mapper.readValue(reserialized, Constraints.class);
    assertEquals(Map.of("columns", 4, "rows", 2, "label", java.util.List.of("a", "b")), reloaded.getExtras());
  }
}
