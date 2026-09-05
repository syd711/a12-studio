package de.a12.studio.models.querymodel.operator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * Matches an IDateType/IDateTimeType/IDateRangeType field against an inclusive [from, to] range, serialized per
 * the target field's date format. For an IDateRangeType field, {@code value} (optionally with an interval-format
 * "start/end" string, or paired with {@code reverse}) is an alternative to {@code from}/{@code to} - mutually
 * exclusive per the platform's Data Services documentation.
 */
@Getter
@Setter
public class DateRangeOperator extends Operator {

  private String field;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String from;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String to;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String value;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean reverse;

  public DateRangeOperator() {
    setOperator("date_range");
  }
}
