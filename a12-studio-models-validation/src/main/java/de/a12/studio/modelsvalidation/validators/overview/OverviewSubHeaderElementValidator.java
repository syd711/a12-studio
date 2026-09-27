package de.a12.studio.modelsvalidation.validators.overview;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.overviewmodel.BoxElementType;
import de.a12.studio.models.overviewmodel.ElementBox;
import de.a12.studio.models.overviewmodel.FilterConfiguration;
import de.a12.studio.models.overviewmodel.NewFilterConfiguration;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.Severity;
import de.a12.studio.modelsvalidation.ValidationContext;
import de.a12.studio.modelsvalidation.ValidationMessages;
import de.a12.studio.modelsvalidation.validators.ModelValidator;

import java.util.ArrayList;
import java.util.List;

/**
 * The Filter/Search/Multi-Selection Subheader element rules SME gates on whether the corresponding feature is
 * actually switched on, checked directly against every rule's real condition in {@code
 * OverviewMetaModel.json} (not the simplified "just skip while off" a first reading suggests):
 * <ul>
 *   <li>Filter: a {@code filter} element while Enable Filter/Show Filter Button is off is an error, and
 *   Filter's own "no element added"/"only one allowed" pair - for every purpose <b>except</b> {@code
 *   selected_item} ({@code filterType...NotAllowedWhenNotShowFilter}, {@code noFilterIsAdded}, {@code
 *   onlyOneFilterIsAllowed}: {@code purpose} unset or {@code available_item} still gets these, {@code
 *   selected_item} never does - a Binding's Selected Items overview apparently doesn't offer its own filter
 *   button at all).
 *   <li>Search: a {@code search} element while Show Full Text Search is off is an error - but only for a
 *   non-Binding overview ({@code purpose} unset); for {@code available_item} the same shape is instead a
 *   WARNING with a different message ({@code searchType...NotAllowedWhenNotShowFullTextSearch_AvailableItems_Binding},
 *   "Search element is not supported in Available Items Binding Overview."), and {@code selected_item} has no
 *   rule for it at all. {@link OverviewSearchElementValidator}'s own "no element added"/"only one allowed" pair
 *   is likewise {@code purpose}-gated (skipped for both Binding purposes), same reasoning as {@link
 *   OverviewMultiSelectionElementValidator}.
 *   <li>Multi-Selection: a {@code multi_selection} element while Multi-Selection is off is an error, but only
 *   for a non-Binding overview - skipped for either Binding purpose, matching {@link
 *   OverviewMultiSelectionElementValidator}'s own gating.
 * </ul>
 * Every rule fires per Subheader slot (major = {@code rightSlot}, minor = {@code leftSlot}) independently, so a
 * model with the same disallowed element type in both slots gets two errors.
 */
public final class OverviewSubHeaderElementValidator implements ModelValidator {

  public static final String FILTER_ELEMENT_ID = "content/subHeaderBox/filter";
  public static final String SEARCH_ELEMENT_ID = "content/subHeaderBox/search";
  public static final String MULTI_SELECTION_ELEMENT_ID = "content/subHeaderBox/multiSelection";

  @Override
  public List<ModelValidationError> validate(A12Model<?> model, ValidationContext context) {
    if (!(model instanceof OverviewModel overviewModel)) {
      return List.of();
    }
    OverviewConfiguration configuration = overviewModel.getContent().getConfiguration();
    ElementBox subHeaderBox = overviewModel.getContent().getSubHeaderBox();
    if (configuration == null || subHeaderBox == null) {
      return List.of();
    }
    String purpose = OverviewBindingPurpose.resolve(model.getId(), context);

    List<ModelValidationError> errors = new ArrayList<>();
    validateFilter(model, configuration, subHeaderBox, purpose, errors);
    validateSearch(model, configuration, subHeaderBox, purpose, errors);
    validateMultiSelection(model, configuration, subHeaderBox, purpose, errors);
    return errors;
  }

  /** Whether a {@code filter} Subheader element is currently allowed (SME: not {@code purpose ==
   * "selected_item"}, and either Custom Filter mode or Enable Filter + Show Filter Button both on) - shared
   * with {@code OverviewSubHeaderPruning} (gap 17 of "Overview Model: gap review"), which removes the element
   * instead of merely flagging it. */
  static boolean isFilterAllowed(OverviewConfiguration configuration, String purpose) {
    if (OverviewBindingPurpose.SELECTED_ITEM.equals(purpose)) {
      return true;
    }
    // SME's meta model has no concept of Custom Filter (newFilterConfiguration) at all - its filter rules key
    // off filterConfiguration.showFilterButton, which a Custom Filter model doesn't set, so a literal port
    // flags 5 real fixtures (Person_Ov, PersonEmployee_Ov, PersonFreelancer_Ov, All_Skills_Ov,
    // Available_Skills_Ov - all Custom Filter, all with a Filter element) as needing the element removed. Per
    // the platform BA doc, a Custom Filter overview doesn't need the Subheader element at all, so this whole
    // check is skipped once newFilterConfiguration actually holds data (see NewFilterConfiguration#hasContent).
    NewFilterConfiguration newFilterConfiguration = configuration.getNewFilterConfiguration();
    if (newFilterConfiguration != null && newFilterConfiguration.hasContent()) {
      return true;
    }
    FilterConfiguration filterConfig = configuration.getFilterConfiguration();
    return Boolean.TRUE.equals(configuration.getEnableFilter())
        && filterConfig != null && Boolean.TRUE.equals(filterConfig.getShowFilterButton());
  }

