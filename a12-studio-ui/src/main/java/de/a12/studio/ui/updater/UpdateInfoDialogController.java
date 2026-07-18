package de.a12.studio.ui.updater;

import de.a12.studio.ui.components.DialogController;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Shows the release notes for a newly available version, with a button to proceed to the
 * actual updater dialog ({@link UpdateDialogController}).
 */
@Slf4j
public class UpdateInfoDialogController implements DialogController {

  @FXML
  private TextArea textArea;

  @FXML
  private Button updateBtn;

  @FXML
  private Button closeBtn;

  private Stage stage;
  private String version;

  public void setData(Stage stage, String version) {
    this.stage = stage;
    this.version = version;
    textArea.setText(downloadReleaseNotes(version));
  }

  @FXML
  private void onClose() {
    stage.close();
  }

  @FXML
  private void onUpdate() {
    stage.close();
    Platform.runLater(() -> Dialogs.openUpdateDialog(version));
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  private static String downloadReleaseNotes(String version) {
    String url = "https://raw.githubusercontent.com/syd711/a12-studio/" + version + "/RELEASE_NOTES.md";
    try {
      HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
      HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(5)).GET().build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() == 200) {
        return response.body();
      }
      log.info("No release notes found at {} (status {})", url, response.statusCode());
    }
    catch (Exception e) {
      log.warn("Failed to download release notes: {}", e.getMessage());
    }
    return "No release notes available.";
  }
}
