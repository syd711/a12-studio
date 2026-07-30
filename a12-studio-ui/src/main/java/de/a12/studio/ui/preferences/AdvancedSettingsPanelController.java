package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.AdvancedSettings;
import de.a12.studio.ui.Studio;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;

import java.net.URL;
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

    useApplicationGroupsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
      settings.setUseApplicationGroups(newVal);
      applicationGroupNameField.setDisable(!newVal);
      settings.save();
    });

    // --- Application Group Name ---
    applicationGroupNameField.setText(settings.getApplicationGroupName());
    applicationGroupNameField.textProperty().addListener((obs, oldVal, newVal) -> {
      settings.setApplicationGroupName(newVal);
      settings.save();
    });
  }
}
