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
 * Add/edit dialog for a single {@link de.a12.studio.models.applicationmodel.Region} entry of {@link
 * de.a12.studio.ui.editors.propertyeditors.SubregionsPanelController}. Currently only edits the subregion name;
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

  public static Optional<String> showForAdd(Stage owner) {
    return show(owner, "Add Subregion", "");
  }

  public static Optional<String> showForEdit(Stage owner, String currentName) {
    return show(owner, "Edit Subregion", currentName);
  }

  private static Optional<String> show(Stage owner, String title, String initialName) {
    FXMLLoader fxmlLoader = new FXMLLoader(SubregionDialogController.class.getResource("subregion-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-subregion", fxmlLoader, owner, title);
    SubregionDialogController controller = (SubregionDialogController) stage.getUserData();
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
