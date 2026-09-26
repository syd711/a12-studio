package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Annotation;
import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.Confirmation;
import de.a12.studio.models.overviewmodel.Icon;
import de.a12.studio.models.overviewmodel.OverviewButtonLike;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One action of a tree node: a row action ({@code TreeNode.actions}) or an entry of a context-menu group.
 * Either an {@code event} action (fires {@link #getEvent()}) or an {@code insert} action (inserts a new node,
 * SME's {@code TreeNodeInsertActionButton}) - see {@link #TYPE_EVENT}/{@link #TYPE_INSERT}. Shares the button shape
 * ({@link OverviewButtonLike}) with the Overview Model's buttons, so the same Icon/Priority/Confirmation editors
 * apply. The insert-only fields are absent on an event action.
 */
@Getter
@Setter
public class TreeNodeAction implements OverviewButtonLike {

  public static final String TYPE_EVENT = "event";
  public static final String TYPE_INSERT = "insert";

  public static final String POSITION_AS_CHILD = "as_child";
  public static final String POSITION_ABOVE = "above";
  public static final String POSITION_BELOW = "below";

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean primary;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean destructive;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  private String type;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String event;
  // Insert actions only: where the new node goes relative to the selected one, and which Document Model it is
  // created from (absent = the default one).
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String position;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String documentModelRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean hasDocumentModelRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean useGlobalIcon;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean useLabelFromDocumentModel;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean useTitleFromDocumentModel;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> description = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Confirmation confirmation;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> styles = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Annotation> annotations = new ArrayList<>();

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @Override
  @JsonIgnore
  public String getIconName() {
    return icon != null ? icon.getName() : null;
  }

  @Override
  @JsonIgnore
  public void setIconName(String name) {
    if (name == null || name.isEmpty()) {
      icon = null;
      return;
    }
    if (icon == null) {
      icon = new Icon();
    }
    icon.setName(name);
  }

  @JsonIgnore
  public boolean isInsert() {
    return TYPE_INSERT.equals(type);
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
