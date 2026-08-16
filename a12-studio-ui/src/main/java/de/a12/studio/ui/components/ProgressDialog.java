package de.a12.studio.ui.components;

import de.a12.studio.ui.Studio;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.stage.Stage;

public class ProgressDialog {

  public static <T> ProgressResultModel createProgressDialog(ProgressModel<T> model) {
    return createProgressDialog(Studio.stage, model);
  }

  public static <T> ProgressResultModel createProgressDialog(Stage parentStage, ProgressModel<T> model) {
    Stage stage = WidgetFactory.createDialogStage(ProgressDialogController.class, parentStage, model.getTitle(), "dialog-progress.fxml");
    stage.setAlwaysOnTop(true);
    ProgressDialogController controller = (ProgressDialogController) stage.getUserData();
    controller.setProgressModel(stage, model);
    stage.showAndWait();
    ProgressResultModel progressResult = controller.getProgressResult();
    stage.close();
    return progressResult;
  }
}
