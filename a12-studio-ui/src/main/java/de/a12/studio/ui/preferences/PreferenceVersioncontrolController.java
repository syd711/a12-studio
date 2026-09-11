package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.settings.VersionControlSettings;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferenceVersioncontrolController implements Initializable {

  @FXML
  private CheckBox enabledCheckBox;

  private final VersionControlSettings settings = VersionControlSettings.load();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    enabledCheckBox.setSelected(settings.isEnabled());
    enabledCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      settings.setEnabled(newValue);
      persistAndNotify();
    });
  }

  private void persistAndNotify() {
    settings.save();
    StudioEventManager.getInstance().fireSettingsChangedEvent(settings);
  }
}
