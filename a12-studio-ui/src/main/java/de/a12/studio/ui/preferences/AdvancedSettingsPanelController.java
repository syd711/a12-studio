package de.a12.studio.ui.preferences;

import de.a12.studio.models.features.A12StudioFeatureException;
import de.a12.studio.models.features.ApplicationGroupFeature;
import de.a12.studio.models.features.ApplicationGroupResult;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.AdvancedSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class AdvancedSettingsPanelController implements Initializable {

  @FXML
  private CheckBox useApplicationGroupsCheckBox;

  @FXML
  private TextField applicationGroupNameField;

  @FXML
  private Button applyBtn;

  @FXML
  private void onApplicationGroupApply() {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return;
    }

    Stage stage = (Stage) applyBtn.getScene().getWindow();
    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(stage,
        "This will rename all model files in this project and add or update the \"applicationGroup\" header annotation. Continue?");
    if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
      return;
    }

    try {
      ApplicationGroupResult result = new ApplicationGroupFeature().apply(project);
      project.reload();
      StudioEventManager.getInstance().fireProjectOpenEvent(project);
      WidgetFactory.showInformation(stage,
          "Applied application group \"" + result.groupName() + "\": renamed " + result.renamedCount()
              + " model(s) and updated " + result.referencesUpdatedCount() + " reference(s).", null);
    }
    catch (A12StudioFeatureException e) {
      WidgetFactory.showAlert(stage, e.getMessage());
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return;
    }

    AdvancedSettings settings = project.getSettings().getAdvancedSettings();

    // --- Use Application Groups ---
    useApplicationGroupsCheckBox.setSelected(settings.isUseApplicationGroups());
    applicationGroupNameField.setDisable(!settings.isUseApplicationGroups());
    applyBtn.setDisable(!settings.isUseApplicationGroups());

    useApplicationGroupsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
      settings.setUseApplicationGroups(newVal);
      applyBtn.setDisable(!newVal);
      applicationGroupNameField.setDisable(!newVal);
      settings.save();
      Studio.getValidationService().setApplicationGroupValidatorEnabled(newVal);
    });

    // --- Application Group Name ---
    applicationGroupNameField.setText(settings.getApplicationGroupName());
    applyBtn.setDisable(!ApplicationGroupFeature.isValidGroupName(settings.getApplicationGroupName()) || !settings.isUseApplicationGroups());
    applicationGroupNameField.textProperty().addListener((obs, oldVal, newVal) -> {
      settings.setApplicationGroupName(newVal);
      settings.save();
      applyBtn.setDisable(!ApplicationGroupFeature.isValidGroupName(newVal));
    });
  }
}
