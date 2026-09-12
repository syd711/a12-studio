package de.a12.studio.models.overviewmodel;

import de.a12.studio.models.ModelRoundTrip;
import org.junit.jupiter.api.Test;

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
  void roundTripsStringViewModeEnumerationViewModeAndDateTimeRangesAndPeriods() throws Exception {
    // Person_Ov.json
    ModelRoundTrip.assertRoundTrip(getClass(), "/overviewmodel/PersonWithFilterViewModeAndPeriods_Ov.json", OverviewModel.class);

    OverviewModel model = ModelRoundTrip.load(getClass(), "/overviewmodel/PersonWithFilterViewModeAndPeriods_Ov.json", OverviewModel.class);
    List<FilterGroup> groups = model.getContent().getConfiguration().getNewFilterConfiguration().getFilterGroups();

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
  void roundTripsNumberRangesAndPreferFilterBar() throws Exception {
    // PersonFreelancer_Ov.json
    ModelRoundTrip.assertRoundTrip(getClass(), "/overviewmodel/PersonFreelancerWithFilterRangesAndPreferFilterBar_Ov.json", OverviewModel.class);

    OverviewModel model = ModelRoundTrip.load(getClass(), "/overviewmodel/PersonFreelancerWithFilterRangesAndPreferFilterBar_Ov.json", OverviewModel.class);
    List<FilterGroup> groups = model.getContent().getConfiguration().getNewFilterConfiguration().getFilterGroups();

    FilterItem numberItem = groups.get(1).getFilterItems().get(0);
    assertEquals("number", numberItem.getType());
    assertEquals(4, numberItem.getOptions().getRanges().size());

    FilterItem preferFilterBarItem = groups.get(0).getFilterItems().get(2);
    assertTrue(preferFilterBarItem.getPreferFilterBar());
  }
}
