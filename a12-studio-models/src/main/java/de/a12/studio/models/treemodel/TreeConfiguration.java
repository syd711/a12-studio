package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.a12.studio.models.overviewmodel.MultiSelectionConfig;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DoubleNode;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class TreeConfiguration {

  // Absent = drag and drop disabled; otherwise {"onDrag": {"expandHoveredNode": <bool>}}.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Object> dnd;
  // Absent = multi-selection disabled.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private MultiSelectionConfig multiSelection;
  // Points at the id of one of the nodes' childRelationshipConfigurations: the relationship that yields the
  // tree's root nodes (SME "Root").
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String rootRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String hierarchicalColumnRef;
  // SME writes this as `true` or omits it ("Hide Label"), never `false`.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ExpansionStrategy expansionStrategy;
  // SME writes these two as `true` or omits them ("Enable Virtual Scrolling", "Enable Columns Resize"), never `false`.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableVirtualScroll;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableColumnsResize;
  // Independent of enableVirtualScroll: SME keeps the value while virtual scrolling is off and only requires it
  // while it is on.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer rowHeight;
  // A relative width (SME shows it with one fractional digit, e.g. "1.0"), but fixtures store it as `1`; a JsonNode
  // preserves the original token across a load/save cycle, the same trick as overviewmodel.OverviewConfiguration.
  @JsonProperty("actionColumnWidth")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode actionColumnWidthNode;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonIgnore
  public Double getActionColumnWidth() {
    return actionColumnWidthNode == null || actionColumnWidthNode.isNull() ? null : actionColumnWidthNode.asDouble();
  }

  @JsonIgnore
  public void setActionColumnWidth(Double actionColumnWidth) {
    actionColumnWidthNode = actionColumnWidth == null ? null : DoubleNode.valueOf(actionColumnWidth);
  }

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }
}
