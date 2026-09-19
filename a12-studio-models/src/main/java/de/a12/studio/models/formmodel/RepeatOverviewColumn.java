package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import de.a12.studio.models.Annotation;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;

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
  // A column width is a number with at most one decimal place and a minimum of 0.3 (SME's NumberType
  // constraints; 1.0 is roughly 150px). Fixtures mix plain integers ("1") and fractions ("0.8"), so a JsonNode
  // preserves the original token across a load/save cycle, the same trick as overviewmodel.Column#width.
  // getWidth()/setWidth(Double) are the convenience accessors; an integral value is written back as an
  // integer, like the fixtures do.
  @JsonProperty("width")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode widthNode;
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

  @JsonIgnore
  public Double getWidth() {
    return widthNode == null || widthNode.isNull() ? null : widthNode.asDouble();
  }

  @JsonIgnore
  public void setWidth(Double width) {
    if (width == null) {
      widthNode = null;
    }
    else if (width == Math.rint(width) && Math.abs(width) < Integer.MAX_VALUE) {
      widthNode = IntNode.valueOf((int) Math.rint(width));
    }
    else {
      widthNode = DoubleNode.valueOf(width);
    }
  }
}
