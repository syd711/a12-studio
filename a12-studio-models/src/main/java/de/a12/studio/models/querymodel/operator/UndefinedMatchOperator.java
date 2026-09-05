package de.a12.studio.models.querymodel.operator;

import lombok.Getter;
import lombok.Setter;

/** Matches if a field's value is null, empty, or the field doesn't exist in the document. */
@Getter
@Setter
public class UndefinedMatchOperator extends Operator {

  private String field;

  public UndefinedMatchOperator() {
    setOperator("undefined_match");
  }
}
