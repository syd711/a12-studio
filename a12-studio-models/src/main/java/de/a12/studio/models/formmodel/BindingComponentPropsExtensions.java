package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code propsExtensions} of a {@link BindingComponent} - SME's {@code I_BindingComponent.json}
 * {@code propsExtensions} group, holding the type-specific configuration for whichever
 * {@link BindingComponentType} the component actually uses.
 */
@Getter
@Setter
public class BindingComponentPropsExtensions {

  @JsonInclude(JsonInclude.Include.NON_NULL)
  private DualPaneProps dualPaneProps;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private TableListProps tableListProps;
}
