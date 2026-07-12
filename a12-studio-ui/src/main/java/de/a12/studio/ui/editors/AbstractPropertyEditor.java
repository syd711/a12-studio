package de.a12.studio.ui.editors;

import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.models.ModelType;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TitledPane;

import java.net.URL;
import java.util.ResourceBundle;

public class AbstractPropertyEditor implements Initializable {

  @FXML
  private TitledPane root;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    Platform.runLater(() -> {
      String settingsKey = getExpandedSettingsKey();
      if (settingsKey != null) {
        root.setExpanded(LocalUISettings.getBoolean(settingsKey));
        root.expandedProperty().addListener((observable, oldValue, newValue) ->
            LocalUISettings.saveProperty(settingsKey, String.valueOf(newValue)));
      }
    });
  }

  private String getExpandedSettingsKey() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || projectItem.getModel() == null) {
      return null;
    }

    ModelType modelType = projectItem.getModel().getModelType();
    if (modelType == null) {
      return null;
    }

    return modelType.getValue() + "." + getClass().getSimpleName() + ".expanded";
  }
}
