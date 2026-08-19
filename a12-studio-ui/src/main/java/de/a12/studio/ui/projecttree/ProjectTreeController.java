package de.a12.studio.ui.projecttree;

import de.a12.studio.models.A12Model;
import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.AdvancedSettings;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.*;
import de.a12.studio.ui.events.PreferencesOpenRequestedEvent;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.plugin.manager.ICreateItemMenuEntry;
import de.a12.studio.plugin.manager.PluginManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuButton;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ProjectTreeController implements Initializable, StudioEventListener {

  @FXML
  private TreeView<ProjectItemViewModel> projectTree;

  @FXML
  private MenuButton newButton;

  private Project project;
  private ProjectItemViewModel rootViewModel;
  private ProjectTreeMenuActions menuFactory;
  private Map<String, List<ModelValidationError>> validationErrorsByPath = new HashMap<>();

  @FXML
  private void onExpandAll() {
    setExpandedRecursive(projectTree.getRoot(), true);
  }

  @FXML
  private void onCollapseAll() {
    setExpandedRecursive(projectTree.getRoot(), false);
  }

  @FXML
  private void onReload() {
    if (project != null) {
      project.reload();
      load(project);
    }
  }

  public void load(@NonNull Project project) {
    this.project = project;
    this.validationErrorsByPath = validateAllModels(project);
    String applicationGroupName = resolveApplicationGroupName(project);
    this.rootViewModel = new ProjectItemViewModel(project.getRoot(), validationErrorsByPath, applicationGroupName);
    TreeItem<ProjectItemViewModel> rootTreeItem = toTreeItem(rootViewModel);
    rootTreeItem.setExpanded(true);
    projectTree.setRoot(rootTreeItem);
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    if (project == null || event.getItem().getModel() == null) {
      return;
    }
    refreshNode(event.getItem().getModel());
  }

  @Override
  public void modelDeleted(@NonNull ModelDeletedEvent event) {
    if (project == null || event.getItem().getModel() == null) {
      return;
    }
    refreshNode(event.getItem().getModel());
  }

  /**
   * Revalidates {@code model} plus every other model in the project, and redraws the tree if any of their
   * error sets actually changed as a result - not just {@code model}'s own. Several validators consult other
   * documents' content (see {@link de.a12.studio.modelsvalidation.ValidationContext#otherDocumentModels()}/
   * {@link de.a12.studio.modelsvalidation.ValidationContext#otherModels()}, e.g. an Include reference, a
   * Relationship/Form/Print/Tree/MasterDetail document reference, or a project-wide check like duplicate
   * model ids), so fixing (or introducing) an error in one document can change what's reported against a
   * completely different one. There's no cheap way to know in advance which other models actually reference
   * {@code model} without re-running their validators, so this simply revalidates all of them.
   */
  public void refreshNode(@NonNull A12Model<?> model) {
    if (project == null || findTreeItem(projectTree.getRoot(), model) == null) {
      return;
    }

    List<ProjectItem> modelItems = new ArrayList<>();
    collectModelItems(project.getRoot(), modelItems);
    boolean changed = false;
    for (ProjectItem item : modelItems) {
      changed |= refreshItemIfChanged(item);
    }

    // TreeView has no API to redraw a single row: TreeItem.setValue() fires TreeItem.valueChangedEvent(),
    // but TreeView's internal listener only reacts to events that derive from
    // TreeItem.expandedItemCountChangeEvent() (structural changes - children added/removed, branch
    // expanded/collapsed), so a plain value change is silently ignored and never triggers a layout pass or
    // re-invokes TreeCell.updateItem(). TreeView#refresh() is the only reliable way to get changed rows
    // redrawn, so it's used here - but only when something actually changed, to avoid rebuilding every
    // visible cell on every keystroke.
    if (changed) {
      projectTree.refresh();
    }
  }

  private boolean refreshItemIfChanged(@NonNull ProjectItem projectItem) {
    List<ModelValidationError> errors;
    try {
      errors = Studio.getValidationService().validate(projectItem.getModel());
    }
    catch (Exception e) {
      log.warn("Failed to validate '{}': {}", projectItem.getPath(), e.getMessage(), e);
      errors = List.of(new ModelValidationError(projectItem.getModel(), null, "Failed to parse document: " + e.getMessage(), "ERROR"));
    }

    List<ModelValidationError> previous = validationErrorsByPath.getOrDefault(projectItem.getPath(), List.of());
    if (errors.equals(previous)) {
      return false;
    }

    if (errors.isEmpty()) {
      validationErrorsByPath.remove(projectItem.getPath());
    }
    else {
      validationErrorsByPath.put(projectItem.getPath(), errors);
    }
    return true;
  }

  private void collectModelItems(@NonNull ProjectItem item, @NonNull List<ProjectItem> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectModelItems(child, result);
      }
    }
    else if (item.getModel() != null) {
      result.add(item);
    }
  }

  /**
   * Returns the configured application group name when application groups are enabled and the name
   * is non-blank, or {@code null} otherwise. Used to annotate the project root node in the tree.
   */
  private String resolveApplicationGroupName(@NonNull Project project) {
    AdvancedSettings advanced = project.getSettings().getAdvancedSettings();
    if (advanced.isUseApplicationGroups()) {
      String name = advanced.getApplicationGroupName();
      if (name != null && !name.isBlank()) {
        return name;
      }
    }
    return null;
  }

  private void onNewModel(@NonNull ModelType modelType) {
    if (project != null) {
      menuFactory.onCreateNewModel(resolveTargetFolder(), modelType);
    }
  }

  /**
   * Validates every model in the project, including cross-document include references, so the tree can flag
   * items with problems as soon as the project is opened.
   */
  private Map<String, List<ModelValidationError>> validateAllModels(@NonNull Project project) {
    List<ProjectItem> modelItems = new ArrayList<>();
    collectModelItems(project.getRoot(), modelItems);

    ValidationService validationService = Studio.getValidationService();
    Map<String, List<ModelValidationError>> validationErrorsByPath = new HashMap<>();
    for (ProjectItem item : modelItems) {
      try {
        List<ModelValidationError> errors = validationService.validate(item.getModel());
        if (!errors.isEmpty()) {
          validationErrorsByPath.put(item.getPath(), errors);
        }
      }
      catch (Exception e) {
        log.warn("Failed to validate '{}': {}", item.getPath(), e.getMessage(), e);
        validationErrorsByPath.put(item.getPath(),
            List.of(new ModelValidationError(item.getModel(), null, "Failed to parse document: " + e.getMessage(), "ERROR")));
      }

      Set<Map.Entry<String, List<ModelValidationError>>> entries = validationErrorsByPath.entrySet();
      for (Map.Entry<String, List<ModelValidationError>> entry : entries) {
        String key = entry.getKey();
        List<ModelValidationError> errors = entry.getValue();
        for (ModelValidationError error : errors) {
          log.error("[{}] {} Validation Issue: {}: {}", key, error.severity(), error.elementId(), error.message());
        }
      }
    }
    return validationErrorsByPath;
  }

  private void onNewFolder() {
    if (project != null) {
      menuFactory.onCreateNewFolder(resolveTargetFolder());
    }
  }

  private ProjectItem resolveTargetFolder() {
    TreeItem<ProjectItemViewModel> selected = projectTree.getSelectionModel().getSelectedItem();
    if (selected == null) {
      return project.getRoot();
    }
    ProjectItem item = selected.getValue().getProjectItem();
    return item.isFolder() ? item : item.getParent();
  }

  private void setExpandedRecursive(TreeItem<ProjectItemViewModel> treeItem, boolean expanded) {
    if (treeItem == null) {
      return;
    }
    treeItem.setExpanded(expanded);
    for (TreeItem<ProjectItemViewModel> child : treeItem.getChildren()) {
      setExpandedRecursive(child, expanded);
    }
  }

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    load(event.getProject());
  }

  @Override
  public void modelFocusRequested(@NonNull ModelFocusRequestedEvent event) {
    revealItem(event.getItem());
  }

  private void revealItem(@NonNull ProjectItem item) {
    TreeItem<ProjectItemViewModel> treeItem = findTreeItem(projectTree.getRoot(), item);
    if (treeItem == null) {
      return;
    }

    for (TreeItem<ProjectItemViewModel> parent = treeItem.getParent(); parent != null; parent = parent.getParent()) {
      parent.setExpanded(true);
    }

    projectTree.getSelectionModel().select(treeItem);
    projectTree.scrollTo(projectTree.getRow(treeItem));
  }

  private TreeItem<ProjectItemViewModel> findTreeItem(TreeItem<ProjectItemViewModel> treeItem, @NonNull ProjectItem target) {
    if (treeItem == null) {
      return null;
    }
    if (treeItem.getValue().getProjectItem().getPath().equals(target.getPath())) {
      return treeItem;
    }
    for (TreeItem<ProjectItemViewModel> child : treeItem.getChildren()) {
      TreeItem<ProjectItemViewModel> found = findTreeItem(child, target);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private TreeItem<ProjectItemViewModel> findTreeItem(TreeItem<ProjectItemViewModel> treeItem, @NonNull A12Model<?> model) {
    if (treeItem == null) {
      return null;
    }
    if (treeItem.getValue().getProjectItem().getModel() == model) {
      return treeItem;
    }
    for (TreeItem<ProjectItemViewModel> child : treeItem.getChildren()) {
      TreeItem<ProjectItemViewModel> found = findTreeItem(child, model);
      if (found != null) {
        return found;
      }
    }
    return null;
  }

  private TreeItem<ProjectItemViewModel> toTreeItem(@NonNull ProjectItemViewModel viewModel) {
    TreeItem<ProjectItemViewModel> treeItem = new TreeItem<>(viewModel);
    treeItem.setExpanded(true);
    for (ProjectItemViewModel child : viewModel.getChildren()) {
      treeItem.getChildren().add(toTreeItem(child));
    }
    return treeItem;
  }

  private void openItem(@NonNull ProjectItemViewModel viewModel) {
    if (viewModel.isSettings()) {
      StudioEventManager.getInstance().firePreferencesOpenRequestedEvent(
          PreferencesOpenRequestedEvent.Section.GENERAL_SETTINGS);
      return;
    }
    if (!viewModel.isFolder() && (viewModel.hasModel() || viewModel.hasAuthFile())) {
      if (project != null) {
        project.getSettings().getUISettings().addOpenedFile(viewModel.getProjectItem().getPath());
        project.getSettings().getUISettings().save();
      }
      StudioEventManager.getInstance().fireModelOpenEvent(viewModel.getProjectItem());
    }
  }

  private Stage getStage() {
    return (Stage) projectTree.getScene().getWindow();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    menuFactory = new ProjectTreeMenuActions(this::getStage, this::onReload, this::openItem);
    ProjectTreeContextMenu contextMenuFactory = new ProjectTreeContextMenu(menuFactory);
    for (ModelType modelType : ModelType.values()) {
      if (modelType == ModelType.DOCUMENT) {
        // Document Model gets a submenu with import options.
        Menu documentMenu = new Menu(modelType.getDisplayName());
        documentMenu.setGraphic(WidgetFactory.createModelIcon(Icons.forModelType(modelType)));

        MenuItem createBlank = new MenuItem(StudioBundle.get("new_document_model.create_blank"));
        createBlank.setOnAction(event -> onNewModel(ModelType.DOCUMENT));
        documentMenu.getItems().add(createBlank);

        // Append plugin-contributed "createMenu" extension points as import options.
        List<ICreateItemMenuEntry> pluginEntries = PluginManager.getInstance().getCreateMenuEntries();
        if (!pluginEntries.isEmpty()) {
          documentMenu.getItems().add(new SeparatorMenuItem());
          for (ICreateItemMenuEntry entry : pluginEntries) {
            MenuItem pluginItem = new MenuItem(entry.getMenuLabel());
            javafx.scene.Node graphic = entry.getMenuGraphic();
            if (graphic != null) {
              graphic.getStyleClass().add("menu-icon");
              pluginItem.setGraphic(graphic);
            }
            pluginItem.setOnAction(event -> entry.execute(getStage(), resolveTargetFolder()));
            documentMenu.getItems().add(pluginItem);
          }
        }

        newButton.getItems().add(documentMenu);
      }
      else {
        MenuItem modelItem = new MenuItem(modelType.getDisplayName());
        modelItem.setGraphic(WidgetFactory.createModelIcon(Icons.forModelType(modelType)));
        modelItem.setOnAction(event -> onNewModel(modelType));
        newButton.getItems().add(modelItem);
      }
    }
    newButton.getItems().add(new SeparatorMenuItem());
    MenuItem folderItem = new MenuItem(StudioBundle.get("folder"));
    FontIcon folderIcon = WidgetFactory.createIcon(Icons.FOLDER_OUTLINE);
    folderIcon.getStyleClass().add("menu-icon");
    folderItem.setGraphic(folderIcon);
    folderItem.setOnAction(event -> onNewFolder());
    newButton.getItems().add(folderItem);
    StudioEventManager.getInstance().addListener(this);
    projectTree.setOnKeyPressed(event -> {
      TreeItem<ProjectItemViewModel> selected = projectTree.getSelectionModel().getSelectedItem();
      if (event.getCode() == KeyCode.ENTER) {
        if (selected != null) {
          openItem(selected.getValue());
        }
      }
      else if (event.getCode() == KeyCode.DELETE) {
        if (selected != null && !selected.getValue().getProjectItem().isRoot()
            && !selected.getValue().isSettings() && !selected.getValue().isAuthFile()) {
          menuFactory.onDeleteItem(selected.getValue().getProjectItem());
        }
      }
      else if (event.getCode() == KeyCode.F2) {
        if (selected != null && !selected.getValue().getProjectItem().isRoot()
            && !selected.getValue().isSettings() && !selected.getValue().isAuthFile()) {
          menuFactory.onRenameItem(selected.getValue().getProjectItem());
        }
      }
    });
    AtomicReference<ProjectItemViewModel> dragSource = new AtomicReference<>();
    projectTree.setCellFactory(treeView -> new ProjectTreeCell(this::openItem, menuFactory, contextMenuFactory, dragSource));
  }
}
