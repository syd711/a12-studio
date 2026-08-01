package de.a12.studio.ui;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.auth.RolesDocument;
import de.a12.studio.models.auth.UsersDocument;
import de.a12.studio.models.combinationmodel.CombinationModel;
import de.a12.studio.models.contentmodel.ContentModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.formmodel.FormModel;
import de.a12.studio.models.mappingmodel.MappingModel;
import de.a12.studio.models.masterdetailmodel.MasterDetailModel;
import de.a12.studio.models.overviewmodel.OverviewModel;
import de.a12.studio.models.printmodel.PrintModel;
import de.a12.studio.models.querymodel.QueryModel;
import de.a12.studio.models.relationshipmodel.RelationshipModel;
import de.a12.studio.models.treemodel.TreeModel;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.applicationmodel.ApplicationModelEditorController;
import de.a12.studio.ui.editors.auth.RolesEditorController;
import de.a12.studio.ui.editors.auth.UsersEditorController;
import de.a12.studio.ui.editors.combinationmodel.CombinationModelEditorController;
import de.a12.studio.ui.editors.contentmodel.ContentModelEditorController;
import de.a12.studio.ui.editors.documentmodel.DocumentModelEditorController;
import de.a12.studio.ui.editors.formmodel.FormModelEditorController;
import de.a12.studio.ui.editors.mappingmodel.MappingModelEditorController;
import de.a12.studio.ui.editors.masterdetailmodel.MasterDetailModelEditorController;
import de.a12.studio.ui.editors.overviewmodel.OverviewModelEditorController;
import de.a12.studio.ui.editors.querymodel.QueryModelEditorController;
import de.a12.studio.ui.editors.relationshipmodel.RelationshipModelEditorController;
import de.a12.studio.ui.editors.treemodel.TreeModelEditorController;
import de.a12.studio.ui.editors.typedefinitionmodel.TypeDefintionModelEditorController;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.io.IOException;

@Slf4j
public class EditorFactory {

  public static Parent create(@NonNull ProjectItem item) {
    try {
      Parent content = null;

      // TypeDefinitionModel extends DocumentModel, so this check must come first.
      if (item.getModel() instanceof TypeDefinitionModel) {
        FXMLLoader loader = new FXMLLoader(TypeDefintionModelEditorController.class.getResource("typedefinition-model-editor.fxml"));
        content = loader.load();
        TypeDefintionModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof DocumentModel) {
        FXMLLoader loader = new FXMLLoader(DocumentModelEditorController.class.getResource("document-model-editor.fxml"));
        content = loader.load();
        DocumentModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof ApplicationModel) {
        FXMLLoader loader = new FXMLLoader(ApplicationModelEditorController.class.getResource("application-model-editor.fxml"));
        content = loader.load();
        ApplicationModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof OverviewModel) {
        FXMLLoader loader = new FXMLLoader(OverviewModelEditorController.class.getResource("overview-model-editor.fxml"));
        content = loader.load();
        OverviewModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof MasterDetailModel) {
        FXMLLoader loader = new FXMLLoader(MasterDetailModelEditorController.class.getResource("master-detail-model-editor.fxml"));
        content = loader.load();
        MasterDetailModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof FormModel) {
        FXMLLoader loader = new FXMLLoader(FormModelEditorController.class.getResource("form-model-editor.fxml"));
        content = loader.load();
        FormModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof RelationshipModel) {
        FXMLLoader loader = new FXMLLoader(RelationshipModelEditorController.class.getResource("relationship-model-editor.fxml"));
        content = loader.load();
        RelationshipModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof ContentModel) {
        FXMLLoader loader = new FXMLLoader(ContentModelEditorController.class.getResource("content-model-editor.fxml"));
        content = loader.load();
        ContentModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof TreeModel) {
        FXMLLoader loader = new FXMLLoader(TreeModelEditorController.class.getResource("tree-model-editor.fxml"));
        content = loader.load();
        TreeModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof PrintModel) {
        WidgetFactory.showAlert(Studio.stage, "Print models are not supported yet.");
      }
      else if (item.getModel() instanceof CombinationModel) {
        FXMLLoader loader = new FXMLLoader(CombinationModelEditorController.class.getResource("combination-model-editor.fxml"));
        content = loader.load();
        CombinationModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof MappingModel) {
        FXMLLoader loader = new FXMLLoader(MappingModelEditorController.class.getResource("mapping-model-editor.fxml"));
        content = loader.load();
        MappingModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getModel() instanceof QueryModel) {
        FXMLLoader loader = new FXMLLoader(QueryModelEditorController.class.getResource("query-model-editor.fxml"));
        content = loader.load();
        QueryModelEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getAuthDocument() instanceof RolesDocument) {
        FXMLLoader loader = new FXMLLoader(RolesEditorController.class.getResource("roles-editor.fxml"));
        content = loader.load();
        RolesEditorController controller = loader.getController();
        controller.load(item);
      }
      else if (item.getAuthDocument() instanceof UsersDocument) {
        FXMLLoader loader = new FXMLLoader(UsersEditorController.class.getResource("users-editor.fxml"));
        content = loader.load();
        UsersEditorController controller = loader.getController();
        controller.load(item);
      }
      return content;
    }
    catch (IOException e) {
      log.error("Failed to load editor: {}", e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
    return null;
  }
}
