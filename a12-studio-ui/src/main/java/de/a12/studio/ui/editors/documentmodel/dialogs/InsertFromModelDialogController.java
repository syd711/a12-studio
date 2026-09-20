package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.modelsvalidation.documentinsertion.DocumentModelInsertion;
import de.a12.studio.ui.components.DialogController;
import de.a12.studio.ui.util.StudioBundle;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Modal picker for "Insert from Document Model...": choose the Document Model whose content is copied into the
 * open one (see {@link DocumentModelInsertion}). Only models {@link DocumentModelInsertion#isCandidate} accepts
 * are offered, sorted by id; the dialog cannot be submitted without a choice.
 */
public class InsertFromModelDialogController implements DialogController {

  @FXML
  private ComboBox<DocumentModel> sourceComboBox;

  @FXML
  private Label noCandidatesLabel;

  @FXML
  private Button okButton;

  @FXML
  private Button cancelButton;

  private Stage stage;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  @FXML
  private void initialize() {
    sourceComboBox.setConverter(new StringConverter<>() {
      @Override
      public String toString(DocumentModel model) {
        return model == null ? "" : model.getId();
      }

      @Override
      public DocumentModel fromString(String string) {
        return null;
      }
    });
    okButton.disableProperty().bind(sourceComboBox.valueProperty().isNull());
    noCandidatesLabel.setText(StudioBundle.get("insert_from_model.no_candidates"));
  }

  void init(Stage stage, @NonNull DocumentModel target, @NonNull List<DocumentModel> otherModels) {
    this.stage = stage;
    List<DocumentModel> candidates = otherModels.stream()
        .filter(model -> DocumentModelInsertion.isCandidate(target, model))
        .sorted(Comparator.comparing(DocumentModel::getId))
        .toList();
    sourceComboBox.getItems().setAll(candidates);
    noCandidatesLabel.setVisible(candidates.isEmpty());
    noCandidatesLabel.setManaged(candidates.isEmpty());
    if (!candidates.isEmpty()) {
      sourceComboBox.requestFocus();
    }
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

  Optional<DocumentModel> getResult() {
    if (result.isPresent() && result.get() == ButtonType.OK) {
      return Optional.ofNullable(sourceComboBox.getValue());
    }
    return Optional.empty();
  }
}
