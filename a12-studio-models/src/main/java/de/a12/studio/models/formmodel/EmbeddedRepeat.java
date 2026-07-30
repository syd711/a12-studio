package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

// Expands a row's detail edit form as a Control Grid embedded directly below the overview table.
@Getter
@Setter
@JsonPropertyOrder({"type", "id", "name", "title", "readonly", "repeatOverviewColumn", "groupRef", "enableAdd",
    "enableRemove", "enableReorder", "enableCopy", "enableColumnsResize", "infiniteScrolling",
    "readonlyPresentation", "tableStyle", "controlGrid", "defaultRowAction"})
public class EmbeddedRepeat extends AbstractRepeat {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ControlGrid controlGrid;

  public EmbeddedRepeat() {
    setType(ScreenElementType.EMBEDDED_REPEAT);
  }
}
