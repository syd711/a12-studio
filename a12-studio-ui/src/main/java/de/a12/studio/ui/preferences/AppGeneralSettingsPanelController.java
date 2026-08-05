package de.a12.studio.ui.preferences;

import de.a12.studio.ui.util.localsettings.LocalUISettings;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class AppGeneralSettingsPanelController implements Initializable {

  /** BCP-47 tag → display name. Ordered: system default first, then explicit locales. */
  private static final Map<String, String> SUPPORTED_LANGUAGES = new LinkedHashMap<>();
  static {
    SUPPORTED_LANGUAGES.put("", "System default");
    SUPPORTED_LANGUAGES.put("en", "English");
    SUPPORTED_LANGUAGES.put("de", "Deutsch");
  }

  @FXML
  private ComboBox<String> languageCombo;

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    // --- Language ---
    languageCombo.getItems().addAll(SUPPORTED_LANGUAGES.keySet());
    languageCombo.setConverter(new StringConverter<>() {
      @Override
      public String toString(String tag) {
        return tag == null ? "" : SUPPORTED_LANGUAGES.getOrDefault(tag, tag);
      }

      @Override
      public String fromString(String s) {
        throw new UnsupportedOperationException();
      }
    });
    String storedLang = LocalUISettings.getString(LocalUISettings.LANGUAGE);
    languageCombo.setValue(storedLang != null ? storedLang : "");
    languageCombo.valueProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null) {
        LocalUISettings.saveProperty(LocalUISettings.LANGUAGE, newVal);
      }
    });
  }
}
