package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A {@link BindingDetails#getMainComponent()}/{@link BindingDetails#getEditModalComponent()}: which widget
 * (drop-down, dual-pane selection, or an editable table-list) renders a {@link Binding}'s candidates/links, and
 * that widget's own configuration - SME's {@code I_BindingComponent.json} (referenced from {@code
 * client/src/modules/formModel/fmElements/types/binding.ts}'s {@code Component}).
 */
@Getter
@Setter
public class BindingComponent {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String id;
  private BindingComponentType name;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponentModelsSme modelsSME;
  // Page size of the candidate ("available items") list - only meaningful for DropDownSelection/DualPaneSelection.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer candidatePageSize;
  // Page size of the already-linked ("selected items") list - only meaningful for DualPaneSelection.
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer linkPageSize;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponentPropsExtensions propsExtensions;

  // Everything SME's Component carries that isn't modeled above (e.g. "models", generic "props"), kept
  // verbatim so a load-then-save doesn't drop it.
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
