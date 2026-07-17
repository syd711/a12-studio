package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.models.documentmodel.DocumentModelContent;
import de.a12.studio.dataservices.models.documentmodel.ModelConfig;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TitledPane;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link ModelConfig#getTimeZone()}. Not an {@link de.a12.studio.ui.editors.AbstractPropertyEditor}
 * since the time zone lives on the model's {@link ModelConfig} rather than a single Element.
 */
public class TimezonePanelController implements Initializable {

  private static final List<String> TIMEZONES = List.of("UTC", "Europe/Berlin");

  @FXML
  private TitledPane root;

  @FXML
  private ComboBox<String> timezoneCombo;

  private DocumentModel model;

  // Set while setModel() is repopulating the combo box from the model, so the valueProperty listener
  // below does not mistake that programmatic change for a user edit and write it straight back.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> {
      String settingsKey = getExpandedSettingsKey();
      if (settingsKey != null) {
        boolean animated = root.isAnimated();
        root.setAnimated(false);
        root.setExpanded(LocalUISettings.getBoolean(settingsKey));
        root.setAnimated(animated);
        root.expandedProperty().addListener((observable, oldValue, newValue) ->
            LocalUISettings.saveProperty(settingsKey, String.valueOf(newValue)));
      }
    });

    timezoneCombo.getItems().addAll(TIMEZONES);
    timezoneCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        commitChange(newValue);
      }
    });
  }

  public void setModel(@NonNull DocumentModel model) {
    this.model = model;
    ModelConfig modelConfig = getModelConfig(model);
    updatingFromModel = true;
    try {
      timezoneCombo.setValue(modelConfig != null ? modelConfig.getTimeZone() : null);
    } finally {
      updatingFromModel = false;
    }
  }

  private void commitChange(String timeZone) {
    if (model == null) {
      return;
    }

    ModelConfig modelConfig = getModelConfig(model);
    if (modelConfig == null) {
      return;
    }
    modelConfig.setTimeZone(timeZone);

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      projectItem.save();
    }
  }

  private static ModelConfig getModelConfig(DocumentModel model) {
    DocumentModelContent content = model.getContent();
    return content != null ? content.getModelConfig() : null;
  }

  private String getExpandedSettingsKey() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || projectItem.getModel() == null || projectItem.getModel().getModelType() == null) {
      return null;
    }
    return projectItem.getModel().getModelType().getValue() + "." + getClass().getSimpleName() + ".expanded";
  }
}
