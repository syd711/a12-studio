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
import de.a12.studio.modelsvalidation.ElementProperty;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.events.ElementValidatedEvent;
import de.a12.studio.ui.events.StudioEventListener;
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
import javafx.scene.control.Spinner;
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
abstract public class AbstractPropertyEditor implements Initializable, StudioEventListener {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

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

  /**
   * Whether this panel is embedded in a dialog with its own Save button, as opposed to being bound directly
   * to the currently selected project item. Mirrors {@link #saveMode}: a {@link PropertyEditorSaveMode.Deferred}
   * is only ever handed to panels embedded in such a dialog (see the callers of {@link #setSaveMode}), so it
   * doubles as that signal for subclasses that need to adjust their UI accordingly (e.g. hiding a button that
   * only makes sense outside a dialog).
   */
  protected boolean isEmbeddedInDialog() {
    return saveMode instanceof PropertyEditorSaveMode.Deferred;
  }

  /**
   * Releases resources held by this panel once it (and the editor/dialog that embeds it) is torn down.
   * Unregisters this panel from {@link StudioEventManager}, registered in {@link #initialize}. Subclasses
   * overriding this for their own cleanup (e.g. to react to {@code localesChanged}) must call
   * {@code super.destroy()} to still unregister.
   */
  public void destroy() {
    StudioEventManager.getInstance().removeListener(this);
  }

