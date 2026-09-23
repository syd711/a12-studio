package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;

/**
 * {@code propsExtensions.tableListProps} of a {@link BindingComponent} whose {@link BindingComponentType} is
 * {@link BindingComponentType#TABLE_LIST} - SME's {@code I_BindingComponent.json}
 * {@code propsExtensions/tableListProps} group.
 */
@Getter
@Setter
public class TableListProps {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String editComponent;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private Integer editDialogWidth;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponentTableLabel editDialogTitle;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponentTableLabel editDialogCancelButtonLabel;
  @JsonInclude(JsonInclude.Include.NON_NULL)
  private BindingComponentTableLabel editDialogCloseButtonLabel;
}
