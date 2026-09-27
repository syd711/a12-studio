package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.FilterElement;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import de.a12.studio.models.overviewmodel.MultiSelectionElement;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.overviewmodel.OverviewModelContent;
import de.a12.studio.models.overviewmodel.SearchElement;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Gap 17 of the "Overview Model: gap review" - {@link OverviewSubHeaderPruning} removes a Subheader element
 * once its feature switch is off, reusing {@link OverviewSubHeaderElementValidator}'s own predicates. */
class OverviewSubHeaderPruningTest {

  private static OverviewModel modelWithSubHeader(boolean showFullTextSearch, boolean enableFilter, boolean showFilterButton,
      boolean multiSelectionEnabled) {
    OverviewModel model = new OverviewModel();
    model.setId("Team_Ov");
    OverviewModelContent content = new OverviewModelContent();
    OverviewConfiguration configuration = new OverviewConfiguration();
    configuration.setShowFullTextSearch(showFullTextSearch);
    configuration.setEnableFilter(enableFilter);
    FilterConfiguration filterConfiguration = new FilterConfiguration();
    filterConfiguration.setShowFilterButton(showFilterButton);
    configuration.setFilterConfiguration(filterConfiguration);
    if (multiSelectionEnabled) {
      configuration.setMultiSelection(new MultiSelectionConfig());
    }
    content.setConfiguration(configuration);

    ElementBox subHeaderBox = ElementBox.createEmpty();
    subHeaderBox.getLeftSlot().add(new MultiSelectionElement());
    subHeaderBox.getRightSlot().add(new SearchElement());
    subHeaderBox.getRightSlot().add(new FilterElement());
    content.setSubHeaderBox(subHeaderBox);
    model.setContent(content);
    return model;
  }

  @Test
  void removesAllThreeElementsWhenEveryFeatureIsOff() {
    OverviewModel model = modelWithSubHeader(false, false, false, false);

    boolean changed = OverviewSubHeaderPruning.pruneDisallowedElements(model, null);

    assertTrue(changed);
    assertTrue(model.getContent().getSubHeaderBox().getLeftSlot().isEmpty());
    assertTrue(model.getContent().getSubHeaderBox().getRightSlot().isEmpty());
  }

  @Test
  void keepsElementsWhoseFeatureIsOn() {
    OverviewModel model = modelWithSubHeader(true, true, true, true);

    boolean changed = OverviewSubHeaderPruning.pruneDisallowedElements(model, null);

    assertFalse(changed);
    assertEquals(1, model.getContent().getSubHeaderBox().getLeftSlot().size());
    assertEquals(2, model.getContent().getSubHeaderBox().getRightSlot().size());
  }

  @Test
  void removesOnlyTheFilterElementWhenOnlyFilterIsOff() {
    OverviewModel model = modelWithSubHeader(true, false, false, true);

    boolean changed = OverviewSubHeaderPruning.pruneDisallowedElements(model, null);

    assertTrue(changed);
    assertEquals(1, model.getContent().getSubHeaderBox().getLeftSlot().size(), "Multi-Selection stays");
    assertEquals(1, model.getContent().getSubHeaderBox().getRightSlot().size(), "Search stays, Filter is removed");
    assertEquals("search", model.getContent().getSubHeaderBox().getRightSlot().get(0).getType().getValue());
  }

  @Test
  void doesNotPruneSearchOrMultiSelectionForABindingOverviewEvenWhenOff() {
    // Neither has a "not allowed" rule for either Binding purpose - only Filter's does (skipped for
    // selected_item specifically, handled by isFilterAllowed itself).
    OverviewModel model = modelWithSubHeader(false, true, true, false);

    boolean changed = OverviewSubHeaderPruning.pruneDisallowedElements(model, OverviewBindingPurpose.AVAILABLE_ITEM);

    assertFalse(changed);
    assertEquals(1, model.getContent().getSubHeaderBox().getLeftSlot().size());
    assertEquals(2, model.getContent().getSubHeaderBox().getRightSlot().size());
  }

  @Test
  void doesNotPruneFilterForASelectedItemsBindingOverviewEvenWhenOff() {
    OverviewModel model = modelWithSubHeader(true, false, false, true);

    boolean changed = OverviewSubHeaderPruning.pruneDisallowedElements(model, OverviewBindingPurpose.SELECTED_ITEM);

    assertFalse(changed);
    assertEquals(2, model.getContent().getSubHeaderBox().getRightSlot().size());
  }
}
