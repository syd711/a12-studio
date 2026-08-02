package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Button;
import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

import java.util.Optional;

public class Dialogs {

  private Dialogs() {
  }

  /**
   * Opens the (currently empty) column editor for {@code column}. No result is returned yet since the dialog
   * has no fields to submit.
   */
  public static void showColumn(Stage owner, ElementIndex documentModelIndex, Column column) {
    FXMLLoader fxmlLoader = new FXMLLoader(OverviewColumnDialogController.class.getResource("overview-column-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("overview-column-dialog", fxmlLoader, owner, "Edit Column");
    OverviewColumnDialogController controller = (OverviewColumnDialogController) stage.getUserData();
    controller.initDialog(stage, documentModelIndex, column);
    stage.showAndWait();
  }

  public static Optional<Button> showMultiSelectionActionForAdd(Stage owner) {
    Button button = new Button();
    return showMultiSelectionAction(owner, "Add Action", button) ? Optional.of(button) : Optional.empty();
  }

  public static boolean showMultiSelectionActionForEdit(Stage owner, Button button) {
    return showMultiSelectionAction(owner, "Edit Action", button);
  }

  private static boolean showMultiSelectionAction(Stage owner, String title, Button button) {
    FXMLLoader fxmlLoader = new FXMLLoader(MultiSelectionActionDialogController.class.getResource("overview-multi-selection-action-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("overview-multi-selection-action-dialog", fxmlLoader, owner, title);
    MultiSelectionActionDialogController controller = (MultiSelectionActionDialogController) stage.getUserData();
    controller.initDialog(stage, button);
    stage.showAndWait();
    return controller.isConfirmed();
  }
}
