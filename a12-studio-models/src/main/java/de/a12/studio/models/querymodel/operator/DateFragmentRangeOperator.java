package de.a12.studio.models.querymodel.operator;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/** Matches an IDateFragmentType field against an inclusive [from, to] range. */
@Getter
@Setter
public class DateFragmentRangeOperator extends Operator {

  private String field;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String from;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String to;

  public DateFragmentRangeOperator() {
    setOperator("datefragment_range");
  }
}
