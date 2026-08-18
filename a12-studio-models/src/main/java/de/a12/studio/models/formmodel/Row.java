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

  // Hides this row when the referenced boolean field equals hideConditionValue.
  // hideConditionField holds the document model field id; hideConditionValue is "true" or null (= "no value").
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String hideConditionField;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String hideConditionValue;

  private List<Cell> cell = new ArrayList<>();
}
