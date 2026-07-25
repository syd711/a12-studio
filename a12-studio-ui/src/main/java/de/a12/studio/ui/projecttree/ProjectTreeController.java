package de.a12.studio.ui.projecttree;

import de.a12.studio.models.ModelType;
import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.modelsvalidation.ModelValidationError;
import de.a12.studio.modelsvalidation.ValidationService;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.events.*;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
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

  public void load(@NonNull Project project) {
    this.project = project;
    this.validationErrorsByPath = validateAllModels(project);
    this.rootViewModel = new ProjectItemViewModel(project.getRoot(), validationErrorsByPath);
    TreeItem<ProjectItemViewModel> rootTreeItem = toTreeItem(rootViewModel);
    rootTreeItem.setExpanded(true);
    projectTree.setRoot(rootTreeItem);
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    if (project == null) {
      return;
    }
    validationErrorsByPath.clear();
    validationErrorsByPath.putAll(validateAllModels(project));
    projectTree.refresh();
  }

  @Override
  public void modelDeleted(@NonNull ModelDeletedEvent event) {
    if (project == null) {
      return;
    }
    validationErrorsByPath.clear();
    validationErrorsByPath.putAll(validateAllModels(project));
    projectTree.refresh();
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

  private void onNewModel(@NonNull ModelType modelType) {
    if (project != null) {
      menuFactory.onCreateNewModel(resolveTargetFolder(), modelType);
    }
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

  private TreeItem<ProjectItemViewModel> toTreeItem(@NonNull ProjectItemViewModel viewModel) {
    TreeItem<ProjectItemViewModel> treeItem = new TreeItem<>(viewModel);
    treeItem.setExpanded(true);
    for (ProjectItemViewModel child : viewModel.getChildren()) {
      treeItem.getChildren().add(toTreeItem(child));
    }
    return treeItem;
  }

  private void openItem(@NonNull ProjectItemViewModel viewModel) {
    if (!viewModel.isFolder() && viewModel.hasModel()) {
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
    for (ModelType modelType : ModelType.values()) {
      MenuItem modelItem = new MenuItem(modelType.getDisplayName());
      modelItem.setGraphic(WidgetFactory.createModelIcon(Icons.forModelType(modelType)));
      modelItem.setOnAction(event -> onNewModel(modelType));
      newButton.getItems().add(modelItem);
    }
    newButton.getItems().add(new SeparatorMenuItem());
    MenuItem folderItem = new MenuItem("Folder");
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
        if (selected != null && !selected.getValue().getProjectItem().isRoot()) {
          menuFactory.onDeleteItem(selected.getValue().getProjectItem());
        }
      }
      else if (event.getCode() == KeyCode.F2) {
        if (selected != null && !selected.getValue().getProjectItem().isRoot()) {
          menuFactory.onRenameItem(selected.getValue().getProjectItem());
        }
      }
    });
    AtomicReference<ProjectItemViewModel> dragSource = new AtomicReference<>();
    projectTree.setCellFactory(treeView -> new ProjectTreeCell(this::openItem, menuFactory, dragSource));
  }
}
