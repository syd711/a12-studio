package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.settings.AiSettings;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class PreferenceAiController implements Initializable {

  @FXML
  private CheckBox claudeConsoleButtonCheckBox;

  @FXML
  private RadioButton openFromPathRadioButton;

  @FXML
  private RadioButton configurePathRadioButton;

  @FXML
  private TextField claudePathField;

  @FXML
  private Button claudePathBrowseButton;

  private final AiSettings settings = AiSettings.load();

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    claudeConsoleButtonCheckBox.setSelected(settings.isAddClaudeConsoleButton());
    claudeConsoleButtonCheckBox.selectedProperty().addListener((observable, oldValue, newValue) -> {
      settings.setAddClaudeConsoleButton(newValue);
      persistAndNotify();
    });

    boolean configurePath = settings.getClaudePathMode() == AiSettings.ClaudePathMode.CONFIGURE_PATH;
    openFromPathRadioButton.setSelected(!configurePath);
    configurePathRadioButton.setSelected(configurePath);
    claudePathField.setText(settings.getClaudeExecutablePath());
    updatePathFieldState(configurePath);

    configurePathRadioButton.selectedProperty().addListener((observable, oldValue, isConfigurePath) -> {
      settings.setClaudePathMode(isConfigurePath ? AiSettings.ClaudePathMode.CONFIGURE_PATH : AiSettings.ClaudePathMode.OPEN_FROM_PATH);
      persistAndNotify();
      updatePathFieldState(isConfigurePath);
    });

    claudePathField.textProperty().addListener((observable, oldValue, newValue) -> {
      settings.setClaudeExecutablePath(newValue.isEmpty() ? null : newValue);
      persistAndNotify();
    });
  }

  private void updatePathFieldState(boolean enabled) {
    claudePathField.setDisable(!enabled);
    claudePathBrowseButton.setDisable(!enabled);
  }

  private void persistAndNotify() {
    settings.save();
    StudioEventManager.getInstance().fireSettingsChangedEvent(settings);
  }

  @FXML
  private void onBrowseClaudePath() {
    FileChooser chooser = new FileChooser();
    chooser.setTitle(StudioBundle.get("select_claude_executable"));

    String currentPath = claudePathField.getText();
    if (currentPath != null && !currentPath.isEmpty()) {
      File currentFile = new File(currentPath).getParentFile();
      if (currentFile != null && currentFile.exists()) {
        chooser.setInitialDirectory(currentFile);
      }
    }

    File file = chooser.showOpenDialog(claudePathBrowseButton.getScene().getWindow());
    if (file != null) {
      claudePathField.setText(file.getAbsolutePath());
    }
  }
}
