package de.a12.studio.ui.editors;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.ui.util.Debouncer;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.dataservices.services.documentmodel.features.validation.DMValidationService;
import de.a12.studio.dataservices.services.documentmodel.features.validation.ElementValidationError;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

@Slf4j
abstract public class AbstractPropertyEditor implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

  private static final DMValidationService VALIDATION_SERVICE = new DMValidationService();

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TitledPane root;

  @FXML
  private ErrorContainerController errorContainerController;

  // Non-null while this editor is bound to a single Element via setElement(). Subclasses that also support
  // editing a whole model's header (via their own setModel()) leave this null in that mode, which disables
  // the per-element validation/error-container plumbing below (there is no single Element to validate) while
  // still saving on every change like the Element-bound case.
  protected Element element;

  // Set while a field's value is being repopulated from the model (e.g. on element selection), so those
  // programmatic updates don't get mistaken for user edits and trigger a save/validation cycle.
  private boolean updatingFromModel;

  // Appended to the expanded-state settings key so multiple instances of the same controller class (e.g. a
  // panel reused for both an internal and external variant) don't share persisted expanded/collapsed state.
  private String settingsKeySuffix = "";

  // Immediate by default: a commit is persisted right away. Panels embedded in a dialog with its own Save
  // button are switched to a shared PropertyEditorSaveMode.Deferred instance via setSaveMode(), so their
  // commits are only persisted once that button is pressed.
  private PropertyEditorSaveMode saveMode = PropertyEditorSaveMode.IMMEDIATE;

  public void setSaveMode(@NonNull PropertyEditorSaveMode saveMode) {
    this.saveMode = saveMode;
  }

  public void setElement(@NonNull Element element) {
    this.element = element;
    showValidationError(null);
  }

  /**
   * Puts this panel into (or out of) read-only mode by disabling every control in its content area. JavaFX
   * propagates the disabled state to all descendants, so this covers rows added dynamically later (e.g. by
   * an "Add" button) as well. The TitledPane's own expand/collapse toggle is left usable.
   */
  public void setEditorDisabled(boolean disabled) {
    root.getContent().setDisable(disabled);
  }

  /**
   * Shows or hides this panel's whole {@link TitledPane}, e.g. for panels that only make sense for some
   * field types and would otherwise render as an empty, title-only pane. {@code setManaged} is toggled
   * alongside {@code setVisible} so the hidden pane doesn't still reserve layout space.
   */
  protected void setEditorVisible(boolean visible) {
    root.setVisible(visible);
    root.setManaged(visible);
  }

  /**
   * Reflects whether this panel is currently showing an error in its own error container, so an owning
   * dialog that embeds several panels can observe and aggregate their error state into its own, dialog-level
   * error container.
   */
  public ReadOnlyBooleanProperty errorProperty() {
    return errorContainerController.errorProperty();
  }

  /**
   * Reflects the severity ("ERROR" or "WARNING") of whatever this panel's error container is currently (or
   * was last) showing, so an owning dialog aggregating {@link #errorProperty()} across several panels can
   * tell warnings apart from errors.
   */
  public ReadOnlyStringProperty severityProperty() {
    return errorContainerController.severityProperty();
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> {
      String settingsKey = getExpandedSettingsKey();
      if (settingsKey != null) {
        boolean animated = root.isAnimated();
        root.setAnimated(false);
        root.setExpanded(LocalUISettings.getBoolean(settingsKey, true));
        root.setAnimated(animated);
        root.expandedProperty().addListener((observable, oldValue, newValue) ->
            LocalUISettings.saveProperty(settingsKey, String.valueOf(newValue)));
      }
    });
    showValidationError(null);
  }

  /**
   * Overrides this panel's title, e.g. when the same controller class is reused for multiple variants of a
   * panel (see {@link #setSettingsKeySuffix}).
   */
  protected void setTitle(@NonNull String title) {
    root.setText(title);
  }

  /**
   * Distinguishes the persisted expanded/collapsed state of multiple instances of the same controller class,
   * which would otherwise collide on the same settings key (derived from the class name).
   */
  protected void setSettingsKeySuffix(@NonNull String suffix) {
    this.settingsKeySuffix = suffix;
  }

  /**
   * Sets a text field's value without triggering the save/validation cycle registered by {@link
   * #bindTextField}. Property editors should use this (instead of {@code textField.setText(...)}) whenever
   * they repopulate a field from the model, e.g. in {@link #setElement}.
   */
  protected void setFieldValue(@NonNull TextField textField, String value) {
    updatingFromModel = true;
    try {
      textField.setText(value);
    } finally {
      updatingFromModel = false;
    }
  }

  /**
   * Sets a checkbox's value without triggering the save/validation cycle registered by {@link
   * #bindCheckBox}. Property editors should use this (instead of {@code checkBox.setSelected(...)})
   * whenever they repopulate a field from the model, e.g. in {@link #setElement}.
   */
  protected void setFieldValue(@NonNull CheckBox checkBox, boolean value) {
    updatingFromModel = true;
    try {
      checkBox.setSelected(value);
    } finally {
      updatingFromModel = false;
    }
  }

  /**
   * Sets a combo box's value without triggering the save/validation cycle registered by {@link
   * #bindComboBox}. Property editors should use this (instead of {@code comboBox.setValue(...)})
   * whenever they repopulate a field from the model, e.g. in {@link #setElement}.
   */
  protected void setFieldValue(@NonNull ComboBox<String> comboBox, String value) {
    updatingFromModel = true;
    try {
      comboBox.setValue(value);
    } finally {
      updatingFromModel = false;
    }
  }

  /**
   * Sets a text area's value without triggering the save/validation cycle registered by {@link
   * #bindTextArea}. Property editors should use this (instead of {@code textArea.setText(...)}) whenever
   * they repopulate a field from the model, e.g. in {@link #setElement}.
   */
  protected void setFieldValue(@NonNull TextArea textArea, String value) {
    updatingFromModel = true;
    try {
      textArea.setText(value);
    } finally {
      updatingFromModel = false;
    }
  }

  /**
   * Reusable pattern for property editor fields: whenever the text field's value changes, applies it to the
   * element via {@code setter}, saves the owning model's json file, and re-validates the element via the
   * data service api, reflecting the result on the field's styling and in the error container. The
   * actual save/validate is debounced so rapid typing doesn't trigger a file write and validation per
   * keystroke.
   */
  protected void bindTextField(@NonNull TextField textField, @NonNull BiConsumer<Element, String> setter) {
    textField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      setter.accept(element, newValue);
      debouncer.debounce(textField.getId(), () -> commitChange(textField), COMMIT_DEBOUNCE_MS, true);
    });
  }

  /**
   * Same as {@link #bindTextField} but for a text area.
   */
  protected void bindTextArea(@NonNull TextArea textArea, @NonNull BiConsumer<Element, String> setter) {
    textArea.textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      setter.accept(element, newValue);
      debouncer.debounce(textArea.getId(), () -> commitChange(textArea), COMMIT_DEBOUNCE_MS, true);
    });
  }

  /**
   * Same as {@link #bindTextField} but for a checkbox's selected state.
   */
  protected void bindCheckBox(@NonNull CheckBox checkBox, @NonNull BiConsumer<Element, Boolean> setter) {
    checkBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      setter.accept(element, newValue);
      debouncer.debounce(checkBox.getId(), () -> commitChange(checkBox), COMMIT_DEBOUNCE_MS, true);
    });
  }

  /**
   * Same as {@link #bindTextField} but for a combo box's value.
   */
  protected void bindComboBox(@NonNull ComboBox<String> comboBox, @NonNull BiConsumer<Element, String> setter) {
    comboBox.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      setter.accept(element, newValue);
      debouncer.debounce(comboBox.getId(), () -> commitChange(comboBox), COMMIT_DEBOUNCE_MS, true);
    });
  }

  private void commitChange(@NonNull Node field) {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    saveMode.commit(projectItem);
    applyValidationResult(field, validateElement(projectItem));
  }

  /**
   * Same as {@link #commitChange(Node)} but for structural changes (e.g. adding/removing a row in a
   * dynamic list) that aren't tied to a single field, so there's no field to mark with the error
   * pseudo-class.
   */
  protected void commitChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    saveMode.commit(projectItem);
    if (element == null) {
      return;
    }
    Optional<ElementValidationError> error = validateElement(projectItem);
    showValidationError(error.orElse(null));
    StudioEventManager.getInstance().fireElementValidatedEvent(element.getId(), error.orElse(null));
    StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
  }

  private Optional<ElementValidationError> validateElement(@NonNull ProjectItem projectItem) {
    if (element == null || !(projectItem.getModel() instanceof DocumentModel documentModel)) {
      return Optional.empty();
    }
    try {
      return VALIDATION_SERVICE.validateElement(documentModel, element.getId(), List.of());
    } catch (Exception e) {
      log.warn("Failed to validate element '{}': {}", element.getId(), e.getMessage(), e);
      return Optional.empty();
    }
  }

  private void applyValidationResult(@NonNull Node field, @NonNull Optional<ElementValidationError> error) {
    field.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error.isPresent());
    showValidationError(error.orElse(null));
    if (element != null) {
      StudioEventManager.getInstance().fireElementValidatedEvent(element.getId(), error.orElse(null));
    }
  }

  private void showValidationError(ElementValidationError error) {
    if (error == null) {
      errorContainerController.hide();
    } else {
      errorContainerController.show(error.severity(), error.message());
    }
  }

  /**
   * For subclasses whose validation isn't expressed as an {@link ElementValidationError} (e.g. a model-header
   * panel that isn't bound to a single {@link Element}, so {@link #commitChange()}'s element-based validation
   * never runs): shows this panel's own error container directly.
   */
  protected void showError(@NonNull String severity, @NonNull String message) {
    errorContainerController.show(severity, message);
  }

  /**
   * Counterpart to {@link #showError}.
   */
  protected void hideError() {
    errorContainerController.hide();
  }

  private String getExpandedSettingsKey() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || projectItem.getModel() == null) {
      return null;
    }

    ModelType modelType = projectItem.getModel().getModelType();
    if (modelType == null) {
      return null;
    }

    return modelType.getValue() + "." + getClass().getSimpleName() + settingsKeySuffix + ".expanded";
  }

  protected ModelConfig getModelConfig(A12Model<?> model) {
    if (!(model instanceof DocumentModel documentModel)) {
      return null;
    }
    DocumentModelContent content = documentModel.getContent();
    return content != null ? content.getModelConfig() : null;
  }
}
