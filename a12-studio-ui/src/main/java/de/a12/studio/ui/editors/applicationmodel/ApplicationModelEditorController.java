package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.dataservices.services.documentmodel.features.validation.DMValidationService;
import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.dialogs.EditorDialogs;
import de.a12.studio.ui.editors.propertyeditors.ActivityPanelController;
import de.a12.studio.ui.editors.propertyeditors.LayoutPanelController;
import de.a12.studio.ui.editors.propertyeditors.ModulesPanelController;
import de.a12.studio.ui.editors.propertyeditors.RegionPanelController;
import de.a12.studio.ui.preview.PreviewLauncher;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ApplicationModelEditorController extends AbstractEditorController implements Initializable {

  private static final String TABLE_SETTINGS_ID = ModelType.APPLICATION.getValue();

  private static final String DEFAULT_SETTINGS_TOOLTIP = "Model Settings";

  private static final DMValidationService VALIDATION_SERVICE = new DMValidationService();

  @FXML
  private Tooltip settingsButtonTooltip;

  @FXML
  private Circle settingsErrorBadge;

  @FXML
  private ActivityPanelController activityController;

  @FXML
  private ModulesPanelController modulesController;

  @FXML
  private LayoutPanelController layoutController;

  @FXML
  private RegionPanelController regionController;

  @FXML
  public void onFileOpen(ActionEvent e) {
    File file = projectItem.getFile();
    SystemUtil.openFile(file);
  }

  @FXML
  public void onFileEdit(ActionEvent e) {
    File file = projectItem.getFile();
    SystemUtil.editFile(file);
  }

  @FXML
  public void onSettings(ActionEvent e) {
    EditorDialogs.openSettings();
    updateSettingsErrorBadge();
  }

  @FXML
  public void onPreview(ActionEvent e) {
    PreviewLauncher.openPreview(projectItem);
  }

  public void loadModel(@NonNull A12Model model) {
    load((ApplicationModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull ApplicationModel documentModel) {
    modulesController.setModel(documentModel);
    activityController.setModel(documentModel);
    layoutController.setModel(documentModel);
    regionController.setModel(documentModel);
  }

  private void updateSettingsErrorBadge() {
    List<String> issues = projectItem.getModel() instanceof DocumentModel documentModel
        ? VALIDATION_SERVICE.getSettingsIssueMessages(documentModel, ProjectDocumentModels.getOtherDocumentModels(projectItem))
        : List.of();

    settingsErrorBadge.setVisible(!issues.isEmpty());
    settingsButtonTooltip.setText(issues.isEmpty() ? DEFAULT_SETTINGS_TOOLTIP : String.join("\n\n", issues));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BaseTableSettings tableSettings = LocalUISettings.getTablePreference(TABLE_SETTINGS_ID);
  }
}
