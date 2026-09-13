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
 * {@link #preferFilterBar}, {@link #collapsed}, and {@link #label}/{@link #icon}.
 * <p>
 * The field reference lives under {@link #options} (SME's actual shape: {@code options.fieldId}), not as a
 * sibling {@code fieldRef}/{@code FieldRef} property - that was this class's original (incorrect) guess, which
 * caused every open/save cycle through the Overview Model editor to silently drop the field reference via {@code
 * ignoreUnknown = true} (recovered from {@code testing/workspaces/basic/models/Company_OM.json}'s git history,
 * commit {@code 059be31}, which shows the real shape; commit {@code 8f4757d} shows this class's old {@code
 * fieldRef}-based (de)serialization stripping it back out).
 * <p>
 * A Filter-Definition-based (query) item is the alternative to a Field Reference-based one: {@link #type} is
 * {@value #TYPE_QUERY}, {@link #options} (in particular its {@code fieldId}) is unused, and {@link
 * #filterDefinition} holds the query expression instead - written/validated the same way as {@link
 * de.a12.studio.models.querymodel.QueryModelContent#getFilterDefinition()} (same query language and grammar, per
 * the platform docs' "Filter Items" section). {@link #description} and {@link #enabled} are the two further
 * properties the docs describe as specific to query-based items ("Description defines the text shown for the
 * filter in the application", "Enabled defines whether the predefined filter is active by default" - the latter
 * modeled as a {@link BooleanUserAccessOption} like {@link FilterItemOptions}' matching-option fields, since
 * there's no fixture evidence of the real JSON shape and this is the same "a default value plus whether end
 * users may change it" shape used everywhere else in this model).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterItem {

  public static final String TYPE_QUERY = "query";

  private String id;
  private String type;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private FilterItemOptions options;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean preferFilterBar;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean collapsed;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String filterDefinition;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> description = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption enabled;
}
