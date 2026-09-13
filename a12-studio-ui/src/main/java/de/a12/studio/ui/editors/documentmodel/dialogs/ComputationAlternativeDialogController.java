package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.ui.components.DialogController;
import javafx.fxml.FXML;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

/**
 * Modal Add/Edit dialog for a single {@link ComputationAlternative} (precondition/operation pair), opened from
 * {@link de.a12.studio.ui.editors.documentmodel.ComputationAlternativesPanelController} (via {@link Dialogs}) by
 * clicking a row or its Edit/Add button. Same "mutate only in onDialogSubmit" shape as {@link
 * de.a12.studio.ui.editors.combineddocumentmodel.dialogs.CombinationStepDialogController} - Cancel needs no
 * snapshot/undo since {@code alternative} is only ever written once, on OK.
 */
public class ComputationAlternativeDialogController implements DialogController {

  @FXML
  private TextArea preconditionField;

  @FXML
  private TextArea operationField;

  private Stage stage;

  private ComputationAlternative alternative;

  private Optional<ButtonType> result = Optional.of(ButtonType.CANCEL);

  void init(@NonNull Stage stage, @NonNull ComputationAlternative alternative) {
    this.stage = stage;
    this.alternative = alternative;

    preconditionField.setText(alternative.getPrecondition());
    operationField.setText(alternative.getOperation());
  }

  @Override
  public void onDialogCancel() {
    stage.close();
  }

  @FXML
  private void onDialogSubmit() {
    alternative.setPrecondition(blankToNull(preconditionField.getText()));
    alternative.setOperation(blankToNull(operationField.getText()));

    result = Optional.of(ButtonType.OK);
    stage.close();
  }

  private static String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value;
  }

  boolean isConfirmed() {
    return result.isPresent() && result.get() == ButtonType.OK;
  }
}
