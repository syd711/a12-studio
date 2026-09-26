package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The "Initial Expansion" of the "Level by level" {@link ExpansionStrategy}: which levels of which node types the Tree
 * Engine expands on load. Present in the file exactly when SME's "Enable Initial Expansion" is checked.
 */
@Getter
@Setter
public class InitialExpansion {

  /** Expand every level of the affected node types. */
  public static final String ALL_LEVELS = "all_levels";
  /** Expand {@link #getLevel()} levels of the affected node types. */
  public static final String LEVEL_LIMIT = "level_limit";

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String type;
  // Only meaningful for LEVEL_LIMIT.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer level;
  // Ids of the node types to apply it to; absent (or empty) = all of them.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<String> affectedNodeRefs;

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
