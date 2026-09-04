package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.GeneralSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferenceValidationSettingsPanelController implements Initializable {

  @FXML
  private CheckBox enforceModelSuffixesCheckBox;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    enforceModelSuffixesCheckBox.setTooltip(WidgetFactory.createTooltip(
        StudioBundle.get("validation_settings.enforce_model_suffixes_tooltip")));

    Project project = Studio.getCurrentProject();
    if (project == null) {
      return;
    }

    ProjectRootSettings rootSettings = project.getSettings().getProjectRootSettings();
    GeneralSettings general = rootSettings.getGeneral();

    enforceModelSuffixesCheckBox.setSelected(general.isEnforceModelSuffixes());
    enforceModelSuffixesCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
      general.setEnforceModelSuffixes(newVal);
      rootSettings.save();
    });
  }
}
