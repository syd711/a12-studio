package de.a12.studio.ui.editors;

import de.a12.studio.commons.fx.Debouncer;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.Element;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.dataservices.services.documentmodel.features.validation.DMValidationService;
import de.a12.studio.dataservices.services.documentmodel.features.validation.ElementValidationError;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
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

  protected Element element;

  // Set while a field's value is being repopulated from the model (e.g. on element selection), so those
  // programmatic updates don't get mistaken for user edits and trigger a save/validation cycle.
  private boolean updatingFromModel;

  public void setElement(@NonNull Element element) {
    this.element = element;
    showValidationError(null);
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> {
      String settingsKey = getExpandedSettingsKey();
      if (settingsKey != null) {
        boolean animated = root.isAnimated();
        root.setAnimated(false);
        root.setExpanded(LocalUISettings.getBoolean(settingsKey));
        root.setAnimated(animated);
        root.expandedProperty().addListener((observable, oldValue, newValue) ->
            LocalUISettings.saveProperty(settingsKey, String.valueOf(newValue)));
      }
    });
    showValidationError(null);
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

  private void commitChange(@NonNull Node field) {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    applyValidationResult(field, validateElement(projectItem));
  }

  private Optional<ElementValidationError> validateElement(@NonNull ProjectItem projectItem) {
    if (!(projectItem.getModel() instanceof DocumentModel documentModel)) {
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
    StudioEventManager.getInstance().fireElementValidatedEvent(element.getId(), error.orElse(null));
  }

  private void showValidationError(ElementValidationError error) {
    if (error == null) {
      errorContainerController.hide();
    } else {
      errorContainerController.show(error.severity(), error.message());
    }
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

    return modelType.getValue() + "." + getClass().getSimpleName() + ".expanded";
  }
}
