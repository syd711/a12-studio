package de.a12.studio.ui.editors.relationshipmodel.dialogs;

import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.relationshipmodel.EntityCharacteristic;
import de.a12.studio.models.relationshipmodel.LinkConstraints;
import de.a12.studio.models.relationshipmodel.Multiplicity;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class Dialogs {

  private Dialogs() {
  }

  public static Optional<EntityCharacteristic> showEntityForAdd(Stage owner, List<String> documentModelOptions) {
    EntityCharacteristic entity = new EntityCharacteristic();
    entity.setOrdered(false);
    Multiplicity multiplicity = new Multiplicity();
    multiplicity.setUnbounded(true);
    LinkConstraints constraints = new LinkConstraints();
    constraints.setMultiplicity(multiplicity);
    entity.setLinkConstraints(constraints);
    return showEntity(owner, StudioBundle.get("add_entity_title"), documentModelOptions, entity)
        ? Optional.of(entity) : Optional.empty();
  }

  public static boolean showEntityForEdit(Stage owner, List<String> documentModelOptions, EntityCharacteristic entity) {
    return showEntity(owner, StudioBundle.get("edit_entity_title"), documentModelOptions, entity);
  }

  /**
   * Opens the entity editor for {@code entity}, editing it live so a Cancel can undo the changes.
   */
  private static boolean showEntity(Stage owner, String title, List<String> documentModelOptions, EntityCharacteristic entity) {
    FXMLLoader fxmlLoader = new FXMLLoader(EntityCharacteristicDialogController.class.getResource("entity-characteristic-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("entity-characteristic-dialog", fxmlLoader, owner, title);
    EntityCharacteristicDialogController controller = (EntityCharacteristicDialogController) stage.getUserData();
    controller.init(stage, entity, documentModelOptions);
    stage.setOnHidden(event -> controller.destroy());
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.isConfirmed();
  }

  /**
   * Opens the target-folder picker for {@link
   * de.a12.studio.ui.editors.relationshipmodel.RelationshipModelEditorController#onGenerateDocumentModels}, a
   * plain Location combo reusing {@link de.a12.studio.ui.util.ProjectModelFolders#configureLocationCombo}, the
   * same folder chooser as the "New Model" dialog.
   */
  public static Optional<ProjectItem> showGenerateDocumentModelsFolder(Stage owner, @NonNull ProjectItem targetFolder) {
    FXMLLoader fxmlLoader = new FXMLLoader(GenerateDocumentModelsDialogController.class.getResource("generate-document-models-dialog.fxml"));
    fxmlLoader.setResources(StudioBundle.getBundle());
    Stage stage = WidgetFactory.createDialogStage("generate-document-models-dialog", fxmlLoader, owner, StudioBundle.get("generate_document_models"));
    GenerateDocumentModelsDialogController controller = (GenerateDocumentModelsDialogController) stage.getUserData();
    controller.init(stage, targetFolder);
    WidgetFactory.installResizable(stage);

    stage.showAndWait();
    return controller.getResult();
  }
}
