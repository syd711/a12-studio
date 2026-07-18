package de.a12.studio.ui.editors.dialogs;

import de.a12.studio.commons.util.FXResizeHelper;
import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.ui.Studio;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class EditorDialogs {

  public static void openSettings() {
    FXMLLoader fxmlLoader = new FXMLLoader(DocumentModelSettingsDialog.class.getResource("/de/a12/studio/ui/editors/dialogs/document-model-settings-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("document-mode-settings", fxmlLoader, Studio.stage, "Model Settings");
    DocumentModelSettingsDialog controller = (DocumentModelSettingsDialog) stage.getUserData();
    controller.setStage(stage);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(800);
    stage.setMinHeight(600);

    stage.showAndWait();
  }
}
