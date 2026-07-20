package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Add/edit dialog for a single {@link de.a12.studio.models.applicationmodel.Module} entry of {@link
 * de.a12.studio.ui.editors.propertyeditors.ModulesPanelController}. Currently only edits the module name;
 * further module details (menu, flows) are expected to be added to this dialog later.
 */
public class ModuleDialogController implements DialogController {

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

  public static Optional<String> showForAdd(Stage owner) {
    return show(owner, "Add Module", "");
  }

  public static Optional<String> showForEdit(Stage owner, String currentName) {
    return show(owner, "Edit Module", currentName);
  }

  private static Optional<String> show(Stage owner, String title, String initialName) {
    FXMLLoader fxmlLoader = new FXMLLoader(ModuleDialogController.class.getResource("module-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-module", fxmlLoader, owner, title);
    ModuleDialogController controller = (ModuleDialogController) stage.getUserData();
    controller.stage = stage;
    controller.nameField.setText(initialName);
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      String name = controller.nameField.getText();
      if (name != null && !name.isBlank()) {
        return Optional.of(name.trim());
      }
    }
    return Optional.empty();
  }
}
