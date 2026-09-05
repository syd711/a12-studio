package de.a12.studio.models.querymodel.operator;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DoubleNode;

/** Matches an INumberType field against an inclusive [from, to] range; either bound may be left open. */
@Getter
@Setter
public class DoubleRangeOperator extends Operator {

  private String field;

  // Backed by a JsonNode (mirroring overviewmodel.Column.width) so a plain-integer source value (e.g. "5")
  // round-trips instead of always coming back as a decimal (e.g. "5.0").
  @JsonProperty("from")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode fromNode;

  @JsonProperty("to")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode toNode;

  public DoubleRangeOperator() {
    setOperator("double_range");
  }

  @JsonIgnore
  public Double getFrom() {
    return fromNode == null || fromNode.isNull() ? null : fromNode.asDouble();
  }

  @JsonIgnore
  public void setFrom(Double from) {
    fromNode = from == null ? null : DoubleNode.valueOf(from);
  }

  @JsonIgnore
  public Double getTo() {
    return toNode == null || toNode.isNull() ? null : toNode.asDouble();
  }

  @JsonIgnore
  public void setTo(Double to) {
    toNode = to == null ? null : DoubleNode.valueOf(to);
  }
}
