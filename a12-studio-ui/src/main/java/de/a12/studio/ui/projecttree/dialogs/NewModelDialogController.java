package de.a12.studio.ui.projecttree.dialogs;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.components.NewDocumentModelPanelController;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.util.ModelTypeLabels;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NewModelDialogController implements DialogController {

  public record NewModelInput(ModelType modelType, String name, String documentModelId, List<Locale> locales,
      List<String> roles, ProjectItem folder, boolean buildScreensFromFields) {
  }

  @FXML
  private ComboBox<ModelType> typeComboBox;

  @FXML
  private Label documentModelLabel;

  @FXML
  private ComboBox<String> documentModelCombo;

  @FXML
  private CheckBox buildScreensFromFieldsCheckBox;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  @FXML
  private NewDocumentModelPanelController commonFieldsController;

  @FXML
  private ErrorContainerController errorContainerController;

  private Stage stage;

  private ProjectItem targetFolder;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    buildScreensFromFieldsCheckBox.setDisable(true);
    typeComboBox.getItems().setAll(ModelType.values());
    typeComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(ModelType modelType) {
        return ModelTypeLabels.getDisplayName(modelType);
      }

      @Override
      public ModelType fromString(String string) {
        return null;
      }
    });
    typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      commonFieldsController.setModelType(newValue);
      updateDocumentModelVisibility(newValue);
      updateBuildScreensFromFieldsVisibility(newValue);
      if (!requiresDocumentModel(newValue)) {
        // Switching away from a document-model type: seed roles from the application model instead
        if (targetFolder != null) {
          commonFieldsController.setRoles(RolesEditorPanelController.findApplicationModelRoles(targetFolder));
        }
      } else {
        // Switching to a document-model type: seed roles from currently selected document model (if any)
        String selectedDocModel = documentModelCombo.getValue();
        if (selectedDocModel != null && !selectedDocModel.isBlank()) {
          onDocumentModelSelected(selectedDocModel);
        } else {
          commonFieldsController.setRoles(List.of());
        }
      }
      validate();
    });
    documentModelCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      onDocumentModelSelected(newValue);
      buildScreensFromFieldsCheckBox.setDisable(newValue == null || newValue.isBlank());
      validate();
    });
    commonFieldsController.setOnChanged(this::validate);
    typeComboBox.getSelectionModel().selectFirst();
  }

  // Combines the filename/document-model check the OK button already gated on with the panel's own
  // live "Enforce Model Suffixes" / model name convention checks, surfacing whichever message applies
  // in the dialog's error container per the "validator messages must name the field" convention.
  private void validate() {
    boolean missingDocumentModel = requiresDocumentModel(typeComboBox.getValue()) && documentModelCombo.getValue() == null;
    Optional<String> nameConventionError = commonFieldsController.getNameConventionError();
    Optional<String> suffixError = nameConventionError.isPresent() ? Optional.empty() : commonFieldsController.getSuffixError();
    nameConventionError.or(() -> suffixError).ifPresentOrElse(
        message -> errorContainerController.show("ERROR", message), errorContainerController::hide);
    okButton.setDisable(!commonFieldsController.isValid() || missingDocumentModel);
  }

  private void onDocumentModelSelected(String documentModelId) {
    if (documentModelId != null && !documentModelId.isBlank() && targetFolder != null) {
      List<String> roles = RolesEditorPanelController.findDocumentModelRoles(targetFolder, documentModelId);
      commonFieldsController.setRoles(roles);
    }
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

  // "Build Screens from Fields" only makes sense for Form Models (see FormScreenGenerator); unlike the
  // Overview Model, which also requires a document model but has no screen tree to generate.
  private void updateBuildScreensFromFieldsVisibility(ModelType modelType) {
    boolean visible = modelType == ModelType.FORM;
    buildScreensFromFieldsCheckBox.setVisible(visible);
    buildScreensFromFieldsCheckBox.setManaged(visible);
    if (!visible) {
      buildScreensFromFieldsCheckBox.setSelected(false);
    }
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
    controller.targetFolder = targetFolder;
    ModelType initialType = preselectedType != null ? preselectedType : controller.typeComboBox.getValue();
    controller.commonFieldsController.init(targetFolder, null, initialType);
    controller.documentModelCombo.getItems().setAll(ProjectDocumentModels.getOtherDocumentModels(targetFolder).stream()
        .map(DocumentModel::getId)
        .sorted(Comparator.naturalOrder())
        .toList());
    if (preselectedType != null) {
      // Fires the type listener (see #initialize), which re-seeds roles from the selected document
      // model instead when preselectedType requires one (e.g. FORM), overriding the application-model
      // roles #init just seeded.
      controller.typeComboBox.getSelectionModel().select(preselectedType);
    }
    controller.validate();
    WidgetFactory.installResizable(stage);
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      ModelType modelType = controller.typeComboBox.getValue();
      String name = controller.commonFieldsController.getModelName();
      if (modelType != null && !name.isBlank()) {
        String documentModelId = requiresDocumentModel(modelType) ? controller.documentModelCombo.getValue() : null;
        boolean buildScreensFromFields = modelType == ModelType.FORM && controller.buildScreensFromFieldsCheckBox.isSelected();
        return Optional.of(new NewModelInput(modelType, name, documentModelId, controller.commonFieldsController.getLocales(),
            controller.commonFieldsController.getRoles(), controller.commonFieldsController.getFolder(), buildScreensFromFields));
      }
    }
    return Optional.empty();
  }
}
