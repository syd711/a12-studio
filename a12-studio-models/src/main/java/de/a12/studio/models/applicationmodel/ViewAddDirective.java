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
@JsonPropertyOrder({"type", "region", "name", "constraints", "models", "configuration", "loadData"})
public class ViewAddDirective extends Directive {

  // The named region(s) of the current layout to add the view to, e.g. ["MODAL"] to pop the view up instead
  // of placing it in the layout's regular regions; unset means the layout's default region for this view.
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<String> region = new ArrayList<>();
  // Name of the UI component to show in the view; resolved by looking up a matching UI component registered
  // in the application code (e.g. "OverviewEngine", "FormEngine", "TreeEngine").
  private String name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Constraints constraints;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<ModelDescriptor> models = new ArrayList<>();
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Map<String, Object> configuration = new LinkedHashMap<>();
  // Only required to load data when the view isn't backed by any A12 model in `models`.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Boolean loadData;

  public ViewAddDirective() {
    setType(DirectiveType.VIEW_ADD);
  }
}
