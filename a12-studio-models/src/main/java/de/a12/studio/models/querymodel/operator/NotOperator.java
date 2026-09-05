package de.a12.studio.models.querymodel.operator;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NotOperator extends Operator {

  private Operator operand;

  public NotOperator() {
    setOperator("not");
  }
}
