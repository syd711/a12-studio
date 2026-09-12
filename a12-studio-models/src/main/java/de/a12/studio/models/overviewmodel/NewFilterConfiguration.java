package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code content.configuration.newFilterConfiguration}: the "Custom Filter" filter mode's full filter structure -
 * see the Custom Filter Configuration editor. Field names/nesting are taken directly from {@code
 * testing/workspaces/basic/models/Company_OM.json} (SME's reference implementation - {@code
 * client/src/modules/overviewModel/document/omDocument.ts} - has no equivalent {@code FilterMode} value or type
 * at all, so that fixture, plus its git history, is the sole source of truth here). See {@link FilterGroup}/{@link
 * FilterItem} for what's modeled in {@link #filterGroups}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class NewFilterConfiguration {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private FilterSelectorConfig filterSelector;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JoinOperatorConfig joinOperator;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption invert;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<FilterGroup> filterGroups = new ArrayList<>();

  /**
   * Whether this holds actual custom-filter data, as opposed to merely existing as an empty shell (e.g. freshly,
   * lazily materialized by a sibling panel reading a value for display). Since SME has no {@code filterMode}
   * equivalent for the "Custom Filter" mode - it's represented purely by {@code newFilterConfiguration} being
   * populated - this is what stands in for "Filter Mode is set" wherever that field is checked (e.g. the overview
   * model's "Filter Mode is required" validator).
   */
  public boolean hasContent() {
    return filterSelector != null || joinOperator != null || invert != null || !filterGroups.isEmpty();
  }
}
