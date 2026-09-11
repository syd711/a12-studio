package de.a12.studio.ui.versioncontrol;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.VersionControlSettings;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.JFXFuture;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;

@Slf4j
public class VersioncontrolPanelController implements Initializable, StudioEventListener {

  @FXML
  private TreeTableView<VersioncontrolTreeNode> changesTree;

  @FXML
  private TreeTableColumn<VersioncontrolTreeNode, Boolean> checkColumn;

  @FXML
  private TreeTableColumn<VersioncontrolTreeNode, String> nameColumn;

  @FXML
  private Button refreshButton;

  @FXML
  private Button revertButton;

  @FXML
  private Button collapseProjectViewButton;

  @FXML
  private TextArea commitMessageField;

  @FXML
  private Button commitButton;

  private final Map<String, SimpleBooleanProperty> checkedByPath = new HashMap<>();
  private final VersionControlSettings settings = VersionControlSettings.load();

  private Project project;
  private GitService gitService;
  private List<GitChangedFile> currentChangedFiles = List.of();
  private boolean updatingCommitMessageField = false;

  private Runnable collapseProjectViewCallback;

  public void setCollapseProjectViewCallback(Runnable callback) {
    this.collapseProjectViewCallback = callback;
  }

  @FXML
  private void onCollapseProjectView() {
    if (collapseProjectViewCallback != null) {
      collapseProjectViewCallback.run();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    checkColumn.setCellValueFactory(param -> {
      VersioncontrolTreeNode node = param.getValue().getValue();
      if (node == null) {
        return new SimpleBooleanProperty(false);
      }
      return checkedByPath.computeIfAbsent(node.getRelativePath(), p -> new SimpleBooleanProperty(false));
    });
    checkColumn.setCellFactory(col -> new TreeTableCell<>() {
      private final CheckBox checkBox = new CheckBox();

      {
        checkBox.setFocusTraversable(false);
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
          if (isEmpty() || getTreeTableRow().getTreeItem() == null) {
            return;
          }
          setCheckedRecursive(getTreeTableRow().getTreeItem(), newVal);
          updateActionButtons();
        });
      }

      @Override
      protected void updateItem(Boolean value, boolean empty) {
        super.updateItem(value, empty);
        if (empty || value == null) {
          setGraphic(null);
          return;
        }
        VersioncontrolTreeNode node = getTreeTableRow() == null ? null : getTreeTableRow().getItem();
        if (node == null) {
          setGraphic(null);
          return;
        }
        SimpleBooleanProperty prop = checkedByPath.computeIfAbsent(node.getRelativePath(), p -> new SimpleBooleanProperty(false));
        checkBox.selectedProperty().unbind();
        checkBox.setSelected(prop.get());
        prop.addListener((o, ov, nv) -> checkBox.setSelected(nv));
        setGraphic(checkBox);
      }
    });

    nameColumn.setCellValueFactory(param -> {
      VersioncontrolTreeNode node = param.getValue().getValue();
      return new ReadOnlyStringWrapper(node == null ? "" : node.getDisplayName());
    });
    nameColumn.setCellFactory(col -> new TreeTableCell<>() {
      @Override
      protected void updateItem(String value, boolean empty) {
        super.updateItem(value, empty);
        VersioncontrolTreeNode node = getTreeTableRow() == null ? null : getTreeTableRow().getItem();
        if (empty || node == null) {
          setText(null);
          setGraphic(null);
          return;
        }
        setText(value);
        setGraphic(node.isFolder() ? WidgetFactory.createIcon(Icons.FOLDER_OUTLINE) : statusIcon(node.getChangedFile().status()));
      }
    });

    changesTree.setShowRoot(true);
    changesTree.setPlaceholder(new Label(StudioBundle.get("versioncontrol_no_changes")));

    commitMessageField.textProperty().addListener((obs, oldVal, newVal) -> {
      updateActionButtons();
      if (updatingCommitMessageField || project == null) {
        return;
      }
      settings.getLastCommitMessages().put(project.getFolder().getAbsolutePath(), newVal);
      settings.save();
    });

