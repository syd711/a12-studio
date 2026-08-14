package de.a12.studio.ui.updater;

import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.Studio;
import javafx.stage.Stage;

/**
 * Entry points for the update dialogs, mirroring the app's other feature-scoped
 * {@code Dialogs} factories (e.g. {@code de.a12.studio.ui.editors.documentmodel.dialogs.Dialogs}).
 */
public class Dialogs {

  public static void openUpdateInfoDialog(String version) {
    Stage stage = WidgetFactory.createDialogStage(UpdateInfoDialogController.class, Studio.stage, StudioBundle.get("release_notes_for", version), "dialog-update-info.fxml");
    UpdateInfoDialogController controller = (UpdateInfoDialogController) stage.getUserData();
    controller.setData(stage, version);
    stage.showAndWait();
  }

  public static void openUpdateDialog(String version) {
    Stage stage = WidgetFactory.createDialogStage(UpdateDialogController.class, Studio.stage, StudioBundle.get("a12_studio_updater"), "dialog-updater.fxml");
    UpdateDialogController controller = (UpdateDialogController) stage.getUserData();
    controller.setData(stage, version);
    stage.showAndWait();
  }
}
