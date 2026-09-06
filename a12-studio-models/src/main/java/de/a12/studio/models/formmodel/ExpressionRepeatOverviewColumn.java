package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// A repeat overview column computed via expression instead of bound to a Document Model field directly.
// Unlike FieldBasedRepeatOverviewColumn, this carries its own "name" (SME's IdNamed mixin, confirmed against
// a real fixture, client/resources/input/models/fmm/TestModel-form.json).
@Getter
@Setter
public class ExpressionRepeatOverviewColumn extends RepeatOverviewColumn {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;
  private String expression;

  public ExpressionRepeatOverviewColumn() {
    setType(RepeatOverviewColumnType.EXPRESSION_BASED);
  }
}
