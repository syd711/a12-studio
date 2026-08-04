package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.applicationmodel.ApplicationModel;
import de.a12.studio.models.applicationmodel.Module;
import de.a12.studio.ui.editors.AbstractEditorController;
import de.a12.studio.ui.events.ModelClosedEvent;
import de.a12.studio.ui.preview.PreviewLauncher;
import de.a12.studio.ui.util.localsettings.BaseTableSettings;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ApplicationModelEditorController extends AbstractEditorController implements Initializable {

  private static final String MODULE_EDITOR_FXML = "module-editor.fxml";

  @FXML
  private ActivityPanelController activityController;

  @FXML
  private ModulesPanelController modulesController;

  @FXML
  private LayoutPanelController layoutController;

  @FXML
  private RegionPanelController regionController;

  @FXML
  private SubregionsPanelController subregionsController;

  @FXML
  private SplitPane splitPane;

  @FXML
  private VBox editorContainer;

  private ModuleEditorController currentModuleEditorController;

  @FXML
  public void onPreview(ActionEvent e) {
    PreviewLauncher.openPreview(projectItem);
  }

  public void loadModel(@NonNull A12Model<?> model) {
    load((ApplicationModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull ApplicationModel documentModel) {
    modulesController.setModel(documentModel);
    activityController.setModel(documentModel);
    layoutController.setModel(documentModel);
    regionController.setModel(documentModel);
    subregionsController.setModel(documentModel);
  }

  private void openModuleEditor(@NonNull Module module) {
    closeModuleEditor();

    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(MODULE_EDITOR_FXML));
loader.setResources(StudioBundle.getBundle());
      Node node = loader.load();
      VBox.setVgrow(node, Priority.ALWAYS);
      currentModuleEditorController = loader.getController();
      currentModuleEditorController.setModule(module);
      currentModuleEditorController.setOnCloseRequested(this::closeModuleEditor);
      editorContainer.getChildren().setAll(node);
      if (!splitPane.getItems().contains(editorContainer)) {
        splitPane.getItems().add(editorContainer);
        splitPane.setDividerPositions(0.5);
      }
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private void closeModuleEditor() {
    if (currentModuleEditorController != null) {
      currentModuleEditorController.destroy();
      currentModuleEditorController = null;
    }
    editorContainer.getChildren().clear();
    splitPane.getItems().remove(editorContainer);
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BaseTableSettings tableSettings = getBaseTableSettings();
    modulesController.setOnEditModule(this::openModuleEditor);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.APPLICATION;
  }

  /**
   * In addition to unregistering this editor itself (see {@link AbstractEditorController#modelClosed}), tears
   * down whichever module editor panel is currently displayed in {@code editorContainer}, since it isn't
   * otherwise reached once the tab is gone.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    super.modelClosed(event);
    if (currentModuleEditorController != null && event.getItem().equals(projectItem)) {
      currentModuleEditorController.destroy();
      currentModuleEditorController = null;
    }
  }
}
