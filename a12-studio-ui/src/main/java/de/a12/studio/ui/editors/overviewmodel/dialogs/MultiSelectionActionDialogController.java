package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Add/edit dialog for a single multi-selection {@link Button} action, opened from {@link
 * de.a12.studio.ui.editors.overviewmodel.OverviewMultiSelectionPanelController} by clicking a row or its
 * Edit button, or by the Add button. Content is intentionally empty for now - the Event/Priority/Destructive/
 * Icon/etc. controls are a follow-up, matching {@link OverviewColumnDialogController}.
 */
public class MultiSelectionActionDialogController implements DialogController {

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  public void initDialog(Stage stage, @NonNull Button button) {
    this.stage = stage;
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
