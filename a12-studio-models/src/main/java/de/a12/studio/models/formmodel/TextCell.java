package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TextCell extends Cell {

  private TextContainer content;
  // Display variant, e.g. "INFO", "WARNING", "SUCCESS", "ERROR".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String decoration;

  public TextCell() {
    setType(CellType.TEXT_CELL);
  }
}
