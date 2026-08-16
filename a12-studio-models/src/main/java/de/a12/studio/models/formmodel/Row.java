package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Annotation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
public class Row {

  // Always "Row" on disk; kept as a plain field since a Row only ever appears inside a ControlGrid's
  // "row" array, so unlike Cell/ScreenElement no polymorphic dispatch on this property is needed here.
  private String type = "Row";
  private String id;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText title;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Style> style = new ArrayList<>();
  // SME's "annotated_mixin" - a plain "annotations" field on the wire, matching documentmodel.Element.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();
  private List<Cell> cell = new ArrayList<>();
}
