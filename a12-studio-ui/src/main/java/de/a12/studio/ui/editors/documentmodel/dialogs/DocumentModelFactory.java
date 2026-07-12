package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.ui.Studio;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class DocumentModelFactory {

  public static void openSettings() {
    FXMLLoader fxmlLoader = new FXMLLoader(DocumentModelSettingsDialog.class.getResource("document-model-settings-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("document-mode-settings", fxmlLoader, Studio.stage, "Settings");
    DocumentModelSettingsDialog controller = (DocumentModelSettingsDialog) stage.getUserData();
    stage.showAndWait();
  }
}
