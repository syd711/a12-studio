package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code newFilterConfiguration.filterSelector}: how the Filter Selector panel itself behaves and looks, per
 * {@code testing/basic/models/Company_OM.json} - {@code viewMode} ("overlay"/"docked"/"modal"), {@code
 * initialVisibility} ("show"/"hide"), the Search Bar ({@link #searchBar}) and Show Only Set Filters ({@link
 * #showSetFiltersOnly}) toggles, the multilingual {@link #headerSubtitle}, and the Filter Button ({@link
 * #trigger}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class FilterSelectorConfig {

  public static final String VIEW_MODE_OVERLAY = "overlay";
  public static final String VIEW_MODE_DOCKED = "docked";
  public static final String VIEW_MODE_MODAL = "modal";

  public static final String VISIBILITY_SHOW = "show";
  public static final String VISIBILITY_HIDE = "hide";

  private String viewMode;
  private String initialVisibility;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption searchBar;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BooleanUserAccessOption showSetFiltersOnly;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> headerSubtitle = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private FilterTriggerConfig trigger;
}
