package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OverviewConfiguration {

  private Boolean enableFilter;
  private Boolean showFullTextSearch;
  private Integer pagingSize;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean showRowCount;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableColumnsResize;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean skipInitialLoad;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private FilterConfiguration filterConfiguration;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private NewFilterConfiguration newFilterConfiguration;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private MultiSelectionConfig multiSelection;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<ColumnRef> initialSorting = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ColumnRef screenReaderColumn;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer rowHeight;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer actionColumnWidth;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableInfiniteScroll;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> rowTitle = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> subtitle = new ArrayList<>();
}