  public void setElement(@NonNull Element element) {
    this.element = element;
    refreshValidationState();
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
    StudioEventManager.getInstance().addListener(this);
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
   * Sets a spinner's editor text without triggering the save/validation cycle registered by {@link
   * #bindSpinner}. Property editors should use this (instead of {@code spinner.getEditor().setText(...)})
   * whenever they repopulate a field from the model, e.g. in {@link #setElement}.
   */
  protected void setFieldValue(@NonNull Spinner<Integer> spinner, String value) {
    updatingFromModel = true;
    try {
      spinner.getEditor().setText(value);
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
   * Replaces a combo box's items without triggering the save/validation cycle registered by {@link
   * #bindComboBox}. Property editors whose item list depends on the current element (instead of being fixed
   * at panel construction) must use this (instead of {@code comboBox.getItems().setAll(...)}) whenever they
   * repopulate it from the model, e.g. in {@link #setElement}: JavaFX resets a ComboBox's value to {@code null}
   * when the backing items list is replaced and the previously selected item isn't found in the new content,
   * and without this guard that spurious reset is indistinguishable from a user clearing the field, wiping out
   * the model's actual value.
   */
  protected void setComboBoxItems(@NonNull ComboBox<String> comboBox, @NonNull List<String> items) {
    updatingFromModel = true;
    try {
      comboBox.getItems().setAll(items);
    } finally {
      updatingFromModel = false;
    }
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

  /**
   * Same as {@link #bindTextField} but for a spinner's editor text, so both typing and the increment/decrement
   * arrows (which JavaFX reflects into the editor's text) go through the same debounced commit path.
   */
  protected void bindSpinner(@NonNull Spinner<Integer> spinner, @NonNull BiConsumer<Element, String> setter) {
    spinner.getEditor().textProperty().addListener((observable, oldValue, newValue) -> {
      if (updatingFromModel) {
        return;
      }
      setter.accept(element, newValue);
      debouncer.debounce(spinner.getId(), () -> commitChange(spinner), COMMIT_DEBOUNCE_MS, true);
    });
  }

  private void commitChange(@NonNull Node field) {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    saveMode.commit(projectItem);
    applyValidationResult(field, validateElement(projectItem));
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
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
    List<ModelValidationError> errors = validateElement(projectItem);
    showValidationError(ownError(errors).orElse(null));
    StudioEventManager.getInstance().fireElementValidatedEvent(element.getId(), errors.isEmpty() ? null : errors.get(0));
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  /**
   * Same as {@link #commitChange()} but for "model header" panels (e.g. {@link
   * de.a12.studio.ui.editors.propertyeditors.RegionPanelController}, {@link
   * de.a12.studio.ui.editors.propertyeditors.LayoutPanelController}) that edit part of a model not tied to a
   * single {@link Element}, so {@code this.element} is never set and the plain {@link #commitChange()} would
   * save the file but then bail out of its {@code element == null} check before firing {@link
   * StudioEventManager#fireModelSavedEvent}. Without that event, {@code ProjectTreeController} never
   * revalidates the project, so a project-item error banner can go stale even after the panel's own error
   * container (managed directly via {@link #showError}/{@link #hideError}) has already updated. Callers still
   * own their own validation/error-container display; this only handles the save and the project-wide
   * revalidation notification.
   */
  protected void commitHeaderChange() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    saveMode.commit(projectItem);
    StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
  }

  /**
   * Re-validates the currently bound element against the model's current state and reflects the result in
   * this panel's error container, without saving. Unlike {@link #commitChange}, this isn't triggered by an
   * edit in one of this panel's own fields, so it's what makes validation problems caused by changes made
   * elsewhere in the model (e.g. a field this element references having been deleted, or an error already
   * present when the project was opened) show up in this panel's own error container as soon as the element
   * is (re)selected — such changes don't otherwise run this panel's own commit/validate cycle. Called
   * automatically by {@link #setElement}, since every element-bound panel belongs to exactly one element and
   * should surface that element's error regardless of which field caused it; subclasses only need to call this
   * again themselves if they must re-check after something that happens later in their own {@code setElement}
   * override.
   */
  protected void refreshValidationState() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (element == null || projectItem == null) {
      return;
    }
    showValidationError(ownError(validateElement(projectItem)).orElse(null));
  }

  /**
   * Keeps this panel's error container in sync with validation changes caused by a sibling panel bound to the
   * same {@link Element} (several panels are routinely bound to the same element at once, see {@link
   * #validationProperty}). Without this, a panel only ever refreshes its own error container from its own
   * field commits ({@link #commitChange}) or a fresh {@link #setElement}, so an error whose owning property
   * belongs to a currently-hidden or not-yet-edited sibling panel would only appear after the whole editor is
   * closed and reopened.
   */
  @Override
  public void elementValidated(@NonNull ElementValidatedEvent event) {
    if (element != null && event.getElementId().equals(element.getId())) {
      refreshValidationState();
    }
  }

  /**
   * The {@link ElementProperty} tag (see that class for the available constants) this panel is the intended
   * home for, or {@code null} (the default) if it doesn't own any. Several sibling panels are routinely bound
   * to the exact same {@link Element} at once (e.g. a document model field's General Information, Type
   * Definition and Data Type Configuration panels each get their own {@link #setElement}), and {@link
   * ModelValidationError#elementId()} alone can't tell them apart — only {@link ModelValidationError#property()}
   * can. {@link #ownError} uses this to pick, out of every error found for the bound element, the one (if any)
   * that is actually this panel's concern, so unrelated errors don't show up in the wrong container. Leaving
   * this at the default means the panel never shows anything automatically, which is correct for panels with
   * no corresponding validator (e.g. the annotations one).
   */
  protected String validationProperty() {
    return null;
  }

  /**
   * Out of every validation problem currently found for the bound element, the one (if any) that matches
   * this panel's {@link #validationProperty()}. See that method for why a plain element-id match isn't
   * enough.
   */
  private Optional<ModelValidationError> ownError(@NonNull List<ModelValidationError> errors) {
    String property = validationProperty();
    if (property == null) {
      return Optional.empty();
    }
    return errors.stream().filter(error -> property.equals(error.property())).findFirst();
  }

  private List<ModelValidationError> validateElement(@NonNull ProjectItem projectItem) {
    A12Model<?> model = projectItem.getModel();
    if (element == null || model == null) {
      return List.of();
    }
    try {
      return Studio.getValidationService().validateElement(model, element.getId());
    } catch (Exception e) {
      log.warn("Failed to validate element '{}': {}", element.getId(), e.getMessage(), e);
      return List.of();
    }
  }

  private void applyValidationResult(@NonNull Node field, @NonNull List<ModelValidationError> errors) {
    Optional<ModelValidationError> ownError = ownError(errors);
    field.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, ownError.isPresent());
    showValidationError(ownError.orElse(null));
    if (element != null) {
      StudioEventManager.getInstance().fireElementValidatedEvent(element.getId(), errors.isEmpty() ? null : errors.get(0));
    }
  }

  private void showValidationError(ModelValidationError error) {
    if (error == null || suppressErrorContainer()) {
      errorContainerController.hide();
    } else {
      showError(error.severity(), error.message());
    }
  }

  /**
   * Lets a subclass keep its own error container permanently hidden, even when a validation error is found
   * for the bound element. Useful for panels (e.g. the annotations one) whose fields aren't themselves the
   * source of the element-level errors surfaced by {@link #commitChange}, so showing them here would be
   * misleading. Field-level error styling (the "error" pseudo-class) is unaffected.
   */
  protected boolean suppressErrorContainer() {
    return false;
  }

  /**
   * For subclasses whose validation isn't expressed as a {@link ModelValidationError} (e.g. a model-header
   * panel that isn't bound to a single {@link Element}, so {@link #commitChange()}'s element-based validation
   * never runs): shows this panel's own error container directly. Also expands the root {@link TitledPane}, so
   * a collapsed panel doesn't hide the fact that it's now showing an error.
   */
  protected void showError(@NonNull String severity, @NonNull String message) {
    root.setExpanded(true);
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
