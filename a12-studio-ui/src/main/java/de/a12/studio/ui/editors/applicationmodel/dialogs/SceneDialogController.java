package de.a12.studio.ui.editors.applicationmodel.dialogs;

import de.a12.studio.models.applicationmodel.Case;
import de.a12.studio.models.applicationmodel.Flow;
import de.a12.studio.models.applicationmodel.Scene;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.propertyeditors.CasesPanelController;
import de.a12.studio.ui.editors.propertyeditors.MatchConditionsPanelController;
import de.a12.studio.ui.editors.propertyeditors.SceneChangePanelController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Add/edit dialog for a single {@link Scene} of a {@link Flow} in {@link
 * de.a12.studio.ui.editors.propertyeditors.FlowsPanelController}. Own fields (Name, Description, Prior Scene,
 * Default Case) are wired directly here; the Match Conditions, Scene Change and Cases sections are delegated to
 * {@link MatchConditionsPanelController}, {@link SceneChangePanelController} and {@link CasesPanelController}.
 * Follows the same edit-in-place pattern as {@link ChildMenuDialogController}/{@link CaseDialogController}: for
 * an edit, the embedded panels mutate the real, already-attached {@link Scene} live, so a {@link SceneSnapshot}
 * taken before showing the dialog can undo it on Cancel; for an add, they mutate a new, not-yet-attached
 * {@link Scene} that the caller only attaches to the parent flow's scenes list once this dialog resolves with
 * {@link ButtonType#OK}.
 */
public class SceneDialogController implements DialogController {

  @FXML
  private TextField nameField;

  @FXML
  private TextField descriptionField;

  @FXML
  private ComboBox<String> priorSceneCombo;

  @FXML
  private ComboBox<String> defaultCaseCombo;

  @FXML
  private MatchConditionsPanelController matchConditionsController;

  @FXML
  private SceneChangePanelController sceneChangeController;

  @FXML
  private CasesPanelController casesController;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private Flow flow;

  private Scene scene;

  // Non-null only when editing an existing, already-attached scene, so onDialogCancel can undo in-place edits;
  // null for a new scene that's never attached until OK, which needs no undo.
  private SceneSnapshot snapshot;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    okButton.disableProperty().bind(nameField.textProperty().map(String::isBlank));
    priorSceneCombo.setEditable(true);
    defaultCaseCombo.setEditable(true);
    nameField.requestFocus();
  }

  void init(Stage stage, Flow flow, Scene scene, SceneSnapshot snapshot) {
    this.stage = stage;
    this.flow = flow;
    this.scene = scene;
    this.snapshot = snapshot;

    nameField.setText(scene.getName());
    nameField.textProperty().addListener((observable, oldValue, newValue) -> scene.setName(newValue));

    descriptionField.setText(scene.getDescription());
    descriptionField.textProperty().addListener((observable, oldValue, newValue) -> scene.setDescription(newValue.isEmpty() ? null : newValue));

    priorSceneCombo.getItems().setAll(priorSceneOptions());
    priorSceneCombo.setValue(scene.getPriorScene());
    priorSceneCombo.valueProperty().addListener((observable, oldValue, newValue) -> scene.setPriorScene(newValue == null || newValue.isEmpty() ? null : newValue));

    matchConditionsController.setMatchConditions(scene.getMatchConditions());
    sceneChangeController.bind(scene::getSceneChange, scene::setSceneChange, true);

    casesController.setScene(scene);
    casesController.setOnChange(this::refreshDefaultCaseItems);
    refreshDefaultCaseItems();
    defaultCaseCombo.valueProperty().addListener((observable, oldValue, newValue) -> scene.setDefaultCase(newValue == null || newValue.isEmpty() ? null : newValue));
  }

  private List<String> priorSceneOptions() {
    return flow.getScenes().stream()
        .filter(sibling -> sibling != scene)
        .map(Scene::getName)
        .filter(Objects::nonNull)
        .toList();
  }

  private void refreshDefaultCaseItems() {
    List<String> items = scene.getCases().stream().map(Case::getName).filter(Objects::nonNull).toList();
    defaultCaseCombo.getItems().setAll(items);
    defaultCaseCombo.setValue(scene.getDefaultCase());
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
    scene.setName(nameField.getText().trim());
    result = Optional.of(ButtonType.OK);

    if (snapshot != null) {
      ProjectItem projectItem = Studio.getSelectedProjectItem();
      if (projectItem != null) {
        projectItem.save();
        StudioEventManager.getInstance().fireModelSaveEvent(projectItem);
      }
    }
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
