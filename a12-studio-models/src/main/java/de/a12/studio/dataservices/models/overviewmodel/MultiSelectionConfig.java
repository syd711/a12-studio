package de.a12.studio.dataservices.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class MultiSelectionConfig {

  public static final String COLLAPSE_OPTION_COLLAPSIBLE_COLLAPSED = "collapsible_collapsed";
  public static final String COLLAPSE_OPTION_COLLAPSIBLE_EXPANDED = "collapsible_expanded";
  public static final String COLLAPSE_OPTION_NON_COLLAPSIBLE = "non_collapsible";

  public static final String COUNTER_OPTION_SIMPLE = "simple";
  public static final String COUNTER_OPTION_NONE = "none";

  public static final String SELECTION_AREA_CHECKBOX = "checkbox";
  public static final String SELECTION_AREA_CHECKBOX_AND_ROW = "checkbox_and_row";

  private String collapseOption;
  private String counterOption;
  private String selectionArea;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Button> buttons = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ClearConfirmation clearConfirmation;
}
