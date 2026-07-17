package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.dialogs.DocumentModelSettingsDialog;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class Dialogs {

  public static void openSettings() {
    FXMLLoader fxmlLoader = new FXMLLoader(DocumentModelSettingsDialog.class.getResource("/de/a12/studio/ui/editors/dialogs/document-model-settings-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("document-mode-settings", fxmlLoader, Studio.stage, "Settings");
    DocumentModelSettingsDialog controller = (DocumentModelSettingsDialog) stage.getUserData();
    stage.showAndWait();
  }

  public static void openTypeDefinitions() {
    FXMLLoader fxmlLoader = new FXMLLoader(TypeDefinitionSettingsDialog.class.getResource("document-model-typedefinitions-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("document-mode-settings", fxmlLoader, Studio.stage, "Type Definitions");
    TypeDefinitionSettingsDialog controller = (TypeDefinitionSettingsDialog) stage.getUserData();
    stage.showAndWait();
  }
}
