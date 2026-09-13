package de.a12.studio.models.documentmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DoubleNode;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
// minValue/maxValue's JsonNode type otherwise gets pushed to the end of the property order by
// Jackson's default introspection regardless of declaration order, so pin the order explicitly.
@JsonPropertyOrder({"minFractionalDigits", "maxFractionalDigits", "minValue", "maxValue", "trait",
    "zeroNotAllowed", "positivesOnly", "leadingZerosAllowed", "maxIntegerDigits"})
public class NumberTypeOptions {

  private Integer minFractionalDigits;
  private Integer maxFractionalDigits;

  // Some files write minValue/maxValue as a plain JSON integer (e.g. "10") while others use a decimal
  // (e.g. "10.0"); a JsonNode preserves that original formatting across a load/save cycle instead of
  // coercing every value through a single numeric representation (same trick as overviewmodel.Column#width).
  @JsonProperty("minValue")
  private JsonNode minValueNode;
  @JsonProperty("maxValue")
  private JsonNode maxValueNode;

  private String trait;
  private Boolean zeroNotAllowed;
  private Boolean positivesOnly;
  private Boolean leadingZerosAllowed;
  private Integer maxIntegerDigits;

  @JsonIgnore
  public Double getMinValue() {
    return minValueNode == null || minValueNode.isNull() ? null : minValueNode.asDouble();
  }

  @JsonIgnore
  public void setMinValue(Double minValue) {
    minValueNode = minValue == null ? null : DoubleNode.valueOf(minValue);
  }

  @JsonIgnore
  public Double getMaxValue() {
    return maxValueNode == null || maxValueNode.isNull() ? null : maxValueNode.asDouble();
  }

  @JsonIgnore
  public void setMaxValue(Double maxValue) {
    maxValueNode = maxValue == null ? null : DoubleNode.valueOf(maxValue);
  }
}
