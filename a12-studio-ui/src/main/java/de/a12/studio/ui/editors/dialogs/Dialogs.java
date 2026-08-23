package de.a12.studio.ui.editors.dialogs;

import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.Studio;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class Dialogs {

  public static void openSettings() {
    FXMLLoader fxmlLoader = new FXMLLoader(ModelSettingsDialog.class.getResource("/de/a12/studio/ui/editors/dialogs/document-model-settings-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("document-mode-settings", fxmlLoader, Studio.stage, StudioBundle.get("model_settings"));
    ModelSettingsDialog controller = (ModelSettingsDialog) stage.getUserData();
    controller.setStage(stage);
    WidgetFactory.installResizable(stage);
    stage.setOnHidden(event -> controller.destroy());

    stage.showAndWait();
  }
}
