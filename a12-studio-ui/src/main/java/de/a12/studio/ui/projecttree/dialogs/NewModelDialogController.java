package de.a12.studio.ui.projecttree.dialogs;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class NewModelDialogController implements DialogController {

  public record NewModelInput(ModelType modelType, String name) {
  }

  @FXML
  private ComboBox<ModelType> typeComboBox;

  @FXML
  private TextField nameField;

  @FXML
  private Label pathLabel;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    typeComboBox.getItems().setAll(ModelType.values());
    typeComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(ModelType modelType) {
        return modelType == null ? "" : modelType.getDisplayName();
      }

      @Override
      public ModelType fromString(String string) {
        return null;
      }
    });
    typeComboBox.getSelectionModel().selectFirst();
    okButton.disableProperty().bind(nameField.textProperty().map(name -> !FileUtils.isValidWindowsFilename(name)));
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

  public static Optional<NewModelInput> show(Stage owner, @NonNull ProjectItem targetFolder) {
    return show(owner, targetFolder, null);
  }

  public static Optional<NewModelInput> show(Stage owner, @NonNull ProjectItem targetFolder, ModelType preselectedType) {
    FXMLLoader fxmlLoader = new FXMLLoader(NewModelDialogController.class.getResource("dialog-new-model.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("dialog-new-model", fxmlLoader, owner, "New Model");
    NewModelDialogController controller = (NewModelDialogController) stage.getUserData();
    controller.stage = stage;
    controller.pathLabel.setText(targetFolder.getPath());
    if (preselectedType != null) {
      controller.typeComboBox.getSelectionModel().select(preselectedType);
    }
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      ModelType modelType = controller.typeComboBox.getValue();
      String name = controller.nameField.getText();
      if (modelType != null && name != null && !name.isBlank()) {
        return Optional.of(new NewModelInput(modelType, name.trim()));
      }
    }
    return Optional.empty();
  }
}
