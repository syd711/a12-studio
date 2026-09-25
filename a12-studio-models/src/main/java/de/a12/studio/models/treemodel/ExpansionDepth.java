package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * One entry of the "Tree" {@link ExpansionStrategy}: how many levels of the relationship model
 * {@link #getRelationshipModel()} the Tree Engine loads in a single query (SME {@code ExpansionDepth}).
 */
@Getter
@Setter
public class ExpansionDepth {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String relationshipModel;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer maxDepth;

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
