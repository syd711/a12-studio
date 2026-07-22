package de.a12.studio.models.applicationmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@JsonPropertyOrder({"type", "name", "constraints", "models", "configuration"})
public class ViewAddDirective extends Directive {

  // Name of the UI component to show in the view; resolved by looking up a matching UI component registered
  // in the application code (e.g. "OverviewEngine", "FormEngine", "TreeEngine").
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Constraints constraints;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<ModelDescriptor> models = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, Object> configuration = new LinkedHashMap<>();

  public ViewAddDirective() {
    setType(DirectiveType.VIEW_ADD);
  }
}