  /** Whether a {@code search} Subheader element is allowed for a non-Binding overview ({@code purpose ==
   * null}) - the only case SME treats as an outright error; {@code available_item} gets a WARNING instead
   * (search stays, see {@link #validateSearch}) and {@code selected_item} has no rule for it at all, so this
   * predicate (shared with {@code OverviewSubHeaderPruning}) is deliberately not purpose-aware itself -
   * callers only consult it once they already know {@code purpose == null}. */
  static boolean isSearchAllowedForPlainOverview(OverviewConfiguration configuration) {
    return Boolean.TRUE.equals(configuration.getShowFullTextSearch());
  }

  /** Whether a {@code multi_selection} Subheader element is allowed for a non-Binding overview ({@code purpose
   * == null}) - Multi-Selection has no rule at all for either Binding purpose, matching {@link
   * OverviewMultiSelectionElementValidator}'s own gating. Shared with {@code OverviewSubHeaderPruning}, same
   * "caller already knows purpose == null" convention as {@link #isSearchAllowedForPlainOverview}. */
  static boolean isMultiSelectionAllowedForPlainOverview(OverviewConfiguration configuration) {
    return configuration.getMultiSelection() != null;
  }

  private void validateFilter(A12Model<?> model, OverviewConfiguration configuration, ElementBox subHeaderBox, String purpose,
      List<ModelValidationError> errors) {
    // selected_item has no filter rule at all - not "not allowed", not "missing"/"duplicate" either -
    // unlike isFilterAllowed's own "true" for this purpose, which only means "don't remove the element"
    // (see OverviewSubHeaderPruning) and must not also be read as "then check missing/duplicate".
    if (OverviewBindingPurpose.SELECTED_ITEM.equals(purpose)) {
      return;
    }
    boolean showFilter = isFilterAllowed(configuration, purpose);
    long minorCount = countType(subHeaderBox.getLeftSlot(), BoxElementType.FILTER);
    long majorCount = countType(subHeaderBox.getRightSlot(), BoxElementType.FILTER);
    if (!showFilter) {
      if (minorCount > 0) {
        errors.add(error(model, FILTER_ELEMENT_ID, "validation.overviewSubHeaderElement.filterNotAllowed"));
      }
      if (majorCount > 0) {
        errors.add(error(model, FILTER_ELEMENT_ID, "validation.overviewSubHeaderElement.filterNotAllowed"));
      }
      return;
    }
    long total = minorCount + majorCount;
    if (total == 0) {
      errors.add(error(model, FILTER_ELEMENT_ID, "validation.overviewFilterElement.missing"));
    }
    else if (total > 1) {
      errors.add(error(model, FILTER_ELEMENT_ID, "validation.overviewFilterElement.duplicate"));
    }
  }

  private void validateSearch(A12Model<?> model, OverviewConfiguration configuration, ElementBox subHeaderBox, String purpose,
      List<ModelValidationError> errors) {
    if (isSearchAllowedForPlainOverview(configuration)) {
      return;
    }
    long minorCount = countType(subHeaderBox.getLeftSlot(), BoxElementType.SEARCH);
    long majorCount = countType(subHeaderBox.getRightSlot(), BoxElementType.SEARCH);
    if (purpose == null) {
      if (minorCount > 0 || majorCount > 0) {
        errors.add(error(model, SEARCH_ELEMENT_ID, "validation.overviewSubHeaderElement.searchNotAllowed"));
      }
    }
    else if (OverviewBindingPurpose.AVAILABLE_ITEM.equals(purpose) && (minorCount > 0 || majorCount > 0)) {
      errors.add(new ModelValidationError(model, SEARCH_ELEMENT_ID,
          ValidationMessages.get("validation.overviewSubHeaderElement.searchNotSupportedInAvailableItems"), Severity.WARNING.name()));
    }
  }

  private void validateMultiSelection(A12Model<?> model, OverviewConfiguration configuration, ElementBox subHeaderBox, String purpose,
      List<ModelValidationError> errors) {
    if (purpose != null || isMultiSelectionAllowedForPlainOverview(configuration)) {
      return;
    }
    long minorCount = countType(subHeaderBox.getLeftSlot(), BoxElementType.MULTI_SELECTION);
    long majorCount = countType(subHeaderBox.getRightSlot(), BoxElementType.MULTI_SELECTION);
    if (minorCount > 0 || majorCount > 0) {
      errors.add(error(model, MULTI_SELECTION_ELEMENT_ID, "validation.overviewSubHeaderElement.multiSelectionNotAllowed"));
    }
  }

  static long countType(List<de.a12.studio.models.overviewmodel.BoxElement> elements, BoxElementType type) {
    return elements.stream().filter(element -> element.getType() == type).count();
  }

  private static ModelValidationError error(A12Model<?> model, String elementId, String messageKey) {
    return new ModelValidationError(model, elementId, ValidationMessages.get(messageKey), Severity.ERROR.name());
  }
}
