package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.Studio;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;

public class DocumentModelDialogs {

  public static void openTypeDefinitions() {
    FXMLLoader fxmlLoader = new FXMLLoader(TypeDefinitionSettingsDialog.class.getResource("document-model-typedefinitions-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("document-type-settings", fxmlLoader, Studio.stage, "Type Definitions");
    TypeDefinitionSettingsDialog controller = (TypeDefinitionSettingsDialog) stage.getUserData();

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(800);
    stage.setMinHeight(600);


    stage.showAndWait();
  }
}
