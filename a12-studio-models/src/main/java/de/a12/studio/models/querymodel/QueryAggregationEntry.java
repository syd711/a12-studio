package de.a12.studio.models.querymodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * One entry of {@link QueryAggregation#getAggregations()}: an aggregation {@code function} applied to a {@code field}
 * of the target Document Model for every group of the result (wire shape {@code {"function": "sum", "field":
 * "/Contract/Liability"}}, plus SME's optional {@code alias}).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class QueryAggregationEntry {

  public static final String FUNCTION_COUNT = "count";
  public static final String FUNCTION_SUM = "sum";
  public static final String FUNCTION_MAX = "max";
  public static final String FUNCTION_MIN = "min";
  public static final String FUNCTION_AVG = "avg";

  /** The five functions Data Services offers, in the order SME's editor lists them. */
  public static final List<String> FUNCTIONS = List.of(FUNCTION_AVG, FUNCTION_MIN, FUNCTION_MAX, FUNCTION_SUM, FUNCTION_COUNT);

  private String function;
  private String field;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String alias;
}
