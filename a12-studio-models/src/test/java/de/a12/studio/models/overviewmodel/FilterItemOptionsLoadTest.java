package de.a12.studio.models.overviewmodel;

import de.a12.studio.models.ModelRoundTrip;
import de.a12.studio.models.util.JsonSettings;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Round-trips the type-specific {@link FilterItemOptions} fields ({@link FilterItemOptions#getViewMode()},
 * {@link FilterItemOptions#getRanges()}, {@link FilterItemOptions#getPeriods()}) and {@link
 * FilterItem#getPreferFilterBar()} against real fixtures copied verbatim from {@code
 * testing/workspaces/advanced_new/models/10_People} (see each test's comment for the source file).
 */
class FilterItemOptionsLoadTest {

  @Test
  void roundTripsPreferFilterBarStringViewModeEnumerationViewModeAndDateTimeRangesAndPeriods() throws Exception {
    // Person_Ov.json - only the filterItems are compared (not the whole model), since this fixture's
    // "MetaData" group label carries a "technical_defaultValue" key that the shared Label class doesn't model
    // (a pre-existing, unrelated gap - see TODO.md's Overview Model section) and would otherwise fail a
    // whole-content round-trip for reasons that have nothing to do with FilterItemOptions.
    assertFilterItemsRoundTrip("/overviewmodel/PersonWithFilterViewModeAndPeriods_Ov.json");

    OverviewModel model = ModelRoundTrip.load(getClass(), "/overviewmodel/PersonWithFilterViewModeAndPeriods_Ov.json", OverviewModel.class);
    List<FilterGroup> groups = model.getContent().getConfiguration().getNewFilterConfiguration().getFilterGroups();

    FilterItem plainStringItem = groups.get(0).getFilterItems().get(0);
    assertTrue(plainStringItem.getPreferFilterBar());

    FilterItem listViewModeStringItem = groups.get(0).getFilterItems().get(1);
    assertEquals("string", listViewModeStringItem.getType());
    assertEquals("list", listViewModeStringItem.getOptions().getViewMode());

    FilterItem enumerationItem = groups.get(1).getFilterItems().get(0);
    assertEquals("enumeration", enumerationItem.getType());
    assertEquals("compact", enumerationItem.getOptions().getViewMode());

    FilterItem dateTimeItem = groups.get(2).getFilterItems().get(1);
    assertEquals("dateTime", dateTimeItem.getType());
    List<FilterOptionToggle> ranges = dateTimeItem.getOptions().getRanges();
    assertEquals(4, ranges.size());
    assertEquals("fromTo", ranges.get(0).getOption());
    assertTrue(ranges.get(0).getDefaultOption());
    List<FilterOptionToggle> periods = dateTimeItem.getOptions().getPeriods();
    assertEquals(6, periods.size());
    assertEquals("date", periods.get(0).getOption());
    assertTrue(periods.get(0).getDefaultOption());
    assertEquals("month", periods.get(5).getOption());
    assertNull(periods.get(5).getDefaultOption());
  }

  @Test
  void roundTripsNumberRanges() throws Exception {
    // PersonFreelancer_Ov.json
    assertFilterItemsRoundTrip("/overviewmodel/PersonFreelancerWithFilterRanges_Ov.json");

    OverviewModel model = ModelRoundTrip.load(getClass(), "/overviewmodel/PersonFreelancerWithFilterRanges_Ov.json", OverviewModel.class);
    List<FilterGroup> groups = model.getContent().getConfiguration().getNewFilterConfiguration().getFilterGroups();

    FilterItem numberItem = groups.get(1).getFilterItems().get(0);
    assertEquals("number", numberItem.getType());
    List<FilterOptionToggle> ranges = numberItem.getOptions().getRanges();
    assertEquals(4, ranges.size());
    assertEquals("fromTo", ranges.get(0).getOption());
    assertTrue(ranges.get(0).getDefaultOption());
  }

  /** Compares every {@code filterGroups[].filterItems} entry (not the whole model) between the fixture and a
   * load/save round trip of it, as a {@link FilterItemOptions}-focused alternative to {@link
   * ModelRoundTrip#assertRoundTrip}. */
  private void assertFilterItemsRoundTrip(String resourcePath) throws Exception {
    String original = ModelRoundTrip.readResource(getClass(), resourcePath);
    OverviewModel model = JsonSettings.objectMapper.readValue(original, OverviewModel.class);
    String resaved = JsonSettings.objectMapper.writeValueAsString(model);

    assertEquals(allFilterItems(original), allFilterItems(resaved), "Re-serialized filter items must be semantically identical to " + resourcePath);
  }

  private static JsonNode allFilterItems(String json) {
    JsonNode root = JsonSettings.objectMapper.readTree(json);
    ArrayNode allItems = JsonSettings.objectMapper.createArrayNode();
    for (JsonNode group : root.at("/content/configuration/newFilterConfiguration/filterGroups")) {
      allItems.addAll((ArrayNode) group.get("filterItems"));
    }
    return allItems;
  }
}
