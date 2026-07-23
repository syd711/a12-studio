package de.a12.studio.ui.preferences;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.Locale;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.GeneralSettings;
import de.a12.studio.models.projects.settings.ProjectRootSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalesPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class GeneralSettingsPanelController implements Initializable {

  @FXML
  private ComboBox<GeneralSettings.RelationshipEngineMode> relationshipEngineModeCombo;

  @FXML
  private CheckBox showMetaDataCheckBox;

  @FXML
  private LocalesPanelController localesController;

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
          case legacy -> "Legacy";
          case standard -> "Standard";
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

    // --- Locales (via existing LocalesPanelController) ---
    // Wrap GeneralSettings in a minimal A12Model adapter so LocalesPanelController
    // can operate on the same Locale list without any duplication of that widget's logic.
    // Use a no-op save mode: the adapter already calls rootSettings.save() on every mutation,
    // so we must not also trigger a project-item save (which would save the wrong file).
    localesController.setSaveMode(projectItem -> { /* handled by adapter */ });
    localesController.setModel(new GeneralSettingsModelAdapter(general, rootSettings));
  }

  // ---------------------------------------------------------------------------
  // Inner adapter — bridges GeneralSettings.locales into the A12Model contract
  // ---------------------------------------------------------------------------

  private static class GeneralSettingsModelAdapter extends A12Model<Void> {

    private final GeneralSettings general;
    private final ProjectRootSettings rootSettings;

    GeneralSettingsModelAdapter(GeneralSettings general, ProjectRootSettings rootSettings) {
      this.general = general;
      this.rootSettings = rootSettings;
      // Seed the A12Model locale list from GeneralSettings so LocalesPanelController
      // reads the right initial values.
      super.setLocales(general.getLocales());
    }

    /** Propagate mutations back to GeneralSettings and persist immediately. */
    @Override
    public void setLocales(List<Locale> locales) {
      super.setLocales(locales);
      general.setLocales(locales);
      rootSettings.save();
    }

    @Override
    public List<Locale> getLocales() {
      return general.getLocales();
    }

    @Override
    public ModelType getModelType() {
      return null; // not a real model — validation in LocalesPanelController guards against null type
    }
  }
}
