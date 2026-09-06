package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.a12.studio.models.Annotation;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

// Fields shared by every column type (SME's RepeatOverviewColumnBase mixin, used by both
// FieldBasedRepeatOverviewColumn and ExpressionRepeatOverviewColumn) live here rather than being duplicated
// per subtype.
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type", visible = true, defaultImpl = GenericRepeatOverviewColumn.class)
@JsonSubTypes({
    @JsonSubTypes.Type(value = FieldBasedRepeatOverviewColumn.class, name = "FieldBasedRepeatOverviewColumn"),
    @JsonSubTypes.Type(value = ExpressionRepeatOverviewColumn.class, name = "ExpressionRepeatOverviewColumn")
})
@Getter
@Setter
public abstract class RepeatOverviewColumn {

  // visible = true above also exposes the type id as this plain property; WRITE_ONLY keeps it settable on
  // deserialization without Jackson also emitting it a second time as a regular property on serialization.
  @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
  private RepeatOverviewColumnType type;
  private String id;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private LocalizedText label;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer width;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean sortable;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean filterable;
  // "FULL" or "STRING".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String filterExposition;
  // "LEFT" or "RIGHT".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String pinDirection;
  // "ASC" or "DESC".
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String preferredSorting;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Alignment specificHorizontalAlignment;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Alignment specificVerticalAlignment;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Style> headerStyle = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean fixedWidth;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private HideCondition hideCondition;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();
}
