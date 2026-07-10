package de.a12.studio.ui.projecttree;

import de.a12.studio.dataservices.projects.Project;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import javafx.beans.value.ChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
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

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
    searchField.textProperty().addListener((observable, oldValue, newValue) -> applyFilter(newValue));
    projectTree.setOnKeyPressed(event -> {
      if (event.getCode() == KeyCode.ENTER) {
        TreeItem<ProjectItemViewModel> selected = projectTree.getSelectionModel().getSelectedItem();
        if (selected != null) {
          openItem(selected.getValue());
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
          return;
        }

        setText(item.toString());
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
