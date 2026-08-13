package de.a12.studio.ui.editors.formmodel.dialogs;

import de.a12.studio.models.formmodel.Defaults;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Edits {@code content.defaults.buttonLabels} - the model-wide overrides for the built-in repeat-widget button
 * labels (ADD/CANCEL/COMMIT_ADD/...), one {@link LocalizedTextPanelController} per action, matching the SME
 * reference's {@code I_SectionDefaultRepeatButtonLabels-form.json} order. Edits commit immediately (each panel's
 * default {@link de.a12.studio.ui.editors.PropertyEditorSaveMode#IMMEDIATE}), so this dialog only needs a single
 * Close button rather than Save/Cancel.
 */
public class RepeatDefaultButtonLabelsDialogController implements Initializable, DialogController {

  @FXML
  private LocalizedTextPanelController addLabelController;
  @FXML
  private LocalizedTextPanelController commitAddLabelController;
  @FXML
  private LocalizedTextPanelController applyLabelController;
  @FXML
  private LocalizedTextPanelController editLabelController;
  @FXML
  private LocalizedTextPanelController removeLabelController;
  @FXML
  private LocalizedTextPanelController viewLabelController;
  @FXML
  private LocalizedTextPanelController cancelLabelController;
  @FXML
  private LocalizedTextPanelController confirmLabelController;
  @FXML
  private LocalizedTextPanelController returnLabelController;
  @FXML
  private LocalizedTextPanelController upLabelController;
  @FXML
  private LocalizedTextPanelController downLabelController;
  @FXML
  private LocalizedTextPanelController copyLabelController;
  @FXML
  private LocalizedTextPanelController closeLabelController;
  @FXML
  private LocalizedTextPanelController downloadLabelController;
  @FXML
  private LocalizedTextPanelController skipLabelController;
  @FXML
  private LocalizedTextPanelController replaceLabelController;
  @FXML
  private LocalizedTextPanelController uploadAsCopyLabelController;

  private Stage stage;

  public void setStage(Stage stage) {
    this.stage = stage;
  }

  @Override
  public void initialize(URL location, ResourceBundle resources) {
  }

  public void setModel(@NonNull FormModel model) {
    Defaults defaults = ensureDefaults(model);

    addLabelController.configureButtonLabel("ADD", "ADD");
    addLabelController.setDefaults(defaults);
    commitAddLabelController.configureButtonLabel("COMMIT_ADD", "COMMIT ADD");
    commitAddLabelController.setDefaults(defaults);
    applyLabelController.configureButtonLabel("APPLY", "APPLY");
    applyLabelController.setDefaults(defaults);
    editLabelController.configureButtonLabel("EDIT", "EDIT");
    editLabelController.setDefaults(defaults);
    removeLabelController.configureButtonLabel("REMOVE", "REMOVE");
    removeLabelController.setDefaults(defaults);
    viewLabelController.configureButtonLabel("VIEW", "VIEW");
    viewLabelController.setDefaults(defaults);
    cancelLabelController.configureButtonLabel("CANCEL", "CANCEL");
    cancelLabelController.setDefaults(defaults);
    confirmLabelController.configureButtonLabel("CONFIRM", "CONFIRM");
    confirmLabelController.setDefaults(defaults);
    returnLabelController.configureButtonLabel("RETURN", "RETURN");
    returnLabelController.setDefaults(defaults);
    upLabelController.configureButtonLabel("UP", "UP");
    upLabelController.setDefaults(defaults);
    downLabelController.configureButtonLabel("DOWN", "DOWN");
    downLabelController.setDefaults(defaults);
    copyLabelController.configureButtonLabel("COPY", "COPY");
    copyLabelController.setDefaults(defaults);
    closeLabelController.configureButtonLabel("CLOSE", "CLOSE");
    closeLabelController.setDefaults(defaults);
    downloadLabelController.configureButtonLabel("DOWNLOAD", "DOWNLOAD");
    downloadLabelController.setDefaults(defaults);
    skipLabelController.configureButtonLabel("SKIP", "SKIP");
    skipLabelController.setDefaults(defaults);
    replaceLabelController.configureButtonLabel("REPLACE", "REPLACE");
    replaceLabelController.setDefaults(defaults);
    uploadAsCopyLabelController.configureButtonLabel("UPLOAD_AS_COPY", "UPLOAD AS COPY");
    uploadAsCopyLabelController.setDefaults(defaults);
  }

  private static Defaults ensureDefaults(FormModel model) {
    Defaults defaults = model.getContent().getDefaults();
    if (defaults == null) {
      defaults = new Defaults();
      model.getContent().setDefaults(defaults);
    }
    return defaults;
  }

  @FXML
  private void onClose() {
    stage.close();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  /**
   * Unregisters every embedded panel from {@link de.a12.studio.ui.events.StudioEventManager} - see {@link
   * Dialogs#openRepeatDefaultButtonLabels}, which calls this from the stage's {@code onHidden} handler.
   */
  public void destroy() {
    for (LocalizedTextPanelController controller : List.of(
        addLabelController, commitAddLabelController, applyLabelController, editLabelController,
        removeLabelController, viewLabelController, cancelLabelController, confirmLabelController,
        returnLabelController, upLabelController, downLabelController, copyLabelController,
        closeLabelController, downloadLabelController, skipLabelController, replaceLabelController,
        uploadAsCopyLabelController)) {
      controller.destroy();
    }
  }
}
