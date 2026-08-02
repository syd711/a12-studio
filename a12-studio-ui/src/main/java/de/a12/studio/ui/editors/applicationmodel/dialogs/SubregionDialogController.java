package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Add/edit dialog for a single {@link de.a12.studio.models.applicationmodel.Region} entry of {@link
 * de.a12.studio.ui.editors.applicationmodel.SubregionsPanelController}. Currently only edits the subregion name;
 * further subregion details (layout) are expected to be added to this dialog later.
 */
public class SubregionDialogController implements DialogController {

  @FXML
  private TextField nameField;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    okButton.disableProperty().bind(nameField.textProperty().map(name -> name == null || name.isBlank()));
    nameField.requestFocus();
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

  void init(Stage stage, String initialName) {
    this.stage = stage;
    nameField.setText(initialName);
  }

  Optional<String> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      String name = nameField.getText();
      if (name != null && !name.isBlank()) {
        return Optional.of(name.trim());
      }
    }
    return Optional.empty();
  }
}
