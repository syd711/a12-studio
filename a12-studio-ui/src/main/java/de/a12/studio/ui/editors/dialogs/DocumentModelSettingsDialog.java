package de.a12.studio.ui.editors.dialogs;

import de.a12.studio.commons.fx.DialogController;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalesPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.ModelSettingsNamePanelController;
import de.a12.studio.ui.editors.propertyeditors.RoleEditorPanelController;
import de.a12.studio.ui.editors.propertyeditors.SupportedCharactersPanelController;
import de.a12.studio.ui.editors.propertyeditors.TimezonePanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;

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

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    labelsController.configureModelLabels();

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null && projectItem.getModel() instanceof DocumentModel documentModel) {
      modelSettingsNameController.setModel(documentModel);
      supportedCharactersController.setModel(documentModel);
      localesController.setModel(documentModel);
      labelsController.setModel(documentModel);
      rolesController.setModel(documentModel);
      annotationsController.setModel(documentModel);
      timezoneController.setModel(documentModel);
    }
  }

  @Override
  public void onDialogCancel() {

  }
}
