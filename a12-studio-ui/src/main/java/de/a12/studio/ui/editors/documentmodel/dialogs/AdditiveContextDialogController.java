package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModelResolver.AdditiveContext;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.editors.documentmodel.DocumentModelElementsTreeController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.util.StringConverter;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

/**
 * Modal picker shown when an {@link de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModel} is
 * referenced by more than one Combination Model: {@link
 * de.a12.studio.ui.editors.documentmodel.DocumentModelElementsTreeController}'s reverse lookup ({@link
 * de.a12.studio.ui.util.AdditiveDocumentModels#findCandidateContexts}) can no longer silently pick a base
 * model to preview against, so the user is asked which Combination Model to treat as context for this
 * editor session - mirroring SME's own {@code SelectModelWithContextView} for the same ambiguity. The
 * choice is never persisted: it only lives for as long as this editor tab stays open (see {@link
 * DocumentModelElementsTreeController#resolveAdditiveState}), same as SME asks again every time the model
 * is reopened.
 */
public class AdditiveContextDialogController implements DialogController {

  @FXML
  private ComboBox<AdditiveContext> contextComboBox;

  @FXML
  private Button okButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    contextComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(AdditiveContext context) {
        return context == null ? "" : StudioBundle.get("additive_context_dialog.entry",
            context.combinationModelId(), context.baseModel().getId());
      }

      @Override
      public AdditiveContext fromString(String string) {
        return null;
      }
    });
    okButton.disableProperty().bind(contextComboBox.valueProperty().isNull());
  }

  void init(Stage stage, @NonNull List<AdditiveContext> candidates) {
    this.stage = stage;
    contextComboBox.getItems().setAll(candidates);
    contextComboBox.getSelectionModel().selectFirst();
    contextComboBox.requestFocus();
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  Optional<AdditiveContext> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.ofNullable(contextComboBox.getValue());
    }
    return Optional.empty();
  }
}
