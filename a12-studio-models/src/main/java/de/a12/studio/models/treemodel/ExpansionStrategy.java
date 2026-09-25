package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

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
