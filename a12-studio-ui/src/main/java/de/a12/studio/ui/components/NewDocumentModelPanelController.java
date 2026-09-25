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
 * Reusable panel for entering the name and location of a new model, together with its locales and
 * roles. Intended to be embedded via {@code fx:include} in dialogs that need to create a new model
 * as part of their flow (e.g. the generic "New Model" dialog, the Move Group dialog, the Access
 * import dialog, the Excel import dialog). Three of those four flows only ever create a Document
 * Model, so {@link #init(ProjectItem, String)} defaults to {@link ModelType#DOCUMENT}; the "New
 * Model" dialog passes the user's selected {@link ModelType} explicitly (see {@link #init(ProjectItem,
 * String, ModelType)} and {@link #setModelType(ModelType)}) so the filename-suffix convention and
 * default suggested name track whichever type is currently selected.
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

  private static final String DEFAULT_MODEL_BASE_NAME = "NewModel";

  @FXML
  private TextField modelNameField;

  @FXML
  private ComboBox<ProjectItem> locationCombo;

  @FXML
  private LocalesPanelController localesController;

  @FXML
  private RolesEditorPanelController rolesController;

  private ProjectItem targetFolder;

  /** Used for the filename-suffix convention and the default suggested name; see the class doc. */
  private ModelType modelType = ModelType.DOCUMENT;

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
   *                     to fall back to the default suggested name for {@link ModelType#DOCUMENT}
   */
  public void init(@NonNull ProjectItem targetFolder, @Nullable String defaultName) {
    init(targetFolder, defaultName, ModelType.DOCUMENT);
  }

  /**
   * Same as {@link #init(ProjectItem, String)}, but for a dialog that can create model types other
   * than Document Model (e.g. the generic "New Model" dialog) -- {@code modelType} drives the
   * filename-suffix convention and the default suggested name (see {@link #getSuffixError()}).
   */
  public void init(@NonNull ProjectItem targetFolder, @Nullable String defaultName, @NonNull ModelType modelType) {
    this.targetFolder = targetFolder;
    this.modelType = modelType;
    updateLocalesVisibility();
    ProjectModelFolders.configureLocationCombo(locationCombo, targetFolder);
    localesController.initializeLocales(DocumentModelBuilder.resolveDefaultLocales(targetFolder));
    rolesController.initializeRoles(RolesEditorPanelController.findApplicationModelRoles(targetFolder));
    modelNameField.setText(defaultName != null && !defaultName.isBlank() ? defaultName : defaultModelName());
    requestNameFocus();
  }

  /**
   * Updates the {@link ModelType} used for suffix validation and the default suggested name, without
   * touching any other field -- for a dialog whose type selection can change after {@link #init}
   * (e.g. the "New Model" dialog's type combo box).
   */
  public void setModelType(@NonNull ModelType modelType) {
    this.modelType = modelType;
    updateLocalesVisibility();
  }

  // A Typesetting Model's header has no locales, so there is nothing to ask for.
  private void updateLocalesVisibility() {
    localesController.setVisible(modelType != ModelType.TYPESETTING);
  }

  /** Sets the callback that is invoked whenever any field in this panel changes. */
  public void setOnChanged(@NonNull Runnable onChanged) {
    this.onChanged = onChanged;
  }

  /**
   * Overwrites the roles panel's current selection, e.g. reseeded by the owning dialog from a
   * selected Document Model's own roles rather than the application model's.
   */
  public void setRoles(@NonNull List<String> roles) {
    rolesController.initializeRoles(roles);
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
   * "Enforce Model Suffixes" rule for this panel's current {@link ModelType} (see {@link #init(ProjectItem,
   * String, ModelType)} / {@link #setModelType(ModelType)}), or empty otherwise.
   */
  public Optional<String> getSuffixError() {
    if (targetFolder == null) {
      return Optional.empty();
    }
    return ModelSuffixValidation.validate(targetFolder, modelType, modelNameField.getText());
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

  /**
   * Requests focus on the name field and selects everything up to (but not including) a trailing
   * model-suffix (e.g. {@code _DM}), if present, so the user can type over the meaningful part of
   * the suggested name while keeping the model suffix convention intact.
   */
  public void requestNameFocus() {
    modelNameField.requestFocus();
    String text = modelNameField.getText();
    String suffix = modelSuffixWithUnderscore();
    int selectionEnd = suffix != null && text.endsWith(suffix) ? text.length() - suffix.length() : text.length();
    modelNameField.selectRange(0, selectionEnd);
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

  /** E.g. {@code "NewModel_DM"} for {@link ModelType#DOCUMENT}, or just {@code "NewModel"} if the current
   *  {@link #modelType} has no configured suffix. */
  private String defaultModelName() {
    String suffix = modelSuffixWithUnderscore();
    return suffix != null ? DEFAULT_MODEL_BASE_NAME + suffix : DEFAULT_MODEL_BASE_NAME;
  }

  @Nullable
  private String modelSuffixWithUnderscore() {
    String suffix = modelType.getSuffix();
    return suffix != null ? "_" + suffix : null;
  }
}
