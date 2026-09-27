package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalesPanelController;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.ModelSuffixValidation;
import de.a12.studio.ui.util.NameConventionValidation;
import de.a12.studio.ui.util.ProjectModelFolders;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Modal dialog for {@link de.a12.studio.ui.editors.overviewmodel.OverviewReferencePanelController}'s "Add"
 * button next to the Query Model picker (gap 14 of "Overview Model: gap review" in {@code
 * docs/sme-reference-comparison.md"}): prompts for the new Query Model's name, target Document Model and
 * whether to seed its Fields/Paging/Sorting from the Overview Model being edited, mirroring {@link
 * CreateOverviewModelDialogController}'s shape. The actual seeding (reading the Overview's columns/
 * configuration and writing them as {@code QueryModelContent.fields}/{@code paging}/{@code sort}) happens in
 * the caller ({@code OverviewModelEditorController#onAddQueryModel}), which has that context; this dialog only
 * returns the flag.
 */
public class CreateQueryModelDialogController implements DialogController {

  public record Result(String name, String targetDocumentModelId, boolean generateFromOverview, List<Locale> locales,
      List<String> roles, ProjectItem folder) {
  }

  @FXML
  private TextField nameField;

  @FXML
  private ComboBox<ProjectItem> locationCombo;

  @FXML
  private ComboBox<String> targetDocumentModelCombo;

  @FXML
  private CheckBox generateFromOverviewField;

  @FXML
  private LocalesPanelController localesController;

  @FXML
  private RolesEditorPanelController rolesController;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  @FXML
  private ErrorContainerController errorContainerController;

  private Stage stage;

  private ProjectItem targetFolder;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    // Same reasoning as CreateOverviewModelDialogController: this dialog builds the model on submit,
    // outside the panel's own save flow, so RolesEditorPanelController's Deferred#flush() is never called -
    // only isEmbeddedInDialog()'s side effect (hiding the "Edit Roles" button) is needed here.
    rolesController.setSaveMode(new PropertyEditorSaveMode.Deferred());
    nameField.textProperty().addListener((observable, oldValue, newValue) -> validate());
    targetDocumentModelCombo.valueProperty().addListener((observable, oldValue, newValue) -> validate());
    nameField.requestFocus();
  }

  void init(Stage stage, @NonNull ProjectItem targetFolder, @NonNull List<DocumentModel> documentModels,
      String preselectedDocumentModelId, @NonNull List<Locale> defaultLocales, @NonNull String defaultName) {
    this.stage = stage;
    this.targetFolder = targetFolder;

    ProjectModelFolders.configureLocationCombo(locationCombo, targetFolder);
    nameField.setText(defaultName);
    targetDocumentModelCombo.getItems().setAll(
        documentModels.stream().map(DocumentModel::getId).sorted(Comparator.naturalOrder()).toList());
    targetDocumentModelCombo.setValue(preselectedDocumentModelId);
    generateFromOverviewField.setSelected(true);
    localesController.initializeLocales(defaultLocales);
    rolesController.initializeRoles(List.of());

    validate();
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

  Optional<Result> getResult() {
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return Optional.empty();
    }
    String name = nameField.getText();
    String targetDocumentModelId = targetDocumentModelCombo.getValue();
    if (name == null || name.isBlank() || targetDocumentModelId == null || targetDocumentModelId.isBlank()) {
      return Optional.empty();
    }
    ProjectItem folder = locationCombo.getValue();
    return Optional.of(new Result(name.trim(), targetDocumentModelId, generateFromOverviewField.isSelected(),
        localesController.getLocales(), rolesController.getRoles(), folder != null ? folder : targetFolder));
  }

  private void validate() {
    boolean hasTarget = targetDocumentModelCombo.getValue() != null;
    Optional<String> nameConventionError = NameConventionValidation.validate("Model name", nameField.getText());
    Optional<String> suffixError = nameConventionError.isPresent() || targetFolder == null ? Optional.empty()
        : ModelSuffixValidation.validate(targetFolder, ModelType.QUERY, nameField.getText());
    Optional<String> targetError = hasTarget ? Optional.empty() : Optional.of(StudioBundle.get("target_document_model_is_required"));
    nameConventionError.or(() -> suffixError).or(() -> targetError)
        .ifPresentOrElse(message -> errorContainerController.show("ERROR", message), errorContainerController::hide);
    okButton.setDisable(!FileUtils.isValidWindowsFilename(nameField.getText()) || !hasTarget
        || nameConventionError.isPresent() || suffixError.isPresent());
  }
}
