package de.a12.studio.ui.projecttree;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.dataservices.models.ModelType;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.Optional;

public class NewModelDialogController implements DialogController {

  public record NewModelInput(ModelType modelType, String name) {
  }

  @FXML
  private ComboBox<ModelType> typeComboBox;

  @FXML
  private TextField nameField;

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

  public static Optional<NewModelInput> show(Stage owner) {
    FXMLLoader fxmlLoader = new FXMLLoader(NewModelDialogController.class.getResource("dialog-new-model.fxml"));
    Stage stage = WidgetFactory.createDialogStage("dialog-new-model", fxmlLoader, owner, "New Model");
    NewModelDialogController controller = (NewModelDialogController) stage.getUserData();
    controller.stage = stage;
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
