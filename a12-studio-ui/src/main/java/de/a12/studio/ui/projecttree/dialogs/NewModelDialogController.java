package de.a12.studio.ui.projecttree.dialogs;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import javafx.beans.binding.Bindings;
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

import java.util.Comparator;
import java.util.Optional;

public class NewModelDialogController implements DialogController {

  public record NewModelInput(ModelType modelType, String name, String documentModelId) {
  }

  @FXML
  private ComboBox<ModelType> typeComboBox;

  @FXML
  private TextField nameField;

  @FXML
  private Label pathLabel;

  @FXML
  private Label documentModelLabel;

  @FXML
  private ComboBox<String> documentModelCombo;

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
    typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> updateDocumentModelVisibility(newValue));
    typeComboBox.getSelectionModel().selectFirst();
    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> !FileUtils.isValidWindowsFilename(nameField.getText())
            || (requiresDocumentModel(typeComboBox.getValue()) && documentModelCombo.getValue() == null),
        nameField.textProperty(), typeComboBox.valueProperty(), documentModelCombo.valueProperty()));
    nameField.requestFocus();
  }

  private static boolean requiresDocumentModel(ModelType modelType) {
    return modelType == ModelType.FORM || modelType == ModelType.OVERVIEW;
  }

  private void updateDocumentModelVisibility(ModelType modelType) {
    boolean visible = requiresDocumentModel(modelType);
    documentModelLabel.setVisible(visible);
    documentModelLabel.setManaged(visible);
    documentModelCombo.setVisible(visible);
    documentModelCombo.setManaged(visible);
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
    controller.pathLabel.setTooltip(WidgetFactory.createTooltip(targetFolder.getPath()));
    controller.documentModelCombo.getItems().setAll(ProjectDocumentModels.getOtherDocumentModels(targetFolder).stream()
        .map(DocumentModel::getId)
        .sorted(Comparator.naturalOrder())
        .toList());
    if (preselectedType != null) {
      controller.typeComboBox.getSelectionModel().select(preselectedType);
    }
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      ModelType modelType = controller.typeComboBox.getValue();
      String name = controller.nameField.getText();
      if (modelType != null && name != null && !name.isBlank()) {
        String documentModelId = requiresDocumentModel(modelType) ? controller.documentModelCombo.getValue() : null;
        return Optional.of(new NewModelInput(modelType, name.trim(), documentModelId));
      }
    }
    return Optional.empty();
  }
}
