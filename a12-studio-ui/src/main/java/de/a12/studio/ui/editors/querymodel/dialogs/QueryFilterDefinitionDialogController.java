package de.a12.studio.ui.editors.querymodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.querymodel.QueryModelContent;
import de.a12.studio.models.querymodel.ql.QueryLanguageEmitter;
import de.a12.studio.models.querymodel.ql.QueryLanguageException;
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
 * #setCustom}). The text is validated against {@link QueryLanguageEmitter} (see
 * docs/sme-reference-comparison.md "Query Model" section) so a typo shows a real parse error instead of being
 * silently accepted as opaque free text.
 */
public class QueryFilterDefinitionDialogController implements DialogController {

  private static final QueryLanguageEmitter EMITTER = new QueryLanguageEmitter();

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
    expressionPanelController.setValidator(QueryFilterDefinitionDialogController::validate);
    okButton.disableProperty().bind(expressionPanelController.errorProperty());
  }

  void init(@NonNull Stage stage, @NonNull QueryModelContent content) {
    this.stage = stage;
    this.content = content;
    this.originalValue = content.getFilterDefinition();
    expressionPanelController.setCustom(content::getFilterDefinition, content::setFilterDefinition);
  }

  private static String validate(String text) {
    try {
      EMITTER.emit(text);
      return null;
    } catch (QueryLanguageException e) {
      return "Invalid filter expression: " + e.getMessage();
    }
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
