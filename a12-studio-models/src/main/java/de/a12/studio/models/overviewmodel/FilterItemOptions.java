package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code filterGroups[].filterItems[].options}: the field-type-specific configuration of a field-based {@link
 * FilterItem}. {@link #fieldId} is common to every field-based item regardless of type - the field's data type
 * (derived via {@link #fieldId}, see {@link FilterItem#getType()}) then determines which of the further,
 * type-specific option groups the platform docs describe apply:
 * <ul>
 *   <li>String: matching options ({@link #invert}, {@link #empty}, {@link #caseSensitive}, {@link #exactMatch})
 *   plus {@link #viewMode} ({@code textField}/{@code list}) - all fixture-evidenced (recovered from {@code
 *   testing/workspaces/basic/models/Company_OM.json}'s git history, commit {@code 059be31}, and {@code
 *   testing/workspaces/advanced_new/models/10_People/Person_Ov.json} for {@link #viewMode}).</li>
 *   <li>Enumeration: {@link #viewMode} - only the value {@code compact} is fixture-evidenced (same {@code
 *   Person_Ov.json}), so the Overview Model editor only ever writes that one value rather than guessing at
 *   further legal values.</li>
 *   <li>Number, Date, DateTime, Time, DateFragment, DateRange: {@link #ranges} (fixture-evidenced for Number/Date/
 *   DateTime).</li>
 *   <li>Date, DateTime, DateFragment, DateRange (not Time - see the platform docs' "Filter Items"): {@link
 *   #periods} (fixture-evidenced for Date/DateTime only; DateFragment/DateRange are inferred by analogy, see
 *   {@code OverviewElementOptions.defaultPeriods} in {@code a12-studio-ui}).</li>
 * </ul>
 * Boolean/Confirm criteria-based configuration and Enumeration/Multi-select's Initial Criteria, Pinned Values and
 * join behavior are <b>not</b> modeled - no fixture anywhere in this workspace (nor the broader sample workspaces
 * under {@code A12 Tools - 2026.06}) has an example of either, and the platform docs only show them as
 * screenshots, not JSON - so they're silently ignored on load ({@code ignoreUnknown = true}) rather than guessed
 * at. See {@code TODO.md}'s Overview Model section.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterItemOptions {

  private String fieldId;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption invert;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption empty;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption caseSensitive;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption exactMatch;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String viewMode;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<FilterOptionToggle> ranges = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<FilterOptionToggle> periods = new ArrayList<>();
}
