package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.documentmodel.dialogs.IncludeDialogController.IncludeInput;
import de.a12.studio.ui.util.FXResizeHelper;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class Dialogs {

  public static void openTypeDefinitions() {
    FXMLLoader fxmlLoader = new FXMLLoader(TypeDefinitionSettingsDialog.class.getResource("document-model-typedefinitions-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("document-type-settings", fxmlLoader, Studio.stage, "Type Definitions");
    TypeDefinitionSettingsDialog controller = (TypeDefinitionSettingsDialog) stage.getUserData();
    controller.setStage(stage);

    FXResizeHelper.install(stage, 30, 6);
    stage.setMinWidth(800);
    stage.setMinHeight(600);
    stage.setOnHidden(event -> controller.destroy());

    stage.showAndWait();
  }

  public static Optional<IncludeInput> showInclude(Stage owner, @NonNull Project project, DocumentModel excludedModel, String defaultName) {
    FXMLLoader fxmlLoader = new FXMLLoader(IncludeDialogController.class.getResource("include-dialog.fxml"));
    Stage stage = WidgetFactory.createDialogStage("include-dialog", fxmlLoader, owner, "New Include");
    IncludeDialogController controller = (IncludeDialogController) stage.getUserData();
    controller.init(stage, project, excludedModel, defaultName);
    stage.showAndWait();
    return controller.getResult();
  }
}
