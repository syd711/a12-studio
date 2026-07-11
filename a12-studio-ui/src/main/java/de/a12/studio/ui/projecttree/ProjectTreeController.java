package de.a12.studio.ui.projecttree;

import de.a12.studio.commons.util.WidgetFactory;
import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.dataservices.projects.ProjectItem;
import de.a12.studio.ui.events.ModelFocusRequestedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Stage;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class ProjectTreeController implements Initializable, StudioEventListener {

  @FXML
  private TreeView<ProjectItemViewModel> projectTree;

  @FXML
  private TextField searchField;

  private Project project;
  private ProjectItemViewModel rootViewModel;

  public void load(@NonNull Project project) {
    this.project = project;
    this.rootViewModel = new ProjectItemViewModel(project.getRoot());
    applyFilter(searchField.getText());
  }

  @FXML
  private void onResetSearch() {
    searchField.clear();
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
    if (!searchField.getText().isEmpty()) {
      searchField.clear();
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
    if (!viewModel.isFolder()) {
      if (project != null) {
        project.getSettings().addOpenedFile(viewModel.getProjectItem().getPath());
        project.getSettings().save();
      }
      StudioEventManager.getInstance().fireModelOpenEvent(viewModel.getProjectItem());
    }
  }

  private ContextMenu createTreeItemContextMenu(@NonNull ProjectItemViewModel viewModel) {
    ProjectItem projectItem = viewModel.getProjectItem();

    Menu newMenu = new Menu("New...");
    MenuItem newFolder = new MenuItem("Folder");
    newFolder.setOnAction(event -> onCreateNewItem(projectItem, true));
    MenuItem newModel = new MenuItem("Model");
    newModel.setOnAction(event -> onCreateNewItem(projectItem, false));
    newMenu.getItems().addAll(newFolder, newModel);

    MenuItem open = new MenuItem("Open");
    open.setDisable(viewModel.isFolder());
    open.setOnAction(event -> openItem(viewModel));

    MenuItem rename = new MenuItem("Rename");
    rename.setDisable(projectItem.isRoot());
    rename.setOnAction(event -> onRenameItem(projectItem));

    MenuItem createCopy = new MenuItem("Create Copy");
    createCopy.setDisable(projectItem.isRoot());
    createCopy.setOnAction(event -> onCreateCopy(projectItem));

    MenuItem delete = new MenuItem("Delete");
    delete.setDisable(projectItem.isRoot());
    delete.setOnAction(event -> onDeleteItem(projectItem));

    return new ContextMenu(newMenu, open, rename, createCopy, new SeparatorMenuItem(), delete);
  }

  private void onCreateNewItem(@NonNull ProjectItem parent, boolean folder) {
    String title = folder ? "New Folder" : "New Model";
    String name = WidgetFactory.showInputDialog(getStage(), title, title, null, null, null);
    if (name == null || name.isBlank()) {
      return;
    }

    try {
      if (folder) {
        parent.createChildFolder(name.trim());
      }
      else {
        parent.createChildModel(name.trim());
      }
      onReload();
    }
    catch (IOException e) {
      showError("Could not create '" + name + "'", e);
    }
  }

  private void onRenameItem(@NonNull ProjectItem item) {
    String name = WidgetFactory.showInputDialog(getStage(), "Rename", "Rename", null, null, item.getName());
    if (name == null || name.isBlank() || name.equals(item.getName())) {
      return;
    }

    try {
      item.renameTo(name.trim());
      onReload();
    }
    catch (IOException e) {
      showError("Could not rename to '" + name + "'", e);
    }
  }

  private void onCreateCopy(@NonNull ProjectItem item) {
    try {
      item.createCopy();
      onReload();
    }
    catch (IOException e) {
      showError("Could not copy '" + item.getName() + "'", e);
    }
  }

  private void onDeleteItem(@NonNull ProjectItem item) {
    Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(), "Delete '" + item.getName() + "'?", null, null, "Delete");
    if (result.isPresent() && result.get() == ButtonType.OK) {
      try {
        item.delete();
        onReload();
      }
      catch (IOException e) {
        showError("Could not delete '" + item.getName() + "'", e);
      }
    }
  }

  private void showError(@NonNull String message, @NonNull Exception e) {
    WidgetFactory.showAlert(getStage(), message, e.getMessage());
  }

  private Stage getStage() {
    return (Stage) projectTree.getScene().getWindow();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
    searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter(newValue));
    projectTree.setOnKeyPressed(event -> {
      TreeItem<ProjectItemViewModel> selected = projectTree.getSelectionModel().getSelectedItem();
      if (event.getCode() == KeyCode.ENTER) {
        if (selected != null) {
          openItem(selected.getValue());
        }
      }
      else if (event.getCode() == KeyCode.DELETE) {
        if (selected != null && !selected.getValue().getProjectItem().isRoot()) {
          onDeleteItem(selected.getValue().getProjectItem());
        }
      }
      else if (event.getCode() == KeyCode.F2) {
        if (selected != null && !selected.getValue().getProjectItem().isRoot()) {
          onRenameItem(selected.getValue().getProjectItem());
        }
      }
    });
    projectTree.setCellFactory(treeView -> new TreeCell<>() {
      private final FontIcon icon = new FontIcon();

      {
        icon.getStyleClass().add("tree-icon");
        setOnMouseClicked(event -> {
          if (event.getClickCount() == 2 && !isEmpty() && getItem() != null) {
            openItem(getItem());
          }
        });
      }

      private final ChangeListener<Boolean> expandedListener = (observable, wasExpanded, expanded) ->
          icon.setIconLiteral(expanded ? Icons.FOLDER_OPEN_OUTLINE : Icons.FOLDER_OUTLINE);
      private TreeItem<ProjectItemViewModel> boundTreeItem;

      @Override
      protected void updateItem(ProjectItemViewModel item, boolean empty) {
        super.updateItem(item, empty);

        if (boundTreeItem != null) {
          boundTreeItem.expandedProperty().removeListener(expandedListener);
          boundTreeItem = null;
        }

        if (empty || item == null) {
          setText(null);
          setGraphic(null);
          setTooltip(null);
          setContextMenu(null);
          return;
        }

        setText(item.toString());
        setTooltip(new Tooltip(item.getName()));
        setContextMenu(createTreeItemContextMenu(item));
        if (item.isFolder()) {
          boundTreeItem = getTreeItem();
          icon.setIconLiteral(boundTreeItem.isExpanded() ? Icons.FOLDER_OPEN : Icons.FOLDER);
          icon.setIconSize(18);
          boundTreeItem.expandedProperty().addListener(expandedListener);
        }
        else {
          icon.setIconSize(18);
          icon.setIconLiteral(Icons.FILE_OUTLINE);
        }
        setGraphic(icon);
      }
    });
  }
}
