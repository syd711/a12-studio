package de.a12.studio.ui.editors.documentmodel.dialogs;

import de.a12.studio.models.additivedocumentmodel.AdditiveDocumentModelResolver.AdditiveContext;
import de.a12.studio.models.documentmodel.ComputationAlternative;
import de.a12.studio.models.documentmodel.ComputationElement;
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

  /**
   * Opens the "Insert from Document Model" picker; {@code otherModels} is every other Document Model of the project,
   * the dialog itself keeps only those {@link de.a12.studio.modelsvalidation.documentinsertion.DocumentModelInsertion#isCandidate}
   * accepts for {@code target}.
   */
  public static Optional<DocumentModel> showInsertFromModel(Stage owner, @NonNull DocumentModel target, @NonNull List<DocumentModel> otherModels) {
    FXMLLoader fxmlLoader = new FXMLLoader(InsertFromModelDialogController.class.getResource("insert-from-model-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("insert-from-model-dialog", fxmlLoader, owner, StudioBundle.get("insert_from_model.title"));
    InsertFromModelDialogController controller = (InsertFromModelDialogController) stage.getUserData();
    controller.init(stage, target, otherModels);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  public static Optional<Result> showCreateOverviewModel(Stage owner, @NonNull ProjectItem targetFolder,
      @NonNull DocumentModel documentModel, @NonNull List<FieldOption> fields, @NonNull String defaultName) {
    FXMLLoader fxmlLoader = new FXMLLoader(CreateOverviewModelDialogController.class.getResource("create-overview-model-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("create-overview-model-dialog", fxmlLoader, owner, StudioBundle.get("create_overview_model_from_selection"));
    CreateOverviewModelDialogController controller = (CreateOverviewModelDialogController) stage.getUserData();
    controller.init(stage, targetFolder, documentModel, fields, defaultName);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  /**
   * Opens the "Select Combination Model" picker shown when an Additive Document Model is referenced by more
   * than one Combination Model (see {@link AdditiveContextDialogController}); {@code candidates} must have at
   * least two entries, since a single candidate is resolved silently without asking.
   */
  public static Optional<AdditiveContext> showAdditiveContext(Stage owner, @NonNull List<AdditiveContext> candidates) {
    FXMLLoader fxmlLoader = new FXMLLoader(AdditiveContextDialogController.class.getResource("additive-context-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("additive-context-dialog", fxmlLoader, owner, StudioBundle.get("additive_context_dialog.title"));
    AdditiveContextDialogController controller = (AdditiveContextDialogController) stage.getUserData();
    controller.init(stage, candidates);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }

  public static boolean showComputationAlternativeForAdd(Stage owner, @NonNull ComputationElement computation, @NonNull ComputationAlternative alternative) {
    return showComputationAlternative(owner, StudioBundle.get("add_alternative_title"), computation, alternative);
  }

  /**
   * Opens the Computation Alternative dialog for an existing, already-attached {@link ComputationAlternative}.
   * Returns whether OK was pressed; {@code alternative} is edited live, but a snapshot taken before showing the
   * dialog is restored on Cancel (see {@link ComputationAlternativeDialogController}).
   */
  public static boolean showComputationAlternativeForEdit(Stage owner, @NonNull ComputationElement computation, @NonNull ComputationAlternative alternative) {
    return showComputationAlternative(owner, StudioBundle.get("edit_alternative_title"), computation, alternative);
  }

  private static boolean showComputationAlternative(Stage owner, String title, @NonNull ComputationElement computation, @NonNull ComputationAlternative alternative) {
    FXMLLoader fxmlLoader = new FXMLLoader(ComputationAlternativeDialogController.class.getResource("computation-alternative-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("computation-alternative-dialog", fxmlLoader, owner, title);
    ComputationAlternativeDialogController controller = (ComputationAlternativeDialogController) stage.getUserData();
    controller.init(stage, computation, alternative);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }
}
