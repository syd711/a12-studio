package de.a12.studio.ui.editors.applicationmodel;

import de.a12.studio.ui.util.StudioBundle;

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
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.jspecify.annotations.NonNull;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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
  private TabPane tabPane;

  private ApplicationModel model;

  // Tracks which modules currently have an open tab. A module missing from this map has no tab, either
  // because it was just added (before its tab is created) or because the user closed its tab manually.
  private final Map<Module, Tab> moduleTabs = new LinkedHashMap<>();

  @FXML
  public void onPreview(ActionEvent e) {
    PreviewLauncher.openPreview(projectItem);
  }

  public void loadModel(@NonNull A12Model<?> model) {
    load((ApplicationModel) model);
    updateSettingsErrorBadge();
  }

  private void load(@NonNull ApplicationModel applicationModel) {
    this.model = applicationModel;
    modulesController.setModel(applicationModel);
    activityController.setModel(applicationModel);
    layoutController.setModel(applicationModel);
    regionController.setModel(applicationModel);
    subregionsController.setModel(applicationModel);

    for (Module module : applicationModel.getContent().getModules()) {
      addModuleTab(module);
    }
  }

  /** Selects the given module's tab, (re)creating it first if the user had previously closed it. */
  private void openModuleTab(@NonNull Module module) {
    Tab tab = moduleTabs.get(module);
    if (tab == null) {
      addModuleTab(module);
      tab = moduleTabs.get(module);
    }
    tabPane.getSelectionModel().select(tab);
  }

  private void addModuleTab(@NonNull Module module) {
    Tab tab = createModuleTab(module);
    moduleTabs.put(module, tab);
    tabPane.getTabs().add(tab);
    reorderModuleTabs();
  }

  private void removeModuleTab(@NonNull Module module) {
    Tab tab = moduleTabs.get(module);
    if (tab != null) {
      // Removing the tab fires its onClosed handler, which tears down the controller and drops the map entry.
      tabPane.getTabs().remove(tab);
    }
  }

  /** Reorders the tabs currently open to match the model's module order (the Overview tab always stays first). */
  private void reorderModuleTabs() {
    List<Tab> ordered = new ArrayList<>();
    ordered.add(tabPane.getTabs().get(0));
    for (Module module : model.getContent().getModules()) {
      Tab tab = moduleTabs.get(module);
      if (tab != null) {
        ordered.add(tab);
      }
    }
    tabPane.getTabs().setAll(ordered);
  }

  private Tab createModuleTab(@NonNull Module module) {
    Tab tab = new Tab(module.getName());
    try {
      FXMLLoader loader = new FXMLLoader(getClass().getResource(MODULE_EDITOR_FXML));
      loader.setResources(StudioBundle.getBundle());
      Node node = loader.load();
      ModuleEditorController controller = loader.getController();
      controller.setOnNameChanged(tab::setText);
      controller.setModule(module);
      tab.setContent(node);
      tab.setUserData(controller);
      tab.setOnClosed(event -> {
        controller.destroy();
        moduleTabs.remove(module);
      });
    }
    catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return tab;
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    BaseTableSettings tableSettings = getBaseTableSettings();
    modulesController.setOnEditModule(this::openModuleTab);
    modulesController.setOnModuleAdded(this::openModuleTab);
    modulesController.setOnModuleRemoved(this::removeModuleTab);
    modulesController.setOnModulesReordered(this::reorderModuleTabs);
  }

  @Override
  public @NonNull ModelType getModelType() {
    return ModelType.APPLICATION;
  }

  /**
   * In addition to unregistering this editor itself (see {@link AbstractEditorController#modelClosed}), tears
   * down every module editor currently open in a tab, since it isn't otherwise reached once the tab is gone.
   */
  @Override
  public void modelClosed(@NonNull ModelClosedEvent event) {
    super.modelClosed(event);
    if (event.getItem().equals(projectItem)) {
      for (Tab tab : moduleTabs.values()) {
        ((ModuleEditorController) tab.getUserData()).destroy();
      }
      moduleTabs.clear();
    }
  }
}
