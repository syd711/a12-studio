package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * One entry of a {@link FilterGroup#getFilterItems()}. Covers the properties the platform docs ({@code
 * overview_engine-overviewengine-dev-docs.md} / {@code sme-sme-om-ba-docs.md}, "Filter Items") describe as
 * common to every filter item: a Field Reference ({@link #fieldRef}), the derived {@link #type}, {@link
 * #showInFilterBar}, {@link #collapsed}, and {@link #label}/{@link #icon}.
 * <p>
 * NOTE: unlike {@link FilterSelectorConfig}/{@link FilterTriggerConfig} (verified against {@code
 * testing/basic/models/Company_OM.json}), no fixture or SME reference exists anywhere in this codebase for a
 * populated {@code filterGroups} entry - {@code Company_OM.json}'s is {@code []}. The field-type-specific option
 * groups the docs describe (Boolean/Confirm criteria, String view mode, Enumeration initial criteria/pinned
 * values, Number ranges, Date/Time ranges and periods) and Filter-Definition-based (query) items are therefore
 * intentionally not modeled here - only Field Reference-based items are supported for now, matching how {@link
 * de.a12.studio.models.overviewmodel.FilterSection} was bootstrapped without its dialog fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterItem {

  private String id;
  private String type;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private FieldRef fieldRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean showInFilterBar;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean collapsed;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
}
