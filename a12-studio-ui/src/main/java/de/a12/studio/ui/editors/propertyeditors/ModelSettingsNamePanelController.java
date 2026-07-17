package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.commons.fx.Debouncer;
import de.a12.studio.commons.util.localsettings.LocalUISettings;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Edits the model's name ({@link DocumentModel#getId()}) and internal description, and shows the model
 * version read-only. Not an {@link de.a12.studio.ui.editors.AbstractPropertyEditor} since these fields
 * live on the model itself rather than a single Element.
 */
public class ModelSettingsNamePanelController implements Initializable {

  private static final int COMMIT_DEBOUNCE_MS = 150;

  private final Debouncer debouncer = new Debouncer();

  @FXML
  private TitledPane root;

  @FXML
  private TextField nameField;

  @FXML
  private TextField versionField;

  @FXML
  private TextArea descriptionArea;

  private DocumentModel model;

  // Set while setModel() is repopulating the fields from the model, so the listeners below don't mistake
  // that programmatic change for a user edit and write it straight back.
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

    nameField.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        commitChange(() -> model.setId(newValue));
      }
    });
    descriptionArea.textProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        commitChange(() -> model.setDescription(newValue));
      }
    });
  }

  public void setModel(@NonNull DocumentModel model) {
    this.model = model;
    updatingFromModel = true;
    try {
      nameField.setText(model.getId());
      versionField.setText(model.getModelVersion());
      descriptionArea.setText(model.getDescription());
    } finally {
      updatingFromModel = false;
    }
  }

  private void commitChange(Runnable applyToModel) {
    if (model == null) {
      return;
    }
    applyToModel.run();
    debouncer.debounce(getClass().getSimpleName(), this::save, COMMIT_DEBOUNCE_MS, true);
  }

  private void save() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      projectItem.save();
    }
  }

  private String getExpandedSettingsKey() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem == null || projectItem.getModel() == null || projectItem.getModel().getModelType() == null) {
      return null;
    }
    return projectItem.getModel().getModelType().getValue() + "." + getClass().getSimpleName() + ".expanded";
  }
}
