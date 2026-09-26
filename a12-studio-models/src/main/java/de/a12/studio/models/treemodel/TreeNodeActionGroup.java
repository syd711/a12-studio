package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One named group of a node's context menu. {@code type: "add"} marks SME's "add group", whose actions are all
 * insert actions; a plain group has no type.
 */
@Getter
@Setter
public class TreeNodeActionGroup {

  public static final String TYPE_ADD = "add";

  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String type;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> title = new ArrayList<>();
  private List<TreeNodeAction> actions = new ArrayList<>();

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonAnySetter
  public void setExtra(String name, Object value) {
    extras.put(name, value);
  }

  @JsonAnyGetter
  public Map<String, Object> getExtras() {
    return extras;
  }
}
