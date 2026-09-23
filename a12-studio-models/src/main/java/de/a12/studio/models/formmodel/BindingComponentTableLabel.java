package de.a12.studio.models.formmodel;

import com.fasterxml.jackson.annotation.JsonInclude;
import de.a12.studio.models.Label;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * A localized table title, wrapping a plain {@code label} list - the shape {@link DualPaneProps}'s
 * {@code availableItemsTable}/{@code selectedItemsTable} and {@link TableListProps}'s
 * {@code editDialogTitle}/{@code editDialogCancelButtonLabel}/{@code editDialogCloseButtonLabel} all share in
 * SME's {@code I_BindingComponent.json}.
 */
@Getter
@Setter
public class BindingComponentTableLabel {

  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private List<Label> label = new ArrayList<>();
}
