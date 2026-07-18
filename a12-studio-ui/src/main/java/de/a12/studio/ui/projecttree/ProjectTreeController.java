package de.a12.studio.ui.projecttree;

import de.a12.studio.commons.components.SearchFieldController;
import de.a12.studio.dataservices.models.documentmodel.DocumentModel;
import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.dataservices.services.documentmodel.features.validation.DMValidationService;
import de.a12.studio.dataservices.services.documentmodel.features.validation.ElementValidationError;
import de.a12.studio.ui.events.ModelFocusRequestedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.input.KeyCode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;

import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
public class ProjectTreeController implements Initializable, StudioEventListener {

  private static final DMValidationService VALIDATION_SERVICE = new DMValidationService();

  @FXML
  private TreeView<ProjectItemViewModel> projectTree;

  @FXML
  private SearchFieldController searchController;

  private Project project;
  private ProjectItemViewModel rootViewModel;
  private ProjectTreeMenuActions menuFactory;

  public void load(@NonNull Project project) {
    this.project = project;
    Map<String, List<ElementValidationError>> validationErrorsByPath = validateAllDocuments(project);
    this.rootViewModel = new ProjectItemViewModel(project.getRoot(), validationErrorsByPath);
    applyFilter(searchController.getText());
  }

  /**
   * Validates every document in the project, including cross-document include references, so the tree can
   * flag documents with problems as soon as the project is opened.
   */
  private Map<String, List<ElementValidationError>> validateAllDocuments(@NonNull Project project) {
    List<ProjectItem> documentItems = new ArrayList<>();
    collectDocumentItems(project.getRoot(), documentItems);

    List<DocumentModel> allModels = documentItems.stream()
        .map(item -> (DocumentModel) item.getModel())
        .toList();

    Map<String, List<ElementValidationError>> validationErrorsByPath = new HashMap<>();
    for (int i = 0; i < documentItems.size(); i++) {
      List<DocumentModel> otherModels = new ArrayList<>(allModels);
      otherModels.remove(i);
      try {
        List<ElementValidationError> errors = VALIDATION_SERVICE.validateDocument(allModels.get(i), otherModels);
        if (!errors.isEmpty()) {
          validationErrorsByPath.put(documentItems.get(i).getPath(), errors);
        }
      }
      catch (Exception e) {
        log.warn("Failed to validate '{}': {}", documentItems.get(i).getPath(), e.getMessage(), e);
        validationErrorsByPath.put(documentItems.get(i).getPath(),
            List.of(new ElementValidationError(null, "Failed to parse document: " + e.getMessage(), "ERROR")));
      }

      Set<Map.Entry<String, List<ElementValidationError>>> entries = validationErrorsByPath.entrySet();
      for (Map.Entry<String, List<ElementValidationError>> entry : entries) {
        String key = entry.getKey();
        List<ElementValidationError> errors = entry.getValue();
        for (ElementValidationError error : errors) {
            log.error("[{}] {} Validation Issue: {}: {}", key, error.severity(), error.elementId(), error.message());
        }
      }
    }
    return validationErrorsByPath;
  }

  private void collectDocumentItems(@NonNull ProjectItem item, @NonNull List<ProjectItem> result) {
    if (item.isFolder()) {
      for (ProjectItem child : item.getChildren()) {
        collectDocumentItems(child, result);
      }
    }
    else if (item.getModel() instanceof DocumentModel) {
      result.add(item);
    }
  }

  private void applyFilter(String filter) {
    if (rootViewModel == null) {
      return;
    }

    String term = filter == null ? "" : filter.trim().toLowerCase();
    TreeItem<ProjectItemViewModel> rootTreeItem = term.isEmpty()
        ? toTreeItem(rootViewModel)
        : toFilteredTreeItem(rootViewModel, term);

    if (rootTreeItem == null) {
      rootTreeItem = new TreeItem<>(rootViewModel);
    }
    rootTreeItem.setExpanded(true);
    projectTree.setRoot(rootTreeItem);
  }

  private TreeItem<ProjectItemViewModel> toFilteredTreeItem(@NonNull ProjectItemViewModel viewModel, @NonNull String term) {
    List<TreeItem<ProjectItemViewModel>> matchingChildren = new ArrayList<>();
    for (ProjectItemViewModel child : viewModel.getChildren()) {
      TreeItem<ProjectItemViewModel> filteredChild = toFilteredTreeItem(child, term);
      if (filteredChild != null) {
        matchingChildren.add(filteredChild);
      }
    }

    boolean selfMatches = viewModel.getName().toLowerCase().contains(term);
    if (!selfMatches && matchingChildren.isEmpty()) {
      return null;
    }

    TreeItem<ProjectItemViewModel> treeItem = new TreeItem<>(viewModel);
    treeItem.getChildren().addAll(matchingChildren);
    treeItem.setExpanded(!matchingChildren.isEmpty());
    return treeItem;
  }

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
    if (!searchController.getText().isEmpty()) {
      searchController.clear();
    }

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
    StudioEventManager.getInstance().addListener(this);
    searchController.setOnSearch(this::applyFilter);
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
