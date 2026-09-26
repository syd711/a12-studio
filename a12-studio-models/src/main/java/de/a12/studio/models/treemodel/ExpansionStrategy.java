package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ExpansionStrategy {

  public static final String LEVEL_BY_LEVEL = "level_by_level";
  public static final String TREE = "tree";

  private String type;

  // Only meaningful for the "tree" strategy; null (key absent) until one is written, an explicit [] is kept.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<ExpansionDepth> expansionDepths;

  // Only for the "level_by_level" strategy; present exactly when SME's "Enable Initial Expansion" is checked.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private InitialExpansion initialExpansion;

  // Only for the "level_by_level" strategy; present exactly when SME's "Enable Pagination" is checked.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer pageSize;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  /**
   * Switches to {@code newType} and drops what only the other strategy has, as SME does on export: "tree" keeps just
   * the expansion depths (an empty list is written for it), "level_by_level" just the initial expansion and page size.
   * Does nothing beyond setting the type when it doesn't change.
   */
  public void switchTypeTo(String newType) {
    boolean changed = newType != null && !newType.equals(type);
    type = newType;
    if (!changed) {
      return;
    }
    if (TREE.equals(newType)) {
      initialExpansion = null;
      pageSize = null;
      if (expansionDepths == null) {
        expansionDepths = new ArrayList<>();
      }
    }
    else if (LEVEL_BY_LEVEL.equals(newType)) {
      expansionDepths = null;
    }
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
