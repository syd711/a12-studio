package de.a12.studio.models.overviewmodel;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DoubleNode;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
@Setter
// width's JsonNode type otherwise gets pushed to the end of the property order by Jackson's default
// introspection regardless of declaration order, so the order must be pinned explicitly here.
@JsonPropertyOrder({"id", "label", "width", "fixedWidth", "alignment", "pinDirection", "styles", "icon",
    "labelHidden", "elementRef", "sortable", "preferredSorting", "attachmentDisplayMode", "suffix", "summary",
    "name", "expression"})
public class Column {

  public static final String PIN_DIRECTION_LEFT = "LEFT";
  public static final String PIN_DIRECTION_RIGHT = "RIGHT";

  public static final String PREFERRED_SORTING_ASC = "ASC";
  public static final String PREFERRED_SORTING_DESC = "DESC";

  public static final String ATTACHMENT_DISPLAY_MODE_PREVIEW = "preview";
  public static final String ATTACHMENT_DISPLAY_MODE_ICON = "icon";
  public static final String ATTACHMENT_DISPLAY_MODE_FILE_NAME = "file_name";
  public static final String ATTACHMENT_DISPLAY_MODE_ICON_WITH_FILE_NAME = "icon_with_file_name";

  private String id;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  // Some files write width as a plain JSON integer (e.g. "1") while others use a decimal (e.g. "1.0"); a
  // JsonNode preserves that original formatting across a load/save cycle instead of coercing every value
  // through a single numeric representation.
  @JsonProperty("width")
  private JsonNode widthNode;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean fixedWidth;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ColumnAlignment alignment;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String pinDirection;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ColumnStyles styles;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String elementRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean sortable;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String preferredSorting;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String attachmentDisplayMode;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> suffix = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<SummaryConfig> summary = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String expression;

  @JsonIgnore
  public Double getWidth() {
    return widthNode == null || widthNode.isNull() ? null : widthNode.asDouble();
  }

  @JsonIgnore
  public void setWidth(Double width) {
    widthNode = width == null ? null : DoubleNode.valueOf(width);
  }
}
