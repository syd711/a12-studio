package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

/**
 * Modal dialog for editing a single {@link Column}, opened from {@link
 * de.a12.studio.ui.editors.propertyeditors.OverviewColumnsPanelController} by clicking a column row. Content
 * is intentionally empty for now - the Field/Sortable/Width/Pin Direction/etc. controls are a follow-up.
 */
public class OverviewColumnDialogController implements DialogController {

  private Stage stage;

  public void initDialog(Stage stage, ElementIndex documentModelIndex, @NonNull Column column) {
    this.stage = stage;
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    stage.close();
  }
}
