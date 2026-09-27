package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DoubleNode;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class OverviewConfiguration {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableFilter;
  @JsonInclude(JsonInclude.Include.NON_NULL)
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
  // A relative width (SME: NumberType, minValue 0.3, one decimal, same unit as Column#width - 1.0 = 150px), but
  // some files use a whole number (e.g. "1") while real files with a fractional value (0.3/0.4/0.5) also exist;
  // a JsonNode preserves the original token across a load/save cycle instead of coercing it through a single
  // numeric representation, the same trick as overviewmodel.Column#width.
  @JsonProperty("actionColumnWidth")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode actionColumnWidthNode;

  public static final double MIN_ACTION_COLUMN_WIDTH = 0.3;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableInfiniteScroll;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> rowTitle = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> subtitle = new ArrayList<>();

  @JsonIgnore
  public Double getActionColumnWidth() {
    return actionColumnWidthNode == null || actionColumnWidthNode.isNull() ? null : actionColumnWidthNode.asDouble();
  }

  @JsonIgnore
  public void setActionColumnWidth(Double actionColumnWidth) {
    actionColumnWidthNode = actionColumnWidth == null ? null : DoubleNode.valueOf(actionColumnWidth);
  }
}
