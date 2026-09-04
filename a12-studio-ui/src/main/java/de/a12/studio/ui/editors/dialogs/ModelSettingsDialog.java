package de.a12.studio.ui.editors.dialogs;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.overviewmodel.OverviewConfiguration;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ErrorContainerController;
import de.a12.studio.ui.editors.AbstractPropertyEditor;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.formmodel.modelsettings.GeneralDetachedRepeatSettingsPanelController;
import de.a12.studio.ui.editors.formmodel.modelsettings.GeneralInlineRepeatSettingsPanelController;
import de.a12.studio.ui.editors.formmodel.modelsettings.GeneralSettingsPanelController;
import de.a12.studio.ui.editors.formmodel.modelsettings.RuleConfirmationSettingsPanelController;
import de.a12.studio.ui.editors.formmodel.modelsettings.SubtitlePanelController;
import de.a12.studio.ui.editors.propertyeditors.AnnotationsPanelController;
import de.a12.studio.ui.editors.propertyeditors.DocumentUniquenessCriteriaPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalesPanelController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.ModelReferencesPanelController;
import de.a12.studio.ui.editors.propertyeditors.ModelSettingsNamePanelController;
import de.a12.studio.ui.editors.propertyeditors.RolesEditorPanelController;
import de.a12.studio.ui.editors.propertyeditors.SupportedCharactersPanelController;
import de.a12.studio.ui.editors.propertyeditors.TimezonePanelController;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ProjectDocumentModels;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ModelSettingsDialog implements Initializable, DialogController {

  @FXML
  private ModelSettingsNamePanelController modelSettingsNameController;

  @FXML
  private GeneralSettingsPanelController generalSettingsController;

  @FXML
  private GeneralDetachedRepeatSettingsPanelController generalDetachedRepeatSettingsController;

  @FXML
  private GeneralInlineRepeatSettingsPanelController generalInlineRepeatSettingsController;

  @FXML
  private RuleConfirmationSettingsPanelController ruleConfirmationSettingsController;

  @FXML
  private SubtitlePanelController subtitleController;

  @FXML
  private SupportedCharactersPanelController supportedCharactersController;

  @FXML
  private LocalesPanelController localesController;

  @FXML
  private LocalizedTextPanelController labelsController;

  @FXML
  private LocalizedTextPanelController subtitlesController;

  @FXML
  private RolesEditorPanelController rolesController;

  @FXML
  private AnnotationsPanelController annotationsController;

  @FXML
  private DocumentUniquenessCriteriaPanelController documentUniquenessCriteriaController;

  @FXML
  private ModelReferencesPanelController modelReferencesController;

  @FXML
  private ErrorContainerController errorContainerController;

  @FXML
  private TimezonePanelController timezoneController;

  // Shared by every property editor panel above so their comm/its are only persisted once #onSave is
  // triggered, rather than immediately as they would be outside of this dialog.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  // Captured in initialize() before any panel can touch the model, so onCancel can undo whatever they
  // already applied to it in place. Null if there was no model to edit in the first place.
  private ModelSnapshot snapshot;

  private Stage stage;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
    errorContainerController.addStyleClass("error-container-no-radius");

    labelsController.configureModelLabels();
    subtitlesController.configureCustom("subtitle", "SUBTITLES");
    annotationsController.hideAnnotationDatasetsButton();

    modelSettingsNameController.setSaveMode(saveMode);
    generalSettingsController.setSaveMode(saveMode);
    generalDetachedRepeatSettingsController.setSaveMode(saveMode);
    generalInlineRepeatSettingsController.setSaveMode(saveMode);
    ruleConfirmationSettingsController.setSaveMode(saveMode);
    subtitleController.setSaveMode(saveMode);
    supportedCharactersController.setSaveMode(saveMode);
    localesController.setSaveMode(saveMode);
    labelsController.setSaveMode(saveMode);
    subtitlesController.setSaveMode(saveMode);
    rolesController.setSaveMode(saveMode);
    annotationsController.setSaveMode(saveMode);
    documentUniquenessCriteriaController.setSaveMode(saveMode);
    modelReferencesController.setSaveMode(saveMode);
    timezoneController.setSaveMode(saveMode);

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null && projectItem.getModel() != null) {
      A12Model<?> model = projectItem.getModel();
      snapshot = new ModelSnapshot(model);

      modelSettingsNameController.setModel(model);
      modelSettingsNameController.focusNameField();
      supportedCharactersController.setModel(model);
      localesController.setModel(model);
      labelsController.setModel(model);
      rolesController.setModel(model);
      annotationsController.setModel(model);
      modelReferencesController.setModel(model);
      if (model instanceof DocumentModel documentModel) {
        documentUniquenessCriteriaController.setModel(documentModel);
        documentUniquenessCriteriaController.setVisible(true);
        timezoneController.setModel(documentModel);
        timezoneController.setVisible(true);
      } else {
        documentUniquenessCriteriaController.setVisible(false);
        timezoneController.setVisible(false);
      }
      if (model instanceof OverviewModel overviewModel) {
        subtitlesController.setCustom(() -> ensureConfiguration(overviewModel).getSubtitle());
        subtitlesController.setVisible(true);
      } else {
        subtitlesController.setVisible(false);
      }
      if (model instanceof FormModel formModel) {
        generalSettingsController.setModel(formModel, ProjectDocumentModels.getOtherDocumentModels(projectItem));
        generalSettingsController.setVisible(true);
        generalDetachedRepeatSettingsController.setModel(formModel);
        generalDetachedRepeatSettingsController.setVisible(true);
        generalInlineRepeatSettingsController.setModel(formModel);
        generalInlineRepeatSettingsController.setVisible(true);
        ruleConfirmationSettingsController.setModel(formModel);
        ruleConfirmationSettingsController.setVisible(true);
        subtitleController.setModel(formModel);
        subtitleController.setVisible(true);
      } else {
        generalSettingsController.setVisible(false);
        generalDetachedRepeatSettingsController.setVisible(false);
        generalInlineRepeatSettingsController.setVisible(false);
        ruleConfirmationSettingsController.setVisible(false);
        subtitleController.setVisible(false);
      }
      supportedCharactersController.setVisible(
          !(model instanceof ApplicationModel) && !(model instanceof OverviewModel) && !(model instanceof FormModel)
              && !(model instanceof RelationshipModel) && !(model instanceof QueryModel));
      modelReferencesController.setVisible(
          !(model instanceof OverviewModel) && !(model instanceof FormModel) && !(model instanceof QueryModel));
    }

    bindErrorContainer();
  }

  private OverviewConfiguration ensureConfiguration(OverviewModel overviewModel) {
    if (overviewModel.getContent().getConfiguration() == null) {
      overviewModel.getContent().setConfiguration(new OverviewConfiguration());
    }
    return overviewModel.getContent().getConfiguration();
  }

  /**
   * Shows this dialog's own error container, with a generic title/message, whenever any of the property
   * editor panels it embeds is showing a validation error or warning in its own (panel-level) error
   * container. An error in any panel takes precedence over a warning, so the dialog only shows "Warning"
   * once every visible panel-level message is a warning.
   */
  private void bindErrorContainer() {
    List<AbstractPropertyEditor> panels = List.of(
        modelSettingsNameController,
        generalSettingsController,
        generalDetachedRepeatSettingsController,
        generalInlineRepeatSettingsController,
        ruleConfirmationSettingsController,
        subtitleController,
        supportedCharactersController,
        localesController,
        labelsController,
        subtitlesController,
        rolesController,
        annotationsController,
        documentUniquenessCriteriaController,
        modelReferencesController,
        timezoneController);

    Runnable updateErrorContainer = () -> {
      boolean anyError = panels.stream()
          .anyMatch(panel -> panel.errorProperty().get() && "ERROR".equalsIgnoreCase(panel.severityProperty().get()));
      boolean anyWarning = panels.stream()
          .anyMatch(panel -> panel.errorProperty().get() && "WARNING".equalsIgnoreCase(panel.severityProperty().get()));
      if (anyError) {
        errorContainerController.show("ERROR", StudioBundle.get("model_settings_dialog.generic_error_message"));
      } else if (anyWarning) {
        errorContainerController.show("WARNING", StudioBundle.get("model_settings_dialog.generic_warning_message"));
      } else {
        errorContainerController.hide();
      }
    };

    panels.forEach(panel -> {
      panel.errorProperty().addListener((observable, oldValue, newValue) -> updateErrorContainer.run());
      panel.severityProperty().addListener((observable, oldValue, newValue) -> updateErrorContainer.run());
    });
    updateErrorContainer.run();
  }

  @FXML
  private void onSave() {
    saveMode.flush();
    StudioEventManager.getInstance().fireLocalesChangedEvent(Studio.getSelectedProjectItem());
    stage.close();
  }

  @FXML
  private void onCancel() {
    onDialogCancel();
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    if (snapshot != null) {
      snapshot.restore();
    }
    StudioEventManager.getInstance().fireLocalesChangedEvent(Studio.getSelectedProjectItem());
  }

  /**
   * Unregisters {@code labelsController} (the only embedded panel that self-registers with {@link
   * StudioEventManager}) once this dialog is closed, regardless of how it was closed (Save, Cancel, or the
   * window's own close button) — see {@link Dialogs#openSettings}, which calls this from the stage's
   * {@code onHidden} handler.
   */
  public void destroy() {
    labelsController.destroy();
  }
}
