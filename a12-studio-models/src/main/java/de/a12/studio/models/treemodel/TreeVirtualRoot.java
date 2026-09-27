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
 * SME's "Virtual Root" ({@code configuration.virtualRoot}): a node drawn as the first row of the tree, with its own
 * {@link #getLabel() label} (shown in the hierarchical column), row {@link #getActions() actions} and {@link
 * #getContextMenu() context menu}. The key is present while the feature is enabled (SME's "Enable Virtual Root" is not
 * stored). Its insert actions always insert as child. Without any node the row is not drawn, so an "add" group in the
 * context menu is how the first node gets created.
 */
@Getter
@Setter
public class TreeVirtualRoot {

  private List<Label> label = new ArrayList<>();
  private List<TreeNodeAction> actions = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TreeNodeContextMenu contextMenu;

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
