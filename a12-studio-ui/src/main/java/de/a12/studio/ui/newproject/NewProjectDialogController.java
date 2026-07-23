package de.a12.studio.ui.newproject;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.components.StudioFolderChooser;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.ProjectTemplates;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.File;
import java.util.Optional;

public class NewProjectDialogController implements DialogController {

  @FXML
  private ComboBox<String> templateComboBox;

  @FXML
  private TextField nameField;

  @FXML
  private TextField locationField;

  @FXML
  private Button createButton;

  @FXML
  private Button cancelButton;

  private Stage stage;
  private File selectedLocation;
  private File createdProjectFolder;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    templateComboBox.getItems().setAll(ProjectTemplates.listTemplateNames());
    templateComboBox.getSelectionModel().selectFirst();

    createButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> !FileUtils.isValidWindowsFilename(nameField.getText()) || locationField.getText() == null || locationField.getText().isBlank(),
        nameField.textProperty(), locationField.textProperty()));

    nameField.requestFocus();
  }

  @FXML
  private void onBrowseLocation() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle("Select Project Location");
    File folder = chooser.showOpenDialog(stage);
    if (folder != null) {
      selectedLocation = folder;
      locationField.setText(folder.getAbsolutePath());
    }
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    String templateName = templateComboBox.getValue();
    String name = nameField.getText() == null ? "" : nameField.getText().trim();
    if (templateName == null || !FileUtils.isValidWindowsFilename(name) || selectedLocation == null) {
      return;
    }

    File targetFolder = new File(selectedLocation, name);
    if (targetFolder.exists()) {
      WidgetFactory.showAlert(stage, "A folder named \"" + name + "\" already exists at the selected location.");
      return;
    }

    if (!targetFolder.mkdirs()) {
      WidgetFactory.showAlert(stage, "Failed to create the project folder \"" + targetFolder.getAbsolutePath() + "\".");
      return;
    }

    if (!ProjectTemplates.install(templateName, targetFolder)) {
      WidgetFactory.showAlert(stage, "Failed to extract the project template \"" + templateName + "\".");
      return;
    }

    createdProjectFolder = targetFolder;
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  public static Optional<File> show(Stage owner) {
    FXMLLoader fxmlLoader = new FXMLLoader(NewProjectDialogController.class.getResource("dialog-new-project.fxml"));
    Stage stage = WidgetFactory.createDialogStage(null, fxmlLoader, owner, "New Project");
    NewProjectDialogController controller = (NewProjectDialogController) stage.getUserData();
    controller.stage = stage;
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      return Optional.ofNullable(controller.createdProjectFolder);
    }
    return Optional.empty();
  }
}
