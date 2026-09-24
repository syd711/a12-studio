package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@JsonPropertyOrder({"type", "id", "name", "offset", "span", "style", "readonly", "readonlyPresentation",
    "messageExposition", "markingOfRequiredFields", "label", "hint", "placeholder", "accessibility",
    "datePickerConfig", "elementRef", "index", "tooltipsOnTop", "labelHiddenButRead", "annotations"})
public class Control extends Cell {

  // Reference to the underlying Document Model field (or group, for attachments) this Control edits.
  private String elementRef;
  // Which repetition of a repeatable group to show when this Control sits outside that group's repeat (SME's
  // "Control Index"). Only meaningful for such a Control, see ElementIndex#granularity.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ControlIndex index;
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
  // Per-Control override of the model-wide FormModelContent.readonlyPresentation default ("INPUT"/"TEXT"),
  // e.g. how a readonly Control renders. Unset falls back to that model setting.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String readonlyPresentation;
  // Per-Control override of the model-wide FieldConfigEntry.exposition default, e.g. "COMPACT".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String exposition;
  // Per-Control override of the model-wide FormModelContent.markingOfRequiredFields default
  // ("NONE"/"REQUIRED"/"ALWAYS"), i.e. whether this Control's label shows a required-field asterisk.
  // Unset falls back to that model setting.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String markingOfRequiredFields;
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
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HideCondition hideCondition;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<de.a12.studio.models.Annotation> annotations = new ArrayList<>();
  // The inverse of HideCondition/DependentConfig: other screen elements (by id) that are hidden while this
  // Control's field has masterValue (null = no value). Edited on the Dependencies tab of a Boolean, Confirm or
  // Enumeration Control (DependentControlsPanelController).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DependentControls dependentControls;

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean autoExpand;

  public Control() {
    setType(CellType.CONTROL);
  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  @Getter
  @Setter
  public static class DependentControls {
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<Entry> screenElement = new ArrayList<>();

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Getter
    @Setter
    public static class Entry {
      private String idref;
      private String masterValue;
    }
  }
}
