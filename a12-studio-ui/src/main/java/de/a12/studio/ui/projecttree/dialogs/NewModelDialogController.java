package de.a12.studio.ui.projecttree.dialogs;

import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalesPanelController;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.util.DocumentModelBuilder;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.ModelSuffixValidation;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.ProjectModelFolders;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
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
  private TextField nameField;

  @FXML
  private ComboBox<ProjectItem> locationCombo;

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
  private LocalesPanelController localesController;

  @FXML
  private RolesEditorPanelController rolesController;

  @FXML
  private ErrorContainerController errorContainerController;

  private Stage stage;

  private ProjectItem targetFolder;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    // Signals RolesEditorPanelController that it's embedded in a dialog, so it hides its "Edit Roles"
    // button (this dialog builds the model on submit, outside the panel's own save flow, so
    // Deferred#flush() is never called -- only isEmbeddedInDialog()'s side effect is needed here).
    rolesController.setSaveMode(new PropertyEditorSaveMode.Deferred());
    buildScreensFromFieldsCheckBox.setDisable(true);
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
    typeComboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      updateDocumentModelVisibility(newValue);
      updateBuildScreensFromFieldsVisibility(newValue);
      if (!requiresDocumentModel(newValue)) {
        // Switching away from a document-model type: seed roles from the application model instead
        if (targetFolder != null) {
          rolesController.initializeRoles(RolesEditorPanelController.findApplicationModelRoles(targetFolder));
        }
      } else {
        // Switching to a document-model type: seed roles from currently selected document model (if any)
        String selectedDocModel = documentModelCombo.getValue();
        if (selectedDocModel != null && !selectedDocModel.isBlank()) {
          onDocumentModelSelected(selectedDocModel);
        } else {
          rolesController.initializeRoles(List.of());
        }
      }
      validate();
    });
    documentModelCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      onDocumentModelSelected(newValue);
      buildScreensFromFieldsCheckBox.setDisable(newValue == null || newValue.isBlank());
      validate();
    });
    nameField.textProperty().addListener((observable, oldValue, newValue) -> validate());
    typeComboBox.getSelectionModel().selectFirst();
    nameField.requestFocus();
  }

  // Combines the filename/document-model checks the OK button already gated on with the live
  // "Enforce Model Suffixes" check (see ModelSuffixValidation), surfacing the latter's message in the
  // dialog's error container per the "validator messages must name the field" convention.
  private void validate() {
    boolean validFilename = FileUtils.isValidWindowsFilename(nameField.getText());
    boolean missingDocumentModel = requiresDocumentModel(typeComboBox.getValue()) && documentModelCombo.getValue() == null;
    Optional<String> suffixError = targetFolder == null ? Optional.empty()
        : ModelSuffixValidation.validate(targetFolder, typeComboBox.getValue(), nameField.getText());
    suffixError.ifPresentOrElse(message -> errorContainerController.show("ERROR", message), errorContainerController::hide);
    okButton.setDisable(!validFilename || missingDocumentModel || suffixError.isPresent());
  }

  private void onDocumentModelSelected(String documentModelId) {
    if (documentModelId != null && !documentModelId.isBlank() && targetFolder != null) {
      List<String> roles = RolesEditorPanelController.findDocumentModelRoles(targetFolder, documentModelId);
      rolesController.initializeRoles(roles);
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
    ProjectModelFolders.configureLocationCombo(controller.locationCombo, targetFolder);
    controller.localesController.initializeLocales(findProjectLocales());
    controller.documentModelCombo.getItems().setAll(ProjectDocumentModels.getOtherDocumentModels(targetFolder).stream()
        .map(DocumentModel::getId)
        .sorted(Comparator.naturalOrder())
        .toList());
    // For model types that require a document model (e.g. FORM), roles are seeded from the selected
    // document model via onDocumentModelSelected; skip the application model lookup for those types.
    if (preselectedType == null || !requiresDocumentModel(preselectedType)) {
      controller.rolesController.initializeRoles(RolesEditorPanelController.findApplicationModelRoles(targetFolder));
    }
    if (preselectedType != null) {
      controller.typeComboBox.getSelectionModel().select(preselectedType);
    }
    controller.validate();
    WidgetFactory.installResizable(stage);
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      ModelType modelType = controller.typeComboBox.getValue();
      String name = controller.nameField.getText();
      if (modelType != null && name != null && !name.isBlank()) {
        String documentModelId = requiresDocumentModel(modelType) ? controller.documentModelCombo.getValue() : null;
        ProjectItem folder = controller.locationCombo.getValue();
        boolean buildScreensFromFields = modelType == ModelType.FORM && controller.buildScreensFromFieldsCheckBox.isSelected();
        return Optional.of(new NewModelInput(modelType, name.trim(), documentModelId, controller.localesController.getLocales(),
            controller.rolesController.getRoles(), folder != null ? folder : targetFolder, buildScreensFromFields));
      }
    }
    return Optional.empty();
  }

  // Seeds the locales panel from the project's own settings.json (general.locales), mirroring
  // NewModelFactory#resolveDefaultLocales -- so what the dialog shows already matches what the model would
  // get if the user left the panel untouched. Falls back to the JVM's system locale (see
  // DocumentModelBuilder#systemLocaleFallback) when the project has none configured yet.
  private static List<Locale> findProjectLocales() {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return DocumentModelBuilder.systemLocaleFallback();
    }
    List<Locale> locales = project.getSettings().getProjectRootSettings().getGeneral().getLocales();
    return locales.isEmpty() ? DocumentModelBuilder.systemLocaleFallback() : locales;
  }
}
