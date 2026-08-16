package de.a12.studio.ui.editors.querymodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.RichtextEditorController;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Single-field dialog editing a {@link QueryModelContent#getFilterDefinition()} via the shared {@link
 * RichtextEditorController} expression editor, mirroring {@code OverviewColumnDialogController}'s use of the
 * same panel: a {@link PropertyEditorSaveMode.Deferred} save mode defers persisting to OK, and {@link
 * #originalValue} restores the in-memory value on Cancel (the panel's writer still mutates {@code content}
 * live as the user types, deferred save mode only skips the file write - see {@link RichtextEditorController
 * #setCustom}).
 */
public class QueryFilterDefinitionDialogController implements DialogController {

  @FXML
  private RichtextEditorController expressionPanelController;

  @FXML
  private Button okButton;
  @FXML
  private Button cancelButton;

  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;
  private QueryModelContent content;
  private String originalValue;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    expressionPanelController.setSaveMode(saveMode);
  }

  void init(@NonNull Stage stage, @NonNull QueryModelContent content) {
    this.stage = stage;
    this.content = content;
    this.originalValue = content.getFilterDefinition();
    expressionPanelController.setCustom(content::getFilterDefinition, content::setFilterDefinition);
  }

  void destroy() {
    expressionPanelController.destroy();
  }

  @Override
  public void onDialogCancel() {
    content.setFilterDefinition(originalValue);
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null) {
      projectItem.save();
      StudioEventManager.getInstance().fireModelSavedEvent(projectItem);
    }
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
