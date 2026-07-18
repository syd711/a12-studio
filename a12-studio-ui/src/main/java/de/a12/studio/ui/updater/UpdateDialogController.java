package de.a12.studio.ui.updater;

import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.StudioVersion;
import javafx.application.Platform;
import javafx.concurrent.Service;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

/**
 * Drives the actual client update: downloads the new version and installs it in-process via
 * {@link UpdaterService} (a12-studio-ui is the only executable this app self-updates).
 */
@Slf4j
public class UpdateDialogController implements DialogController {

  @FXML
  private Label updateLabel;

  @FXML
  private ProgressBar updateProgress;

  @FXML
  private Button closeBtn;

  private final UpdaterService updaterService = new UpdaterService();

  private Service<Void> updateService;

  private Stage stage;

  public void setData(Stage stage, String newVersion) {
    this.stage = stage;

    String currentVersion = StudioVersion.get();
    updateLabel.setText("Downloading version " + newVersion + "...");
    startUpdate(currentVersion, newVersion);
  }

  private void startUpdate(String oldVersion, String newVersion) {
    updateService = new Service<>() {
      @Override
      protected Task<Void> createTask() {
        return new Task<>() {
          @Override
          protected Void call() throws Exception {
            new Thread(() -> updaterService.downloadUpdate(newVersion)).start();
            Thread.sleep(1000);

            while (true) {
              int progress = updaterService.getDownloadProgress();
              Platform.runLater(() -> updateProgress.setProgress(progress / 100.0));
              if (progress >= 100) {
                break;
              }
              Thread.sleep(1000);
            }

            Platform.runLater(() -> {
              updateLabel.setText("Installing update...");
              updateProgress.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
            });

            Thread.sleep(2000);
            updaterService.installUpdate(oldVersion, newVersion);
            return null;
          }
        };
      }
    };
    updateService.start();
  }

  @FXML
  private void onClose(ActionEvent ae) {
    try {
      if (updateService != null && updateService.isRunning()) {
        updateService.cancel();
      }
    }
    catch (Exception e) {
      log.warn("Failed to cancel update service: {}", e.getMessage());
    }
    if (ae != null) {
      stage.close();
    }
  }

  @Override
  public void onDialogCancel() {
    onClose(null);
  }
}
