package de.a12.studio.ui.editors.combineddocumentmodel.dialogs;

import de.a12.studio.models.combineddocumentmodel.CombinationStep;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class Dialogs {

  private Dialogs() {
  }

  /**
   * Opens the Combination Step dialog for a new, not-yet-attached {@link CombinationStep}; the caller only
   * attaches it to the model's Combination Steps list once this resolves with OK, so Cancel needs no undo.
   */
  public static Optional<CombinationStep> showCombinationStepForAdd(Stage owner, List<String> documentModelIds, List<String> selectionModelIds) {
    CombinationStep combinationStep = new CombinationStep();
    return showCombinationStep(owner, StudioBundle.get("add_combination_step_title"), combinationStep, documentModelIds, selectionModelIds)
        ? Optional.of(combinationStep) : Optional.empty();
  }

  /**
   * Opens the Combination Step dialog for an existing, already-attached {@link CombinationStep}. Returns
   * whether OK was pressed; {@code combinationStep} itself is only mutated on OK (see
   * {@link CombinationStepDialogController}), so Cancel leaves it untouched.
   */
  public static boolean showCombinationStepForEdit(Stage owner, CombinationStep combinationStep, List<String> documentModelIds, List<String> selectionModelIds) {
    return showCombinationStep(owner, StudioBundle.get("edit_combination_step_title"), combinationStep, documentModelIds, selectionModelIds);
  }

  private static boolean showCombinationStep(Stage owner, String title, CombinationStep combinationStep,
      List<String> documentModelIds, List<String> selectionModelIds) {
    FXMLLoader fxmlLoader = new FXMLLoader(CombinationStepDialogController.class.getResource("combination-step-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("combinationstep-dialog", fxmlLoader, owner, title);
    CombinationStepDialogController controller = (CombinationStepDialogController) stage.getUserData();
    controller.initDialog(stage, combinationStep, documentModelIds, selectionModelIds);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }
}
