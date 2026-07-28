package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.LocalizedTextPanelController;
import de.a12.studio.ui.editors.propertyeditors.SceneChangePanelController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.Optional;

/**
 * Add/edit dialog for a single {@link Case} of a {@link de.a12.studio.models.applicationmodel.Scene}, opened
 * from {@link de.a12.studio.ui.editors.propertyeditors.CasesPanelController}. Follows the same edit-in-place
 * pattern as {@link ChildMenuDialogController}: for an edit, the embedded panels mutate the real, already-
 * attached {@link Case} live, so a {@link CaseSnapshot} taken before showing the dialog can undo it on Cancel;
 * for an add, they mutate a new, not-yet-attached {@link Case} that the caller only attaches to the parent
 * scene's cases list once this dialog resolves with {@link ButtonType#OK}.
 */
public class CaseDialogController implements DialogController {

  @FXML
  private TextField nameField;

  @FXML
  private LocalizedTextPanelController labelController;

  @FXML
  private SceneChangePanelController sceneChangeController;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  // Shared by the embedded panels so their commits aren't persisted while the dialog is open: an edit is
  // persisted directly by onDialogSubmit below once OK is pressed, and an add is only persisted by the caller
  // once the new case is attached to its parent scene's cases list.
  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private Case caseObj;

  // Non-null only when editing an existing, already-attached case, so onDialogCancel can undo in-place edits;
  // null for a new case that's never attached until OK, which needs no undo.
  private CaseSnapshot snapshot;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    labelController.configureCaseLabel();
    labelController.setSaveMode(saveMode);
    okButton.disableProperty().bind(nameField.textProperty().map(String::isBlank));
    nameField.requestFocus();
  }

  void init(Stage stage, Case caseObj, CaseSnapshot snapshot) {
    this.stage = stage;
    this.caseObj = caseObj;
    this.snapshot = snapshot;

    nameField.setText(caseObj.getName());
    labelController.setCase(caseObj);
    sceneChangeController.bind(caseObj::getSceneChange, caseObj::setSceneChange, false);
  }

  void destroy() {
    labelController.destroy();
  }

  @Override
  public void onDialogCancel() {
    if (snapshot != null) {
      snapshot.restore();
    }
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    caseObj.setName(nameField.getText().trim());
    result = Optional.of(ButtonType.OK);

    if (snapshot != null) {
      ProjectItem projectItem = Studio.getSelectedProjectItem();
      if (projectItem != null) {
        projectItem.save();
        StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
      }
    }
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
