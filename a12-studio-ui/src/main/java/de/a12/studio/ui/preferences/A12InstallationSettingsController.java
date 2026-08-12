package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.settings.A12Settings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.components.StudioFolderChooser;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class A12InstallationSettingsController implements Initializable {

  @FXML
  private TextField installationPathField;

  @FXML
  private Button browseButton;

  @FXML
  private ErrorContainerController errorContainerController;

  private final A12Settings settings = A12Settings.load();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    installationPathField.setText(settings.getInstallationPath());
    installationPathField.textProperty().addListener((observable, oldValue, newValue) -> {
      settings.setInstallationPath(newValue.isEmpty() ? null : newValue);
      persist();
    });

    updateValidation();
  }

  @FXML
  private void onBrowseInstallationPath() {
    StudioFolderChooser chooser = new StudioFolderChooser();
    chooser.setTitle(StudioBundle.get("select_a12_installation_folder"));

    String currentPath = installationPathField.getText();
    if (currentPath != null && !currentPath.isEmpty()) {
      File currentFolder = new File(currentPath);
      if (currentFolder.exists()) {
        chooser.setInitialDirectory(currentFolder);
      }
    }

    File folder = chooser.showOpenDialog(Studio.stage);
    if (folder != null) {
      installationPathField.setText(folder.getAbsolutePath());
    }
  }

  private void persist() {
    settings.save();
    StudioEventManager.getInstance().fireSettingsChangedEvent(settings);
    updateValidation();
  }

  private void updateValidation() {
    String path = installationPathField.getText();
    if (path == null || path.isEmpty()) {
      errorContainerController.hide();
      return;
    }

    if (A12Settings.isValidInstallationFolder(new File(path))) {
      errorContainerController.hide();
    } else {
      errorContainerController.show("ERROR",
          "This folder is not a valid A12 installation. It must contain a \"bin\" folder, a \"licenses\" folder, "
              + "and a link starting with \"Simple Model Editor\".");
    }
  }
}
