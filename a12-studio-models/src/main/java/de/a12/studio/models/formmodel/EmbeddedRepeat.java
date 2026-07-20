package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

// Expands a row's detail edit form as a Control Grid embedded directly below the overview table.
@Getter
@Setter
public class EmbeddedRepeat extends AbstractRepeat {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ControlGrid controlGrid;

  public EmbeddedRepeat() {
    setType(ScreenElementType.EMBEDDED_REPEAT);
  }
}
