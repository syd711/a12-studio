package de.a12.studio.ui;

import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.documentmodel.DocumentModel;
import de.a12.studio.models.typedefinitionmodel.TypeDefinitionModel;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.ui.editors.applicationmodel.ApplicationModelEditorController;
import de.a12.studio.ui.editors.documentmodel.DocumentModelEditorController;
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
      return content;
    }
    catch (IOException e) {
      log.error("Failed to load editor: {}", e.getMessage(), e);
      WidgetFactory.showAlert(Studio.stage, e.getMessage());
    }
    return null;
  }
}
