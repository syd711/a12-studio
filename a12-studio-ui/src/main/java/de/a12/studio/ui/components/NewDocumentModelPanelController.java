package de.a12.studio.ui.components;

import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalesPanelController;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.util.DocumentModelBuilder;
import de.a12.studio.ui.util.FileUtils;
import de.a12.studio.ui.util.ModelSuffixValidation;
import de.a12.studio.ui.util.NameConventionValidation;
import de.a12.studio.ui.util.ProjectModelFolders;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * Reusable panel for entering the name and location of a new Document Model, together with
 * its locales and roles. Intended to be embedded via {@code fx:include} in dialogs that need
 * to create a new Document Model as part of their flow (e.g. the Move Group dialog, the Access
 * import dialog, the Excel import dialog).
 *
 * <p>Call {@link #init(ProjectItem, String)} once the owning dialog's {@code targetFolder} is
 * known, then read {@link #getModelName()}, {@link #getFolder()}, {@link #getLocales()} and
 * {@link #getRoles()} when the dialog is confirmed.
 *
 * <p>Validation is delegated to the embedding dialog via {@link #isValid()}, which it should
 * call from its own {@code validate()} method and wire into the error container / OK-button
 * state. Any suffix violation message is exposed via {@link #getSuffixError()}.
 */
public class NewDocumentModelPanelController {

  @FXML
  private TextField modelNameField;

  @FXML
  private ComboBox<ProjectItem> locationCombo;

  @FXML
  private LocalesPanelController localesController;

  @FXML
  private RolesEditorPanelController rolesController;

  private ProjectItem targetFolder;

  /** Callback invoked on every keystroke / selection change so the owning dialog can re-validate. */
  private Runnable onChanged;

  @FXML
  private void initialize() {
    // RolesEditorPanelController hides its "Edit Roles" button when embedded in a dialog context.
    rolesController.setSaveMode(new PropertyEditorSaveMode.Deferred());
    modelNameField.textProperty().addListener((obs, old, val) -> notifyChanged());
    locationCombo.valueProperty().addListener((obs, old, val) -> notifyChanged());
  }

  /**
   * Wires the panel into the owning dialog's folder and seeds locales/roles from project defaults.
   *
   * @param targetFolder the project folder the new model will be created in
   * @param defaultName  optional pre-filled name (e.g. derived from the group being moved), or {@code null}
   */
  public void init(@NonNull ProjectItem targetFolder, @Nullable String defaultName) {
    this.targetFolder = targetFolder;
    ProjectModelFolders.configureLocationCombo(locationCombo, targetFolder);
    localesController.initializeLocales(DocumentModelBuilder.resolveDefaultLocales(targetFolder));
    rolesController.initializeRoles(RolesEditorPanelController.findApplicationModelRoles(targetFolder));
    if (defaultName != null && !defaultName.isBlank()) {
      modelNameField.setText(defaultName);
    }
  }

  /** Sets the callback that is invoked whenever any field in this panel changes. */
  public void setOnChanged(@NonNull Runnable onChanged) {
    this.onChanged = onChanged;
  }

  private void notifyChanged() {
    if (onChanged != null) {
      onChanged.run();
    }
  }

  /**
   * Returns the name-convention error message if the current name violates the model naming convention
   * (letters/digits/hyphens/underscores/periods, no "xml" prefix, at most 100 characters), or empty
   * otherwise. Checked before {@link #getSuffixError()} so the more fundamental violation takes
   * precedence when both apply.
   */
  public Optional<String> getNameConventionError() {
    return NameConventionValidation.validate("Model name", modelNameField.getText());
  }

  /**
   * Returns the suffix-violation error message if the current name violates the project's
   * "Enforce Model Suffixes" rule for {@link ModelType#DOCUMENT}, or empty otherwise.
   */
  public Optional<String> getSuffixError() {
    if (targetFolder == null) {
      return Optional.empty();
    }
    return ModelSuffixValidation.validate(targetFolder, ModelType.DOCUMENT, modelNameField.getText());
  }

  /**
   * Whether the panel's required fields are filled with valid values (valid filename, valid location,
   * no name-convention or suffix violation).
   */
  public boolean isValid() {
    return FileUtils.isValidWindowsFilename(modelNameField.getText())
        && locationCombo.getValue() != null
        && getNameConventionError().isEmpty()
        && getSuffixError().isEmpty();
  }

  public String getModelName() {
    return modelNameField.getText().trim();
  }

  public ProjectItem getFolder() {
    ProjectItem folder = locationCombo.getValue();
    return folder != null ? folder : targetFolder;
  }

  public List<Locale> getLocales() {
    return localesController.getLocales();
  }

  public List<String> getRoles() {
    return rolesController.getRoles();
  }

  /** Requests focus on the name field for convenient keyboard entry. */
  public void requestNameFocus() {
    modelNameField.requestFocus();
  }

  /**
   * Programmatically sets the model name field text, e.g. to auto-fill a suggested name from an
   * import wizard (file name, table name, etc.) without triggering the {@link #onChanged} callback.
   */
  public void setModelName(@NonNull String name) {
    modelNameField.setText(name);
  }

  /**
   * Whether the model name field still contains its initial/auto-filled value (i.e. the user
   * hasn't manually changed it yet). Useful for import wizards that want to keep updating the
   * suggested name as the user picks different source tables/sheets.
   */
  public boolean isModelNameAutoFilled(String autoFilledValue) {
    return autoFilledValue != null && autoFilledValue.equals(modelNameField.getText());
  }
}
