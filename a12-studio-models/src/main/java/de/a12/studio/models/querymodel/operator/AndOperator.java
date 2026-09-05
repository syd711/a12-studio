package de.a12.studio.models.querymodel.operator;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AndOperator extends Operator {

  private List<Operator> operands;

  public AndOperator() {
    setOperator("and");
  }
}
