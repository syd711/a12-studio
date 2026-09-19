package de.a12.studio.ui.editors.formmodel.formtree.nodeeditors;

import de.a12.studio.models.formmodel.RepeatOverviewColumn;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.formmodel.StylesPanelController;
import javafx.fxml.FXML;
import javafx.scene.control.TitledPane;
import org.jspecify.annotations.NonNull;

/**
 * "Header Styles" property editor for a selected {@link RepeatOverviewColumn}: delegates to the shared {@link
 * StylesPanelController} bound to {@link RepeatOverviewColumn#getHeaderStyle()}, the CSS style classes applied
 * to this column's header cell. The column counterpart of {@link RepeatHeaderStylesPanelController}, which
 * does the same for a whole repeat's header row.
 */
public class RepeatColumnHeaderStylesPanelController {

  @FXML
  private TitledPane root;
  @FXML
  private StylesPanelController headerStylesListController;

  @FXML
  private void initialize() {
    AbstractPropertyEditor.persistExpandedState(root, getClass());
  }

  public void setColumn(@NonNull RepeatOverviewColumn column) {
    headerStylesListController.setCustom(column::getHeaderStyle, column::getHeaderStyle);
  }
}
