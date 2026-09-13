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

@Getter
@Setter
public class TreeColumn {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private IconRef icon;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer width;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Map<String, Object> styles;
  private String id;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  private String name;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String pinDirection;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean fixedWidth;

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
