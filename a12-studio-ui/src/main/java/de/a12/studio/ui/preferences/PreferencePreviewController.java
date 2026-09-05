package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.PreviewAppSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.previewapp.PreviewAppDeployer;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class PreferencePreviewController implements Initializable {

  @FXML
  private TextField urlField;

  @FXML
  private Button testConnectionButton;

  @FXML
  private TextField usernameField;

  @FXML
  private PasswordField passwordField;

  @FXML
  private ComboBox<PreviewAppSettings.BrowserType> browserTypeCombo;

  @FXML
  private CheckBox autoRefreshCheckBox;

  @FXML
  private TextField autoRefreshDelayField;

  private ProjectRootSettings rootSettings;
  private PreviewAppSettings settings;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return;
    }

    rootSettings = project.getSettings().getProjectRootSettings();
    settings = rootSettings.getPreviewApp();

    urlField.setText(settings.getUrl());
    urlField.textProperty().addListener((observable, oldValue, newValue) -> {
      settings.setUrl(newValue);
      rootSettings.save();
    });

    usernameField.setText(settings.getUsername());
    usernameField.textProperty().addListener((observable, oldValue, newValue) -> {
      settings.setUsername(newValue);
      rootSettings.save();
    });

    passwordField.setText(settings.getPassword());
    passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
      settings.setPassword(newValue);
      rootSettings.save();
    });

    browserTypeCombo.getItems().addAll(PreviewAppSettings.BrowserType.values());
    browserTypeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(PreviewAppSettings.BrowserType browserType) {
        return switch (browserType) {
          case SYSTEM_DEFAULT -> "System Default";
          case CHROME -> "Chrome";
          case FIREFOX -> "Firefox";
          case EDGE -> "Edge";
          case null -> "";
        };
      }

      @Override
      public PreviewAppSettings.BrowserType fromString(String string) {
        throw new UnsupportedOperationException();
      }
    });
    browserTypeCombo.setValue(settings.getBrowserType());
    browserTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      settings.setBrowserType(newValue);
      rootSettings.save();
    });

    autoRefreshCheckBox.setSelected(settings.isAutoRefreshEnabled());
    autoRefreshCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      settings.setAutoRefreshEnabled(newValue);
      rootSettings.save();
      updateDelayFieldState(newValue);
    });

    autoRefreshDelayField.setText(String.valueOf(settings.getAutoRefreshDelayMillis()));
    autoRefreshDelayField.textProperty().addListener((observable, oldValue, newValue) -> {
      try {
        settings.setAutoRefreshDelayMillis(Integer.parseInt(newValue.trim()));
        rootSettings.save();
      }
      catch (NumberFormatException e) {
        // Ignore incomplete/invalid input while the user is typing; the last valid value is kept.
      }
    });

    updateDelayFieldState(settings.isAutoRefreshEnabled());
  }

  @FXML
  private void onTestConnection() {
    String url = urlField.getText();
    String username = usernameField.getText();
    String password = passwordField.getText();

    testConnectionButton.setDisable(true);
    Thread testThread = new Thread(() -> {
      try {
        PreviewAppDeployer.testConnection(url, username, password);
        Platform.runLater(() -> {
          testConnectionButton.setDisable(false);
          WidgetFactory.showAlert(Studio.stage, StudioBundle.get("preview_test_connection_succeeded"));
        });
      }
      catch (Exception e) {
        Platform.runLater(() -> {
          testConnectionButton.setDisable(false);
          WidgetFactory.showAlert(Studio.stage, StudioBundle.get("preview_test_connection_failed"), e.getMessage());
        });
      }
    }, "Preview App Connection Test");
    testThread.setDaemon(true);
    testThread.start();
  }

  private void updateDelayFieldState(boolean enabled) {
    autoRefreshDelayField.setDisable(!enabled);
  }
}
