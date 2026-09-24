package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExpressionCell extends Cell {

  private String expression;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;

  public ExpressionCell() {
    setType(CellType.EXPRESSION_CELL);
  }
}
