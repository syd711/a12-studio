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
 * common to every filter item: a Field Reference ({@link #options}' {@code fieldId}), the derived {@link #type},
 * {@link #showInFilterBar}, {@link #collapsed}, and {@link #label}/{@link #icon}.
 * <p>
 * The field reference lives under {@link #options} (SME's actual shape: {@code options.fieldId}), not as a
 * sibling {@code fieldRef}/{@code FieldRef} property - that was this class's original (incorrect) guess, which
 * caused every open/save cycle through the Overview Model editor to silently drop the field reference via {@code
 * ignoreUnknown = true} (recovered from {@code testing/workspaces/basic/models/Company_OM.json}'s git history,
 * commit {@code 059be31}, which shows the real shape; commit {@code 8f4757d} shows this class's old {@code
 * fieldRef}-based (de)serialization stripping it back out). Filter-Definition-based (query) items are still not
 * modeled - only Field Reference-based items are supported for now, matching how {@link
 * de.a12.studio.models.overviewmodel.FilterSection} was bootstrapped without its dialog fields.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterItem {

  private String id;
  private String type;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private FilterItemOptions options;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean showInFilterBar;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean collapsed;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
}
