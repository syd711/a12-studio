package de.a12.studio.ui.editors.propertyeditors.dialogs;

import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Modal dialog for creating/editing an enumeration {@link de.a12.studio.models.documentmodel.Category}'s
 * name and description.
 */
public class CategoryDialogController implements DialogController {

  @FXML
  private TextField nameField;

  @FXML
  private TextField descriptionField;

  @FXML
  private Button okButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  public void initDialog(Stage stage, String name, String description) {
    this.stage = stage;
    nameField.setText(name == null ? "" : name);
    descriptionField.setText(description == null ? "" : description);
    okButton.disableProperty().bind(nameField.textProperty().isEmpty());
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

  public Optional<ButtonType> getResult() {
    return result;
  }

  public String getName() {
    return nameField.getText();
  }

  public String getDescription() {
    return descriptionField.getText();
  }
}
