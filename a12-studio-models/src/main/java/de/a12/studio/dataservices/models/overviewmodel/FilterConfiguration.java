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
public class FilterConfiguration {

  public static final String FILTER_MODE_ALL = "all";
  public static final String FILTER_MODE_ALL_WITH_META = "all_with_meta";
  public static final String FILTER_MODE_ALL_COLUMNS = "all_columns";
  public static final String FILTER_MODE_CUSTOM_LIST = "custom_list";

  private Boolean showFilterButton;
  private Boolean showFilterBar;
  private String filterMode;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<FieldRef> fields = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<FilterSection> sectionData = new ArrayList<>();
}
