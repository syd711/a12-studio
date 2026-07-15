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
import javafx.application.Platform;
import javafx.css.PseudoClass;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.VBox;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.BiConsumer;

@Slf4j
abstract public class AbstractPropertyEditor implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 50;

  private static final PseudoClass ERROR_PSEUDO_CLASS = PseudoClass.getPseudoClass("error");

  private static final DMValidationService VALIDATION_SERVICE = new DMValidationService();

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TitledPane root;

  @FXML
  private VBox errorContainer;

  @FXML
  private Label errorTitle;

  @FXML
  private Label errorMessage;

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
        root.setExpanded(LocalUISettings.getBoolean(settingsKey));
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
   * Reusable pattern for property editor fields: whenever the text field's value changes, applies it to the
   * element via {@code setter}, saves the owning model's json file, and re-validates the element via the
   * data service api, reflecting the result on the field's styling and in {@link #errorContainer}. The
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

  private void commitChange(@NonNull TextField textField) {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null) {
      return;
    }
    projectItem.save();
    applyValidationResult(textField, validateElement(projectItem));
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

  private void applyValidationResult(@NonNull TextField textField, @NonNull Optional<ElementValidationError> error) {
    textField.pseudoClassStateChanged(ERROR_PSEUDO_CLASS, error.isPresent());
    showValidationError(error.orElse(null));
  }

  private void showValidationError(ElementValidationError error) {
    boolean hasError = error != null;
    errorContainer.setManaged(hasError);
    errorContainer.setVisible(hasError);
    if (hasError) {
      errorTitle.setText(capitalize(error.severity()));
      errorMessage.setText(error.message());
    }
  }

  private static String capitalize(@NonNull String value) {
    return value.isEmpty() ? value : Character.toUpperCase(value.charAt(0)) + value.substring(1).toLowerCase();
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
