package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Add/edit dialog for a single {@link de.a12.studio.models.applicationmodel.Flow} entry of {@link
 * de.a12.studio.ui.editors.propertyeditors.FlowsPanelController}. Intentionally empty for now (no fields);
 * flow details are expected to be added to this dialog later.
 */
public class FlowDialogController implements DialogController {

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  void init(Stage stage) {
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
