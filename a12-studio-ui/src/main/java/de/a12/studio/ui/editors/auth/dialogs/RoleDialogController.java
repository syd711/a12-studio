package de.a12.studio.ui.editors.auth.dialogs;

import de.a12.studio.models.auth.Role;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Add/edit dialog for a single {@link Role} entry of {@link de.a12.studio.ui.editors.auth.RolesEditorController}.
 * Does not touch {@link Role#getAccessRights()}, which is edited separately via the access rights list.
 */
public class RoleDialogController implements DialogController {

  @FXML
  private TextField nameField;

  @FXML
  private TextField descriptionField;

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

  void init(Stage stage, Role role) {
    this.stage = stage;
    nameField.setText(role.getName());
    descriptionField.setText(role.getDescription());
  }

  boolean applyResultTo(Role role) {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      role.setName(nameField.getText().trim());
      String description = descriptionField.getText();
      role.setDescription(description == null || description.isBlank() ? null : description.trim());
      return true;
    }
    return false;
  }
}
