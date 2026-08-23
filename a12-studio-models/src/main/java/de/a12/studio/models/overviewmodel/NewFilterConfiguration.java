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
}
