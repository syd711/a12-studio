package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.ColumnAlignment;
import de.a12.studio.models.overviewmodel.ColumnStyles;
import de.a12.studio.models.overviewmodel.Icon;
import lombok.Getter;
import lombok.Setter;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.DoubleNode;
import tools.jackson.databind.node.IntNode;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One column of a Tree Model. Its header is the {@link #getLabel() label} and/or the {@link #getIcon() icon} (a header
 * with neither is what SME warns about as "Empty column header"); the {@link TreeNode}s map their own fields to it.
 */
// width's JsonNode is otherwise pushed to the end of the property order, so the order is pinned here.
@JsonPropertyOrder({"id", "name", "label", "labelHidden", "icon", "width", "alignment", "pinDirection", "fixedWidth",
    "styles"})
@Getter
@Setter
public class TreeColumn {

  public static final String PIN_DIRECTION_LEFT = "left";
  public static final String PIN_DIRECTION_RIGHT = "right";

  /** SME's smallest column width, and the default of a new column. */
  public static final double MIN_WIDTH = 0.3;
  public static final double DEFAULT_WIDTH = 1.0;

  private String id;
  private String name;
  private List<Label> label = new ArrayList<>();
  // SME writes this as `true` or omits it ("Hide Label", the label then only names the icon for screen readers).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  // A relative width with one fractional digit (0.3, 1.5, ...). Files store whole numbers as `1` (a JavaScript
  // export) and others as `0.5`, so a JsonNode keeps the original token across a load/save cycle, the same trick as
  // overviewmodel.Column.width.
  @JsonProperty("width")
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private JsonNode widthNode;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ColumnAlignment alignment;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String pinDirection;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean fixedWidth;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ColumnStyles styles;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonIgnore
  public Double getWidth() {
    return widthNode == null || widthNode.isNull() ? null : widthNode.asDouble();
  }

  /** The width as shown: a whole width without decimals ("1"), any other with its one ("1.5"); empty while unset. */
  @JsonIgnore
  public String getWidthText() {
    Double width = getWidth();
    if (width == null) {
      return "";
    }
    return width == Math.rint(width) ? String.valueOf((long) Math.rint(width)) : String.valueOf(width);
  }

  /** A whole number is stored as an integer (as a JavaScript export writes it), anything else as a decimal. */
  @JsonIgnore
  public void setWidth(Double width) {
    if (width == null) {
      widthNode = null;
    }
    else if (width == Math.rint(width)) {
      widthNode = IntNode.valueOf((int) Math.rint(width));
    }
    else {
      widthNode = DoubleNode.valueOf(width);
    }
  }

  /** Copies everything a column dialog edits from {@code edited} onto this column (its {@code id} and extras stay). */
  public void applyFrom(TreeColumn edited) {
    name = edited.name;
    label = edited.label;
    labelHidden = edited.labelHidden;
    icon = edited.icon;
    widthNode = edited.widthNode;
    alignment = edited.alignment;
    pinDirection = edited.pinDirection;
    fixedWidth = edited.fixedWidth;
    styles = edited.styles;
  }

  @JsonIgnore
  public boolean isPinned() {
    return pinDirection != null && !pinDirection.isEmpty();
  }

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }
}
