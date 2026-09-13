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
public class TreeNodeAction {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean primary;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean destructive;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean labelHidden;
  private String type;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String event;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private IconRef icon;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> description = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Confirmation confirmation;

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
