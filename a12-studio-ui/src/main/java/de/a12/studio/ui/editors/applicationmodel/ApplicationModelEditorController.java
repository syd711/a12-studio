package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.editors.dialogs.EditorDialogs;
import de.a12.studio.ui.editors.propertyeditors.*;
import de.a12.studio.ui.preview.PreviewLauncher;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Circle;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ApplicationModelEditorController extends AbstractEditorController implements Initializable {

  private static final String DEFAULT_SETTINGS_TOOLTIP = "Model Settings";

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
  private SubregionsPanelController subregionsController;


  @FXML
  public void onSettings(ActionEvent e) {
    EditorDialogs.openSettings();
    updateSettingsErrorBadge();
  }

  @FXML
  public void onPreview(ActionEvent e) {
    PreviewLauncher.openPreview(projectItem);
  }

  public void loadModel(@NonNull A12Model<?> model) {
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
    List<String> issues = projectItem.getModel() != null
        ? Studio.getValidationService().getSettingsIssueMessages(projectItem.getModel())
        : List.of();

    settingsErrorBadge.setVisible(!issues.isEmpty());
    settingsButtonTooltip.setText(issues.isEmpty() ? DEFAULT_SETTINGS_TOOLTIP : String.join("\n\n", issues));
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BaseTableSettings tableSettings = getBaseTableSettings();
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.APPLICATION;
  }
}