    updateActionButtons();

    StudioEventManager.getInstance().addListener(this);
  }

  private FontIcon statusIcon(ChangeStatus status) {
    return switch (status) {
      case NEW -> WidgetFactory.createGreenIcon(Icons.PLUS);
      case MODIFIED -> WidgetFactory.createIcon(Icons.PENCIL);
      case DELETED -> WidgetFactory.createAlertIcon(Icons.TRASH);
      case CONFLICTING -> WidgetFactory.createExclamationIcon();
    };
  }

  // -------------------------------------------------------------------------
  // Project lifecycle
  // -------------------------------------------------------------------------

  @Override
  public void projectOpened(@NonNull ProjectOpenedEvent event) {
    setProject(event.getProject());
  }

  /**
   * Pushes {@code project} into this panel directly, bypassing {@link ProjectOpenedEvent}. Needed
   * because this panel's FXML/controller is lazily loaded on first show (see
   * {@code RootController.getVersioncontrolPanelRoot()}) - if that happens after a project is
   * already open, this controller was never registered as a {@link StudioEventListener} at the
   * time {@link ProjectOpenedEvent} fired, so it would otherwise never learn which project/git
   * repository to show changes for, even on manual refresh.
   */
  public void setProject(Project project) {
    this.project = project;
    closeGitService();
    gitService = project == null ? null : GitService.openForProjectFolder(project.getFolder()).orElse(null);

    updatingCommitMessageField = true;
    commitMessageField.setText(project == null
        ? ""
        : settings.getLastCommitMessages().getOrDefault(project.getFolder().getAbsolutePath(), ""));
    updatingCommitMessageField = false;

    refresh();
  }

  @Override
  public void projectClosed(@NonNull ProjectClosedEvent event) {
    this.project = null;
    closeGitService();
    currentChangedFiles = List.of();
    changesTree.setRoot(null);
    updateActionButtons();
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    refresh();
  }

  private void closeGitService() {
    if (gitService != null) {
      gitService.close();
      gitService = null;
    }
  }

  // -------------------------------------------------------------------------
  // Refresh
  // -------------------------------------------------------------------------

  @FXML
  private void onRefresh() {
    refresh();
  }

  private void refresh() {
    if (gitService == null || project == null) {
      currentChangedFiles = List.of();
      changesTree.setRoot(null);
      updateActionButtons();
      return;
    }
    File projectFolder = project.getFolder();
    JFXFuture.supplyAsync(() -> {
          try {
            return gitService.getChangedProjectFiles(projectFolder);
          }
          catch (GitAPIException e) {
            throw new RuntimeException(e);
          }
        })
        .thenAcceptLater(this::populateTree)
        .onErrorLater(ex -> {
          log.error("Failed to refresh git status for '{}'", projectFolder, ex);
          WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_refresh_failed"), ex.getMessage());
        });
  }

  private void populateTree(List<GitChangedFile> changedFiles) {
    this.currentChangedFiles = changedFiles;
    TreeItem<VersioncontrolTreeNode> root = buildTree(changedFiles);
    changesTree.setRoot(root);
    expandAll(root);
    updateActionButtons();
  }

  private TreeItem<VersioncontrolTreeNode> buildTree(List<GitChangedFile> changedFiles) {
    String rootLabel = project != null ? project.getFolder().getName() : StudioBundle.get("versioncontrol");
    TreeItem<VersioncontrolTreeNode> root = new TreeItem<>(VersioncontrolTreeNode.folder("", rootLabel));
    Map<String, TreeItem<VersioncontrolTreeNode>> foldersByPath = new HashMap<>();
    foldersByPath.put("", root);

    List<GitChangedFile> sorted = new ArrayList<>(changedFiles);
    sorted.sort(Comparator.comparing(GitChangedFile::relativePath));

    for (GitChangedFile file : sorted) {
      String[] segments = file.relativePath().split("/");
      TreeItem<VersioncontrolTreeNode> parent = root;
      StringBuilder pathBuilder = new StringBuilder();
      for (int i = 0; i < segments.length - 1; i++) {
        if (!pathBuilder.isEmpty()) {
          pathBuilder.append('/');
        }
        pathBuilder.append(segments[i]);
        String folderPath = pathBuilder.toString();
        TreeItem<VersioncontrolTreeNode> folderItem = foldersByPath.get(folderPath);
        if (folderItem == null) {
          folderItem = new TreeItem<>(VersioncontrolTreeNode.folder(folderPath, segments[i]));
          parent.getChildren().add(folderItem);
          foldersByPath.put(folderPath, folderItem);
        }
        parent = folderItem;
      }
      parent.getChildren().add(new TreeItem<>(VersioncontrolTreeNode.leaf(file)));
    }
    return root;
  }

  private void expandAll(@NonNull TreeItem<VersioncontrolTreeNode> item) {
    item.setExpanded(true);
    for (TreeItem<VersioncontrolTreeNode> child : item.getChildren()) {
      expandAll(child);
    }
  }

  private void setCheckedRecursive(@NonNull TreeItem<VersioncontrolTreeNode> treeItem, boolean checked) {
    VersioncontrolTreeNode node = treeItem.getValue();
    if (node != null) {
      checkedByPath.computeIfAbsent(node.getRelativePath(), p -> new SimpleBooleanProperty(false)).set(checked);
    }
    for (TreeItem<VersioncontrolTreeNode> child : treeItem.getChildren()) {
      setCheckedRecursive(child, checked);
    }
  }

  // -------------------------------------------------------------------------
  // Commit / revert
  // -------------------------------------------------------------------------

  private List<GitChangedFile> getCheckedFiles() {
    List<GitChangedFile> result = new ArrayList<>();
    for (GitChangedFile file : currentChangedFiles) {
      SimpleBooleanProperty prop = checkedByPath.get(file.relativePath());
      if (prop != null && prop.get()) {
        result.add(file);
      }
    }
    return result;
  }

  private void updateActionButtons() {
    boolean anyChecked = !getCheckedFiles().isEmpty();
    String message = commitMessageField == null ? null : commitMessageField.getText();
    if (revertButton != null) {
      revertButton.setDisable(!anyChecked);
    }
    if (commitButton != null) {
      commitButton.setDisable(!anyChecked || message == null || message.isBlank());
    }
  }

  @FXML
  private void onRevert() {
    if (gitService == null) {
      return;
    }
    List<GitChangedFile> files = getCheckedFiles();
    if (files.isEmpty()) {
      return;
    }
    Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(),
        StudioBundle.get("confirm_revert_changes", files.size()), null, null, StudioBundle.get("versioncontrol_revert"));
    if (result.isPresent() && result.get() == ButtonType.OK) {
      JFXFuture.runAsync(() -> {
            try {
              gitService.revert(files);
            }
            catch (GitAPIException e) {
              throw new RuntimeException(e);
            }
          })
          .thenLater(this::refresh)
          .onErrorLater(ex -> {
            log.error("Failed to revert changes", ex);
            WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_revert_failed"), ex.getMessage());
          });
    }
  }

  @FXML
  private void onCommit() {
    if (gitService == null) {
      return;
    }
    List<GitChangedFile> files = getCheckedFiles();
    String message = commitMessageField.getText();
    if (files.isEmpty() || message == null || message.isBlank()) {
      return;
    }
    String trimmedMessage = message.trim();
    JFXFuture.runAsync(() -> {
          try {
            gitService.stageAndCommit(files, trimmedMessage);
          }
          catch (GitAPIException e) {
            throw new RuntimeException(e);
          }
        })
        .thenLater(this::refresh)
        .onErrorLater(ex -> {
          log.error("Failed to commit", ex);
          WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_commit_failed"), ex.getMessage());
        });
  }

  private Stage getStage() {
    return (Stage) changesTree.getScene().getWindow();
  }
}
