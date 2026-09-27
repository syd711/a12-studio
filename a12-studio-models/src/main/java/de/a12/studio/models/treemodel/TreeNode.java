package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import de.a12.studio.models.overviewmodel.Icon;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * One node type of a Tree Model. {@code configuration} keeps SME's free-form flags ({@code dnd}, {@code inherit},
 * {@code showInherit}); a node whose Document Model is a sub type of another node's may inherit the columns,
 * child relationship configurations, icon, actions, context menu, row activation, row title and styles of
 * that node instead of defining its own, see {@code configuration.inherit}.
 */
@Getter
@Setter
public class TreeNode {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Object> configuration;
  private String id;
  private List<TreeNodeAction> actions = new ArrayList<>();
  private String documentModelRef;
  private List<TreeNodeColumn> columns = new ArrayList<>();
  private List<TreeChildRelationshipConfiguration> childRelationshipConfigurations = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Icon icon;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TreeNodeContextMenu contextMenu;
  // Absent = the Tree Engine's default behavior (view/edit), see RowActivation. (Tree model 11.0.0 replaced the
  // former "defaultRowAction"; a file still carrying that key keeps it in extras, migrating is not done.)
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private RowActivation rowActivation;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> rowTitle = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> styles = new ArrayList<>();

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
