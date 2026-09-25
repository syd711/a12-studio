package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class TreeConfiguration {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Object> dnd;
  // Points at the id of one of the nodes' childRelationshipConfigurations: the relationship that yields the
  // tree's root nodes (SME "Root").
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String rootRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String hierarchicalColumnRef;
  // SME writes this as `true` or omits it ("Hide Label"), never `false`.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private ExpansionStrategy expansionStrategy;

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
