package de.a12.studio.dataservices.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Control extends Cell {

  // Reference to the underlying Document Model field (or group, for attachments) this Control edits.
  private String elementRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private GridSpan offset;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Style> style = new ArrayList<>();
  // Positioning of the hint/validation message relative to the Control, e.g. "TOOLTIP".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String messageExposition;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean tooltipsOnTop;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DatePickerConfig datePickerConfig;

  public Control() {
    setType(CellType.CONTROL);
  }
}
