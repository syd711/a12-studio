package de.a12.studio.ui.system;

import de.a12.studio.commons.Updater;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

/**
 *
 */
@Slf4j
public class UpdateController {

  private final Stage owner;

  public UpdateController(Stage owner) {
    this.owner = owner;
  }

  public void checkForUpdateAsync() {
    Thread t = new Thread(this::checkForUpdate);
    t.setName("Update Check");
    t.setDaemon(true);
    t.start();
  }

  private void checkForUpdate() {
    String currentVersion = currentVersion();
    String latestVersion = Updater.checkForUpdate();
    if (!Updater.isLargerVersionThan(latestVersion, currentVersion)) {
      log.info("No update available (current: {}, latest: {})", currentVersion, latestVersion);
      return;
    }

    Platform.runLater(() -> promptInstall(currentVersion, latestVersion));
  }

  private void promptInstall(String currentVersion, String latestVersion) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.initOwner(owner);
    alert.setTitle("Update Available");
    alert.setHeaderText("A12 Studio " + latestVersion + " is available (you have " + currentVersion + ")");
    alert.setContentText("Download and install the update now? The app will restart.");

    Optional<ButtonType> result = alert.showAndWait();
    if (result.isPresent() && result.get() == ButtonType.OK) {
      installUpdate(currentVersion, latestVersion);
    }
  }

  private void installUpdate(String currentVersion, String latestVersion) {
    Thread t = new Thread(() -> {
      try {
        Updater.downloadUpdate(latestVersion, Updater.UI_ZIP);
        Updater.installClientUpdate(currentVersion, latestVersion);
      }
      catch (Exception e) {
        log.error("Failed to install client update: {}", e.getMessage(), e);
      }
    });
    t.setName("Update Installer");
    t.start();
  }

  private String currentVersion() {
    String version = getClass().getPackage().getImplementationVersion();
    return version != null ? version : "0.0.0";
  }
}
