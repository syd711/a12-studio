package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.IntNode;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OverviewConfiguration {

  private Boolean enableFilter;
  private Boolean showFullTextSearch;
  @JsonInclude(JsonInclude.Include.NON_NULL)
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
  // Normally a plain integer, but at least one fixture uses a fractional value (e.g. "0.5"); a JsonNode
  // preserves that original value across a load/save cycle instead of truncating it through an Integer,
  // the same trick as overviewmodel.Column#width.
  @JsonProperty("actionColumnWidth")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode actionColumnWidthNode;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableInfiniteScroll;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> rowTitle = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> subtitle = new ArrayList<>();

  @JsonIgnore
  public Integer getActionColumnWidth() {
    return actionColumnWidthNode == null || actionColumnWidthNode.isNull() ? null : (int) actionColumnWidthNode.asDouble();
  }

  @JsonIgnore
  public void setActionColumnWidth(Integer actionColumnWidth) {
    actionColumnWidthNode = actionColumnWidth == null ? null : IntNode.valueOf(actionColumnWidth);
  }
}
