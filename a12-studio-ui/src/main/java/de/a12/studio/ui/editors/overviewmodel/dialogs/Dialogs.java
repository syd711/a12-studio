package de.a12.studio.ui.editors.overviewmodel.dialogs;

import de.a12.studio.models.overviewmodel.Column;
import de.a12.studio.modelsvalidation.validators.ElementIndex;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

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
}
