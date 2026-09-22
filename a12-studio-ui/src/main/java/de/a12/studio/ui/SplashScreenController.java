package de.a12.studio.ui;

import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.StudioVersion;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;

import java.net.URL;
import java.util.ResourceBundle;

public class SplashScreenController implements Initializable {

  @FXML
  private Label versionLabel;

  @FXML
  private Label statusLabel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    versionLabel.setText(StudioVersion.get());
    statusLabel.setText(StudioBundle.get("studio_splash.starting"));
  }

  public void setStatus(String status) {
    Platform.runLater(() -> statusLabel.setText(status));
  }
}
