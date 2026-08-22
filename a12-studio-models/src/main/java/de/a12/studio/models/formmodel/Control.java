package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({"type", "id", "name", "offset", "span", "style", "readonly", "messageExposition", "label",
    "hint", "placeholder", "accessibility", "datePickerConfig", "elementRef", "tooltipsOnTop",
    "labelHiddenButRead", "annotations"})
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
  // Keeps the label mandatory for screen readers while hiding it visually on screen.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHiddenButRead;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DatePickerConfig datePickerConfig;
  // Per-Control overrides for hint, placeholder and accessibility text (control-level values take precedence
  // over the model-wide FieldConfigEntry values stored in FormModelContent.fieldConfiguration).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer hint;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer placeholder;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer accessibility;
  // Hides this control when the referenced boolean field equals hideConditionValue.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String hideConditionField;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String hideConditionValue;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<de.a12.studio.models.Annotation> annotations = new ArrayList<>();

  public Control() {
    setType(CellType.CONTROL);
  }
}
