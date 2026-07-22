package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.settings.PreviewSettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.ResourceBundle;

public class PreviewSettingsController implements Initializable {

  @FXML
  private ComboBox<PreviewSettings.BrowserType> browserTypeCombo;

  @FXML
  private CheckBox autoRefreshCheckBox;

  @FXML
  private TextField autoRefreshDelayField;

  private final PreviewSettings settings = PreviewSettings.load();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    browserTypeCombo.getItems().addAll(PreviewSettings.BrowserType.values());
    browserTypeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(PreviewSettings.BrowserType browserType) {
        return switch (browserType) {
          case SYSTEM_DEFAULT -> "System Default";
          case CHROME -> "Chrome";
          case FIREFOX -> "Firefox";
          case EDGE -> "Edge";
          case null -> "";
        };
      }

      @Override
      public PreviewSettings.BrowserType fromString(String string) {
        throw new UnsupportedOperationException();
      }
    });
    browserTypeCombo.setValue(settings.getBrowserType());
    browserTypeCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      settings.setBrowserType(newValue);
      settings.save();
    });

    autoRefreshCheckBox.setSelected(settings.isAutoRefreshEnabled());
    autoRefreshCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      settings.setAutoRefreshEnabled(newValue);
      settings.save();
      updateDelayFieldState(newValue);
    });

    autoRefreshDelayField.setText(String.valueOf(settings.getAutoRefreshDelayMillis()));
    autoRefreshDelayField.textProperty().addListener((observable, oldValue, newValue) -> {
      try {
        settings.setAutoRefreshDelayMillis(Integer.parseInt(newValue.trim()));
        settings.save();
      }
      catch (NumberFormatException e) {
        // Ignore incomplete/invalid input while the user is typing; the last valid value is kept.
      }
    });

    updateDelayFieldState(settings.isAutoRefreshEnabled());
  }

  private void updateDelayFieldState(boolean enabled) {
    autoRefreshDelayField.setDisable(!enabled);
  }
}
