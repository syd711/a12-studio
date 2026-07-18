package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class ControlGrid extends ScreenElement {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ColumnLayout layout;
  private List<Row> row = new ArrayList<>();
  // "TOP" or "BOTTOM".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String verticalAlignment;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  // "TEXT" or "INPUT"; defines how readonly Controls inside this grid are rendered.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String readonlyPresentation;

  public ControlGrid() {
    setType(ScreenElementType.CONTROL_GRID);
  }
}
