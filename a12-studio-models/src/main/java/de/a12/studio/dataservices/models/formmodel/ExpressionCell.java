package de.a12.studio.dataservices.models.formmodel;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionCell extends Cell {

  private String expression;

  public ExpressionCell() {
    setType(CellType.EXPRESSION_CELL);
  }
}
