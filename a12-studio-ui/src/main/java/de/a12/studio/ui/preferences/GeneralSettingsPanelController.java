package de.a12.studio.ui.preferences;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.GeneralSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class GeneralSettingsPanelController implements Initializable {

  /** BCP-47 tag → display name. Ordered: system default first, then explicit locales. */
  private static final Map<String, String> SUPPORTED_LANGUAGES = new LinkedHashMap<>();
  static {
    SUPPORTED_LANGUAGES.put("", "System default");
    SUPPORTED_LANGUAGES.put("en", "English");
    SUPPORTED_LANGUAGES.put("de", "Deutsch");
  }

  @FXML
  private ComboBox<GeneralSettings.RelationshipEngineMode> relationshipEngineModeCombo;

  @FXML
  private ComboBox<String> languageCombo;

  @FXML
  private CheckBox showMetaDataCheckBox;

  @FXML
  private GeneralLocalesPanelController localesController;

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

    // --- Language ---
    languageCombo.getItems().addAll(SUPPORTED_LANGUAGES.keySet());
    languageCombo.setConverter(new StringConverter<>() {
      @Override public String toString(String tag) {
        return tag == null ? "" : SUPPORTED_LANGUAGES.getOrDefault(tag, tag);
      }
      @Override public String fromString(String s) { throw new UnsupportedOperationException(); }
    });
    String storedLang = LocalUISettings.getString("language");
    languageCombo.setValue(storedLang != null ? storedLang : "");
    languageCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        LocalUISettings.saveProperty("language", newVal);
      }
    });
  }
}
