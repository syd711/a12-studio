package de.a12.studio.ui.editors.dialogs;

import de.a12.studio.commons.fx.DialogController;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalesPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.ModelSettingsNamePanelController;
import de.a12.studio.ui.editors.propertyeditors.RoleEditorPanelController;
import de.a12.studio.ui.editors.propertyeditors.SupportedCharactersPanelController;
import de.a12.studio.ui.editors.propertyeditors.TimezonePanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class DocumentModelSettingsDialog implements Initializable, DialogController {

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
  private TimezonePanelController timezoneController;

  // Shared by every property editor panel above so their commits are only persisted once #onSave is
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
  }

  @FXML
  private void onSave() {
    saveMode.flush();
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
  }
}
