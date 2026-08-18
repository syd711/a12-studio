package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Common fields shared by Inline, Embedded and Detached repeats, which all render one row per instance of
// a repeatable Document Model group but differ in how the row detail is edited (inline, embedded, or in a
// separate detail screen).
@Getter
@Setter
public abstract class AbstractRepeat extends ScreenElement {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  private List<RepeatOverviewColumn> repeatOverviewColumn = new ArrayList<>();
  // Reference to the repeatable Document Model group this repeat iterates over.
  private String groupRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableAdd;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableRemove;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableReorder;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableCopy;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableColumnsResize;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean infiniteScrolling;
  // "TEXT" or "INPUT"; defines how readonly columns/controls of this repeat are rendered.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String readonlyPresentation;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TableStyle tableStyle;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private RowAction defaultRowAction;
  // Per-Repeat overrides for label, hint and placeholder (take precedence over GroupConfigEntry values).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer hint;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TextContainer placeholder;
  // Horizontal alignment applied to all columns of this repeat's overview table header and body rows.
  // Possible values mirror the SME reference: e.g. "left", "center", "right".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String defaultHorizontalAlignment;
  // Style classes applied specifically to this repeat's column header row (separate from the body Styles
  // inherited from ScreenElement.style).
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Style> headerStyle = new ArrayList<>();
}
