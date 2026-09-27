package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.overviewmodel.BoxElement;
import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;

/**
 * Gap 17 of "Overview Model: gap review" (structural refactoring): removes a Subheader element that {@link
 * OverviewSubHeaderElementValidator} would report as "not allowed" - Filter/Search/Multi-Selection while their
 * feature is off - instead of merely flagging it, mirroring what SME's own refactoring dialog does when a
 * feature switch is turned off. Reuses that validator's exact predicates ({@code isFilterAllowed}/{@code
 * isSearchAllowedForPlainOverview}/{@code isMultiSelectionAllowedForPlainOverview}), so the two never drift
 * apart on what counts as "not allowed". Search and Multi-Selection are only pruned for a non-Binding overview
 * ({@code purpose == null}) - a Binding overview's Search/Multi-Selection element is never flagged as an
 * outright error at all (see that validator's own javadoc), so there's nothing to prune for either purpose.
 * Filter's own purpose gating ({@code isFilterAllowed} already returns {@code true} for {@code
 * selected_item}) is reused as-is, needing no extra purpose check here.
 */
public final class OverviewSubHeaderPruning {

  private OverviewSubHeaderPruning() {
  }

  /** Removes every currently-disallowed Filter/Search/Multi-Selection element from {@code model}'s Subheader,
   * both slots. Returns whether anything was actually removed, so the caller only needs to persist/re-render
   * when this is {@code true}. A no-op if the model has no configuration or Subheader yet. */
  public static boolean pruneDisallowedElements(OverviewModel model, String purpose) {
    OverviewConfiguration configuration = model.getContent().getConfiguration();
    ElementBox subHeaderBox = model.getContent().getSubHeaderBox();
    if (configuration == null || subHeaderBox == null) {
      return false;
    }

    boolean changed = false;
    if (!OverviewSubHeaderElementValidator.isFilterAllowed(configuration, purpose)) {
      changed |= removeType(subHeaderBox, BoxElementType.FILTER);
    }
    if (purpose == null) {
      if (!OverviewSubHeaderElementValidator.isSearchAllowedForPlainOverview(configuration)) {
        changed |= removeType(subHeaderBox, BoxElementType.SEARCH);
      }
      if (!OverviewSubHeaderElementValidator.isMultiSelectionAllowedForPlainOverview(configuration)) {
        changed |= removeType(subHeaderBox, BoxElementType.MULTI_SELECTION);
      }
    }
    return changed;
  }

  private static boolean removeType(ElementBox subHeaderBox, BoxElementType type) {
    boolean removedFromLeft = removeType(subHeaderBox.getLeftSlot(), type);
    boolean removedFromRight = removeType(subHeaderBox.getRightSlot(), type);
    return removedFromLeft || removedFromRight;
  }

  private static boolean removeType(java.util.List<BoxElement> slot, BoxElementType type) {
    return slot.removeIf(element -> element.getType() == type);
  }
}
