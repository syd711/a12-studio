package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code propsExtensions.dualPaneProps} of a {@link BindingComponent} whose {@link BindingComponentType} is
 * {@link BindingComponentType#DUAL_PANE_SELECTION} - SME's {@code I_BindingComponent.json}
 * {@code propsExtensions/dualPaneProps} group.
 */
@Getter
@Setter
public class DualPaneProps {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer height;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponentTableLabel availableItemsTable;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponentTableLabel selectedItemsTable;
}
