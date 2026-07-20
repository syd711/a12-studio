package de.a12.studio.ui.editors.dialogs;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalesPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.ModelSettingsNamePanelController;
import de.a12.studio.ui.editors.propertyeditors.RoleEditorPanelController;
import de.a12.studio.ui.editors.propertyeditors.SupportedCharactersPanelController;
import de.a12.studio.ui.editors.propertyeditors.TimezonePanelController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class DocumentModelSettingsDialog implements Initializable, DialogController {

  private static final String GENERIC_ERROR_MESSAGE =
      "One or more of the panels below contain an error. Please review them before saving.";

  private static final String GENERIC_WARNING_MESSAGE =
      "One or more of the panels below contain a warning. Please review them before saving.";

  @FXML
  private ModelSettingsNamePanelController modelSettingsNameController;

  @FXML
  private SupportedCharactersPanelController supportedCharactersController;

  @FXML
  private LocalesPanelController localesController;

  @FXML
  private LocalizedTextPanelController labelsController;

  @FXML
  private RoleEditorPanelController rolesController;

  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private ErrorContainerController errorContainerController;

  @FXML
  private TimezonePanelController timezoneController;

  // Shared by every property editor panel above so their comm/its are only persisted once #onSave is
  // triggered, rather than immediately as they would be outside of this dialog.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  // Captured in initialize() before any panel can touch the model, so onCancel can undo whatever they
  // already applied to it in place. Null if there was no model to edit in the first place.
  private DocumentModelSnapshot snapshot;

  private Stage stage;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    labelsController.configureModelLabels();

    modelSettingsNameController.setSaveMode(saveMode);
    supportedCharactersController.setSaveMode(saveMode);
    localesController.setSaveMode(saveMode);
    labelsController.setSaveMode(saveMode);
    rolesController.setSaveMode(saveMode);
    annotationsController.setSaveMode(saveMode);
    timezoneController.setSaveMode(saveMode);

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null && projectItem.getModel() instanceof DocumentModel documentModel) {
      snapshot = new DocumentModelSnapshot(documentModel);

      modelSettingsNameController.setModel(documentModel);
      supportedCharactersController.setModel(documentModel);
      localesController.setModel(documentModel);
      labelsController.setModel(documentModel);
      rolesController.setModel(documentModel);
      annotationsController.setModel(documentModel);
      timezoneController.setModel(documentModel);
    }

    bindErrorContainer();
  }

  /**
   * Shows this dialog's own error container, with a generic title/message, whenever any of the property
   * editor panels it embeds is showing a validation error or warning in its own (panel-level) error
   * container. An error in any panel takes precedence over a warning, so the dialog only shows "Warning"
   * once every visible panel-level message is a warning.
   */
  private void bindErrorContainer() {
    List<AbstractPropertyEditor> panels = List.of(
        modelSettingsNameController,
        supportedCharactersController,
        localesController,
        labelsController,
        rolesController,
        annotationsController,
        timezoneController);

    Runnable updateErrorContainer = () -> {
      boolean anyError = panels.stream()
          .anyMatch(panel -> panel.errorProperty().get() && "ERROR".equalsIgnoreCase(panel.severityProperty().get()));
      boolean anyWarning = panels.stream()
          .anyMatch(panel -> panel.errorProperty().get() && "WARNING".equalsIgnoreCase(panel.severityProperty().get()));
      if (anyError) {
        errorContainerController.show("ERROR", GENERIC_ERROR_MESSAGE);
      } else if (anyWarning) {
        errorContainerController.show("WARNING", GENERIC_WARNING_MESSAGE);
      } else {
        errorContainerController.hide();
      }
    };

    panels.forEach(panel -> {
      panel.errorProperty().addListener((observable, oldValue, newValue) -> updateErrorContainer.run());
      panel.severityProperty().addListener((observable, oldValue, newValue) -> updateErrorContainer.run());
    });
    updateErrorContainer.run();
  }

  @FXML
  private void onSave() {
    saveMode.flush();
    StudioEventManager.getInstance().fireLocalesChangedEvent(Studio.getSelectedProjectItem());
    stage.close();
  }

  @FXML
  private void onCancel() {
    onDialogCancel();
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    if (snapshot != null) {
      snapshot.restore();
    }
    StudioEventManager.getInstance().fireLocalesChangedEvent(Studio.getSelectedProjectItem());
  }
}
