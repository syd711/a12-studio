package de.a12.studio.ui.editors.mappingmodel.dialogs;

import de.a12.studio.models.mappingmodel.MappingSource;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class Dialogs {

  private Dialogs() {
  }

  /**
   * Opens the (currently empty) source model editor for {@code sourceModel}. No result is returned yet since
   * the dialog has no fields to submit.
   */
  public static void showSourceModel(Stage owner, MappingSource sourceModel) {
    FXMLLoader fxmlLoader = new FXMLLoader(SourceModelDialogController.class.getResource("source-model-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("source-model-dialog", fxmlLoader, owner, "Edit Source Model");
    SourceModelDialogController controller = (SourceModelDialogController) stage.getUserData();
    controller.initDialog(stage, sourceModel);
    stage.showAndWait();
  }
}
