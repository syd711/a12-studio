package de.a12.studio.models.querymodel.operator;

import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrOperator extends Operator {

  private List<Operator> operands;

  public OrOperator() {
    setOperator("or");
  }
}
