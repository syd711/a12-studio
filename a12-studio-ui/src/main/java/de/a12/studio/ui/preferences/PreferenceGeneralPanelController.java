package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.GeneralSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class PreferenceGeneralPanelController implements Initializable {

  @FXML
  private ComboBox<GeneralSettings.RelationshipEngineMode> relationshipEngineModeCombo;

  @FXML
  private CheckBox showMetaDataCheckBox;

  @FXML
  private GeneralLocalesPanelController localesController;

  @FXML
  private Button resetDialogsButton;

  @FXML
  private void onResetDialogs() {
    Stage stage = (Stage) resetDialogsButton.getScene().getWindow();
    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(stage, StudioBundle.get("reset_all_dialog_states"));
    if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
      return;
    }

    LocalUISettings.resetAllDialogStates();
    WidgetFactory.showInformation(stage, StudioBundle.get("all_dialog_states_have_been_reset"), null);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    Project project = Studio.getCurrentProject();
    if (project == null) {
      return;
    }

    ProjectRootSettings rootSettings = project.getSettings().getProjectRootSettings();
    GeneralSettings general = rootSettings.getGeneral();

    // --- Relationship Engine Mode ---
    relationshipEngineModeCombo.getItems().addAll(GeneralSettings.RelationshipEngineMode.values());
    relationshipEngineModeCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(GeneralSettings.RelationshipEngineMode mode) {
        if (mode == null) return "";
        return switch (mode) {
          case legacy -> StudioBundle.get("legacy");
          case standard -> StudioBundle.get("standard");
        };
      }

      @Override
      public GeneralSettings.RelationshipEngineMode fromString(String s) {
        throw new UnsupportedOperationException();
      }
    });
    relationshipEngineModeCombo.setValue(general.getRelationshipEngineMode());
    relationshipEngineModeCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      general.setRelationshipEngineMode(newVal);
      rootSettings.save();
    });

    // --- Show Metadata ---
    showMetaDataCheckBox.setSelected(general.isShowMetaDataInUIModels());
    showMetaDataCheckBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
      general.setShowMetaDataInUIModels(newVal);
      rootSettings.save();
    });

    // --- Locales ---
    localesController.setGeneralSettings(general, rootSettings);


  }
}
