package de.a12.studio.ui.projecttree.dialogs;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.ProjectModelFolders;
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
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class NewModelDialogController implements DialogController {

  public record NewModelInput(ModelType modelType, String name, String documentModelId, List<String> roles,
      ProjectItem folder, boolean buildScreensFromFields) {
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
  private RolesEditorPanelController rolesController;

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
    });
    documentModelCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      onDocumentModelSelected(newValue);
      buildScreensFromFieldsCheckBox.setDisable(newValue == null || newValue.isBlank());
    });
    typeComboBox.getSelectionModel().selectFirst();
    okButton.disableProperty().bind(Bindings.createBooleanBinding(
        () -> !FileUtils.isValidWindowsFilename(nameField.getText())
            || (requiresDocumentModel(typeComboBox.getValue()) && documentModelCombo.getValue() == null),
        nameField.textProperty(), typeComboBox.valueProperty(), documentModelCombo.valueProperty()));
    nameField.requestFocus();
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
    setupLocationCombo(controller, targetFolder);
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
    WidgetFactory.installResizable(stage);
    // The dialog's content height is inherently variable (roles list length, document-model
    // combo visibility), so a persisted height from a previous, shorter-content session can no
    // longer fit - sizeToScene() re-fits the window to the actual (CSS-applied) content instead
    // of trusting installResizable's pre-show, unstyled minHeight(-1) estimate to catch this.
    stage.sizeToScene();
    stage.showAndWait();

    if (controller.result.isPresent() && controller.result.get() == ButtonType.OK) {
      ModelType modelType = controller.typeComboBox.getValue();
      String name = controller.nameField.getText();
      if (modelType != null && name != null && !name.isBlank()) {
        String documentModelId = requiresDocumentModel(modelType) ? controller.documentModelCombo.getValue() : null;
        ProjectItem folder = controller.locationCombo.getValue();
        boolean buildScreensFromFields = modelType == ModelType.FORM && controller.buildScreensFromFieldsCheckBox.isSelected();
        return Optional.of(new NewModelInput(modelType, name.trim(), documentModelId, controller.rolesController.getRoles(),
            folder != null ? folder : targetFolder, buildScreensFromFields));
      }
    }
    return Optional.empty();
  }

  // Populates the "Location" combo with every folder in the project, defaulting to the folder named
  // "models" (matching this project's own New Model default-folder convention, see
  // ProjectModelFolders#resolveDefaultModelFolder) if one exists anywhere in the tree, else the project
  // root itself (always first in the sorted list ProjectModelFolders#listAllFolders returns).
  private static void setupLocationCombo(NewModelDialogController controller, ProjectItem targetFolder) {
    ProjectItem projectRoot = targetFolder;
    while (projectRoot.getParent() != null) {
      projectRoot = projectRoot.getParent();
    }
    ProjectItem root = projectRoot;

    List<ProjectItem> folders = ProjectModelFolders.listAllFolders(root);
    controller.locationCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(ProjectItem folder) {
        return folder == null ? "" : displayLocation(root, folder);
      }

      @Override
      public ProjectItem fromString(String string) {
        return null;
      }
    });
    controller.locationCombo.getItems().setAll(folders);
    ProjectItem defaultFolder = folders.stream()
        .filter(folder -> folder.getName().equalsIgnoreCase("models"))
        .findFirst()
        .orElse(folders.get(0));
    controller.locationCombo.setValue(defaultFolder);
  }

  private static String displayLocation(ProjectItem projectRoot, ProjectItem folder) {
    if (folder.equals(projectRoot)) {
      return "/";
    }
    Path relative = projectRoot.getFile().toPath().toAbsolutePath().relativize(folder.getFile().toPath().toAbsolutePath());
    return relative.toString().replace(File.separatorChar, '/');
  }
}
