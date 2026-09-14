package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.models.documentmodel.ComputationElement;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.documentmodel.rulelang.RuleLanguageSyntaxChecker;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.PropertyEditorSaveMode;
import de.a12.studio.ui.editors.propertyeditors.PlainPathSuggestionProvider;
import de.a12.studio.ui.editors.propertyeditors.RuleEditorController;
import de.a12.studio.ui.editors.propertyeditors.RuleLanguageConstructs;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Modal Add/Edit dialog for a single {@link ComputationAlternative} (precondition/operation pair), opened from
 * {@link de.a12.studio.ui.editors.documentmodel.ComputationAlternativesPanelController} (via {@link Dialogs}) by
 * clicking a row or its Edit/Add button. Edits the real {@link ComputationAlternative} live via the embedded
 * {@link RuleEditorController}s, so - unlike the class's previous "mutate only in onDialogSubmit" shape - a
 * snapshot of both fields is taken up front and restored on Cancel, mirroring {@link
 * de.a12.studio.ui.editors.overviewmodel.dialogs.OverviewColumnDialogController}. Both panels share a {@link
 * PropertyEditorSaveMode.Deferred} so their edits aren't persisted to disk until this dialog's own commit
 * ({@link de.a12.studio.ui.editors.documentmodel.ComputationAlternativesPanelController}, which saves once after
 * {@link #isConfirmed()}).
 */
public class ComputationAlternativeDialogController implements DialogController {

  @FXML
  private RuleEditorController preconditionController;

  @FXML
  private RuleEditorController operationController;

  @FXML
  private Button okButton;

  private final PropertyEditorSaveMode.Deferred saveMode = new PropertyEditorSaveMode.Deferred();

  private Stage stage;

  private ComputationAlternative alternative;

  private String originalPrecondition;

  private String originalOperation;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    preconditionController.configureCustom("precondition", StudioBundle.get("precondition"));
    preconditionController.setSaveMode(saveMode);
    preconditionController.setValidator(RuleLanguageSyntaxChecker::validate);
    preconditionController.errorProperty().addListener((observable, oldValue, newValue) -> updateOkButton());
    operationController.configureCustom("operation", StudioBundle.get("operation"));
    operationController.setSaveMode(saveMode);
    operationController.setValidator(RuleLanguageSyntaxChecker::validate);
    operationController.errorProperty().addListener((observable, oldValue, newValue) -> updateOkButton());
  }

  void init(@NonNull Stage stage, @NonNull ComputationElement computation, @NonNull ComputationAlternative alternative) {
    this.stage = stage;
    this.alternative = alternative;
    this.originalPrecondition = alternative.getPrecondition();
    this.originalOperation = alternative.getOperation();

    preconditionController.setCustom(alternative::getPrecondition, alternative::setPrecondition);
    operationController.setCustom(alternative::getOperation, alternative::setOperation);

    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null && projectItem.getModel() instanceof DocumentModel documentModel) {
      PlainPathSuggestionProvider suggestionProvider = new PlainPathSuggestionProvider(new ElementIndex(documentModel), computation);
      preconditionController.setSuggestionProvider(suggestionProvider);
      operationController.setSuggestionProvider(suggestionProvider);
      preconditionController.setHighlightedFunctionNames(RuleLanguageConstructs.NAMES);
      operationController.setHighlightedFunctionNames(RuleLanguageConstructs.NAMES);
    }
    updateOkButton();
  }

  private void updateOkButton() {
    okButton.setDisable(preconditionController.errorProperty().get() || operationController.errorProperty().get());
  }

  /** Unregisters the embedded panels once this dialog is closed - see {@link Dialogs}. */
  void destroy() {
    preconditionController.destroy();
    operationController.destroy();
  }

  @Override
  public void onDialogCancel() {
    alternative.setPrecondition(originalPrecondition);
    alternative.setOperation(originalOperation);
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
