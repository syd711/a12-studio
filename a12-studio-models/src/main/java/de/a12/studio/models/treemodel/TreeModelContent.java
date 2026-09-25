package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.overviewmodel.ElementBox;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class TreeModelContent {

  private TreeConfiguration configuration;
  // Same box structure as the Overview Model's; the Subheader takes Button, Multi-Selection and Expand All
  // PopUp elements, the Footer buttons only.
  private ElementBox subHeaderBox;
  private ElementBox footerBox;
  private List<TreeNode> nodes = new ArrayList<>();
  private List<TreeColumn> columns = new ArrayList<>();
  // Logical style names usable by the tree's controls; absent from the JSON when empty.
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
