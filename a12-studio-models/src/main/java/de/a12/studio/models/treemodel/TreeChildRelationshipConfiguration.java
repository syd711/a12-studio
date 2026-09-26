package de.a12.studio.models.treemodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * How a node type finds its children: a relationship model plus the role the node itself plays in it
 * ({@code parentRole}). {@link #getColumns()} maps tree columns to fields of the relationship's link Document
 * Model and is shown on the child nodes; some files omit the key and others write an explicit {@code []}, so it
 * stays {@code null} until something is mapped.
 */
@Getter
@Setter
public class TreeChildRelationshipConfiguration {

  private String id;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String relationshipModelRef;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private String parentRole;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private List<TreeNodeColumn> columns;

  private final Map<String, Object> extras = new LinkedHashMap<>();

  @JsonIgnore
  public List<TreeNodeColumn> getOrCreateColumns() {
    if (columns == null) {
      columns = new ArrayList<>();
    }
    return columns;
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
