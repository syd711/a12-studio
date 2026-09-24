package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Common fields shared by Inline, Embedded and Detached repeats, which all render one row per instance of
// a repeatable Document Model group but differ in how the row detail is edited (inline, embedded, or in a
// separate detail screen).
@Getter
@Setter
public abstract class AbstractRepeat extends ScreenElement {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean readonly;
  // Some files omit "repeatOverviewColumn" while others write it as "[]"; the explicit flag preserves that distinction
  // across a load/save cycle (same approach as A12Model labels/locales).
  @JsonIgnore
  private List<RepeatOverviewColumn> repeatOverviewColumn = new ArrayList<>();

  @JsonIgnore
  private boolean repeatOverviewColumnExplicit;

  @JsonProperty("repeatOverviewColumn")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<RepeatOverviewColumn> getRepeatOverviewColumnForJson() {
    return repeatOverviewColumnExplicit || !repeatOverviewColumn.isEmpty() ? repeatOverviewColumn : null;
  }

  @JsonProperty("repeatOverviewColumn")
  private void setRepeatOverviewColumnForJson(List<RepeatOverviewColumn> value) {
    repeatOverviewColumnExplicit = value != null;
    repeatOverviewColumn = value != null ? value : new ArrayList<>();
  }
  // Reference to the repeatable Document Model group this repeat iterates over.
  private String groupRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableAdd;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean enableRemove;
  // Number of rows fetched/rendered per page. No editor UI yet - mapped purely for lossless round-tripping.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer pageSize;
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
  private DefaultRowAction defaultRowAction;
  // Custom row action buttons, distinct from defaultRowAction (the built-in row-click behavior).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private RowActionGroup rowActionGroup;
  // A query-language expression filtering this repeat's rows, independent of any field/column filter.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String filterExpression;
  // Id of the RepeatOverviewColumn this repeat is initially sorted by.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String initialSorting;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean titleHidden;
  // Per-repeat override of the model-wide Defaults.confirmationTexts (e.g. "REMOVE").
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, ConfirmationText> confirmationTexts = new LinkedHashMap<>();

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, TextContainer> buttonLabels = new LinkedHashMap<>();
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
