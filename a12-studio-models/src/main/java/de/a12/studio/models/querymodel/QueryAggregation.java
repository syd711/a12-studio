package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * {@code QueryModelContent.aggregation}: turns the query into an aggregation-mode query, whose result holds one
 * generated document per {@link #getGroup() group} (or a single one when there is no group) with the value of every
 * {@link #getAggregations() aggregation} instead of the documents themselves. Wire shape (Data Services' Query API
 * and SME's {@code Query.QueryRoot}): {@code "aggregation": {"aggregations": [{"function", "field"}], "group":
 * [{"field"}]}}. SME's "Aggregate Results" switch ({@code technical_useAggregation}) is editor-only: the block being
 * present <em>is</em> the switch.
 *
 * <p>{@code group} is optional on the wire (no group = aggregate the whole result set), so, like {@link
 * QueryModelContent#getSort()}, an absent key is told apart from an explicit {@code []} to round-trip either one.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class QueryAggregation {

  @JsonIgnore
  private List<QueryAggregationEntry> aggregations = new ArrayList<>();
  @JsonIgnore
  private boolean aggregationsExplicit;

  @JsonIgnore
  private List<QueryAggregationGroup> group = new ArrayList<>();
  @JsonIgnore
  private boolean groupExplicit;

  @JsonProperty("aggregations")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<QueryAggregationEntry> getAggregationsForJson() {
    return aggregationsExplicit || !aggregations.isEmpty() ? aggregations : null;
  }

  @JsonProperty("aggregations")
  private void setAggregationsForJson(List<QueryAggregationEntry> value) {
    this.aggregationsExplicit = value != null;
    this.aggregations = value != null ? value : new ArrayList<>();
  }

  @JsonProperty("group")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<QueryAggregationGroup> getGroupForJson() {
    return groupExplicit || !group.isEmpty() ? group : null;
  }

  @JsonProperty("group")
  private void setGroupForJson(List<QueryAggregationGroup> value) {
    this.groupExplicit = value != null;
    this.group = value != null ? value : new ArrayList<>();
  }
}
