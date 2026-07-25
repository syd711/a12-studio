package de.a12.studio.ui.editors.propertyeditors;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.DocumentModelContent;
import de.a12.studio.models.documentmodel.ModelConfig;
import de.a12.studio.models.documentmodel.Element;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.DMValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.util.ProjectDocumentModels;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@link ModelConfig#getTimeZone()}. Not bound to a single {@link Element}
 * (the time zone lives on the model's {@link ModelConfig}), so {@link #setElement} is never called and only
 * {@link #setModel} is used; validation is therefore driven manually via {@link #updateValidation} rather than
 * the element-based validation in {@link AbstractPropertyEditor#commitChange(javafx.scene.Node)}.
 */
public class TimezonePanelController extends AbstractPropertyEditor implements Initializable {

  private static final List<String> TIMEZONES = List.of("UTC", "Europe/Berlin");

  private static final DMValidationService VALIDATION_SERVICE = new DMValidationService();

  @FXML
  private ComboBox<String> timezoneCombo;

  private DocumentModel model;

  // Set while setModel() is repopulating the combo box from the model, so the valueProperty listener
  // below does not mistake that programmatic change for a user edit and write it straight back.
  private boolean updatingFromModel;

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    super.initialize(location, resources);

    timezoneCombo.getItems().addAll(TIMEZONES);
    timezoneCombo.valueProperty().addListener((observable, oldValue, newValue) -> {
      if (!updatingFromModel) {
        commitTimeZoneChange(newValue);
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
    updateValidation();
  }

  private void commitTimeZoneChange(String timeZone) {
    if (model == null) {
      return;
    }

    ModelConfig modelConfig = getModelConfig(model);
    if (modelConfig == null) {
      return;
    }
    modelConfig.setTimeZone(timeZone);

    commitChange();
    updateValidation();
  }

  /**
   * A model's time zone must agree with every other document model in its project, see {@link
   * DMValidationService#getTimeZoneMismatchError}.
   */
  private void updateValidation() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (model == null || projectItem == null) {
      hideError();
      return;
    }
    VALIDATION_SERVICE.getTimeZoneMismatchError(model, ProjectDocumentModels.getOtherDocumentModels(projectItem))
        .ifPresentOrElse(message -> showError("ERROR", message), this::hideError);
  }

  private static ModelConfig getModelConfig(DocumentModel model) {
    DocumentModelContent content = model.getContent();
    return content != null ? content.getModelConfig() : null;
  }
}
