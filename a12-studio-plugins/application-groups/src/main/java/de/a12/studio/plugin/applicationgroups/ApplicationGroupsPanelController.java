package de.a12.studio.plugin.applicationgroups;

import de.a12.studio.models.features.A12StudioFeatureException;
import de.a12.studio.models.projects.Project;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

public class ApplicationGroupsPanelController {

  @FXML
  private CheckBox useApplicationGroupsCheckBox;

  @FXML
  private TextField applicationGroupNameField;

  @FXML
  private Button applyBtn;

  private Project project;

  public void setProject(Project project) {
    this.project = project;

    ApplicationGroupsSettings settings = ApplicationGroupsSettings.load(project.getFolder());

    useApplicationGroupsCheckBox.setSelected(settings.isUseApplicationGroups());
    applicationGroupNameField.setDisable(!settings.isUseApplicationGroups());
    applyBtn.setDisable(!settings.isUseApplicationGroups());

    useApplicationGroupsCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
      settings.setUseApplicationGroups(newVal);
      applyBtn.setDisable(!newVal);
      applicationGroupNameField.setDisable(!newVal);
      settings.save();
    });

    applicationGroupNameField.setText(settings.getApplicationGroupName());
    applyBtn.setDisable(!ApplicationGroupFeature.isValidGroupName(settings.getApplicationGroupName()) || !settings.isUseApplicationGroups());
    applicationGroupNameField.textProperty().addListener((obs, oldVal, newVal) -> {
      settings.setApplicationGroupName(newVal);
      settings.save();
      applyBtn.setDisable(!ApplicationGroupFeature.isValidGroupName(newVal));
    });
  }

  @FXML
  private void onApplicationGroupApply() {
    Stage stage = (Stage) applyBtn.getScene().getWindow();
    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(stage,
        StudioBundle.get("application_groups.confirm_apply"));
    if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
      return;
    }

    try {
      ApplicationGroupResult result = new ApplicationGroupFeature().apply(project);
      project.reload();
      WidgetFactory.showInformation(stage,
          StudioBundle.get("application_groups.applied_info", result.groupName(), result.renamedCount(),
              result.referencesUpdatedCount()), null);
    }
    catch (A12StudioFeatureException e) {
      WidgetFactory.showAlert(stage, e.getMessage());
    }
  }
}
