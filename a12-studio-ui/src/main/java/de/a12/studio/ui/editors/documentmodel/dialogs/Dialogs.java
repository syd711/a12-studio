package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.editors.documentmodel.dialogs.CreateOverviewModelDialogController.FieldOption;
import de.a12.studio.ui.editors.documentmodel.dialogs.CreateOverviewModelDialogController.Result;
import de.a12.studio.ui.editors.documentmodel.dialogs.IncludeDialogController.IncludeInput;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

import de.a12.studio.ui.util.StudioBundle;

public class Dialogs {

  public static void openTypeDefinitions() {
    FXMLLoader fxmlLoader = new FXMLLoader(TypeDefinitionSettingsDialog.class.getResource("document-model-typedefinitions-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("document-type-settings", fxmlLoader, Studio.stage, StudioBundle.get("type_definitions") + titleSuffix());
    TypeDefinitionSettingsDialog controller = (TypeDefinitionSettingsDialog) stage.getUserData();
    controller.setStage(stage);
    WidgetFactory.installResizable(stage);
    stage.setOnHidden(event -> controller.destroy());

    stage.showAndWait();
  }

  private static String titleSuffix() {
    ProjectItem projectItem = Studio.getSelectedProjectItem();
    if (projectItem != null && projectItem.getModel() instanceof DocumentModel documentModel) {
      return " - " + documentModel.getId();
    }
    return "";
  }

  public static Optional<IncludeInput> showInclude(Stage owner, @NonNull Project project, DocumentModel excludedModel, String defaultName) {
    FXMLLoader fxmlLoader = new FXMLLoader(IncludeDialogController.class.getResource("include-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("include-dialog", fxmlLoader, owner, StudioBundle.get("new_include"));
    IncludeDialogController controller = (IncludeDialogController) stage.getUserData();
    controller.init(stage, project, excludedModel, defaultName);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<Result> showCreateOverviewModel(Stage owner, @NonNull ProjectItem targetFolder,
      @NonNull List<FieldOption> fields, @NonNull String defaultName) {
    FXMLLoader fxmlLoader = new FXMLLoader(CreateOverviewModelDialogController.class.getResource("create-overview-model-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("create-overview-model-dialog", fxmlLoader, owner, StudioBundle.get("create_overview_model_from_selection"));
    CreateOverviewModelDialogController controller = (CreateOverviewModelDialogController) stage.getUserData();
    controller.init(stage, targetFolder, fields, defaultName);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }
}
