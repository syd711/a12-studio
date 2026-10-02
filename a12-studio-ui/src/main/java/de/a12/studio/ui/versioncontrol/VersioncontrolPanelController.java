package de.a12.studio.ui.versioncontrol;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.settings.VersionControlSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.components.ProgressDialog;
import de.a12.studio.ui.components.ProgressResultModel;
import de.a12.studio.ui.events.GitStatusChangedEvent;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.ProjectClosedEvent;
import de.a12.studio.ui.events.ProjectOpenedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.util.ConfirmationResult;
import de.a12.studio.ui.util.Icons;
import de.a12.studio.ui.util.JFXFuture;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.WidgetFactory;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.io.IOException;
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
  private TreeView<VersioncontrolTreeNode> changesTree;

  @FXML
  private Label noChangesLabel;

  @FXML
  private HBox branchBar;

  @FXML
  private Label branchLabel;

  @FXML
  private Label unpushedLabel;

  @FXML
  private Button refreshButton;

  @FXML
  private Button revertButton;

  @FXML
  private Button pushButton;

  @FXML
  private Button collapseProjectViewButton;

  @FXML
  private TextArea commitMessageField;

  @FXML
  private Button commitButton;

  private final Map<String, SimpleBooleanProperty> checkedByPath = new HashMap<>();
  private final VersionControlSettings settings = VersionControlSettings.load();

  private Project project;
  private List<GitChangedFile> currentChangedFiles = List.of();
  private GitBranchStatus currentBranchStatus;
  private boolean updatingCommitMessageField = false;

  private Runnable collapseProjectViewCallback;
  private Runnable projectRefreshCallback;

  public void setCollapseProjectViewCallback(Runnable callback) {
    this.collapseProjectViewCallback = callback;
  }

  /**
   * Wired by {@link de.a12.studio.ui.RootController} to its own {@link
   * de.a12.studio.ui.RootController#reloadProject()} - called after a successful {@link
   * #onRevert()} so the project tree and every open editor pick up whatever files the revert
   * added, removed, or changed on disk, the same way the project tree already does after a
   * delete/rename/move.
   */
  public void setProjectRefreshCallback(Runnable callback) {
    this.projectRefreshCallback = callback;
  }

  @FXML
  private void onCollapseProjectView() {
    if (collapseProjectViewCallback != null) {
      collapseProjectViewCallback.run();
    }
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    changesTree.setCellFactory(view -> new TreeCell<>() {
      private final CheckBox checkBox = new CheckBox();
      private final Label nameLabel = new Label();
      private final HBox graphic = new HBox(4, checkBox, nameLabel);

      {
        nameLabel.getStyleClass().add("tree-cell-name-label");
        checkBox.setFocusTraversable(false);
        graphic.setAlignment(Pos.CENTER_LEFT);
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
          if (isEmpty() || getTreeItem() == null) {
            return;
          }
          setCheckedRecursive(getTreeItem(), newVal);
          updateActionButtons();
        });
      }

      @Override
      protected void updateItem(VersioncontrolTreeNode node, boolean empty) {
        super.updateItem(node, empty);
        if (empty || node == null) {
          setGraphic(null);
          return;
        }
        SimpleBooleanProperty prop = checkedByPath.computeIfAbsent(node.getRelativePath(), p -> new SimpleBooleanProperty(false));
        checkBox.selectedProperty().unbind();
        checkBox.setSelected(prop.get());
        prop.addListener((o, ov, nv) -> checkBox.setSelected(nv));

        nameLabel.setText(node.getDisplayName());
        graphic.getChildren().setAll(checkBox,
            node.isFolder() ? WidgetFactory.createIcon(Icons.FOLDER_OUTLINE) : statusIcon(node.getChangedFile().status()),
            nameLabel);
        setGraphic(graphic);
      }
    });

    changesTree.setShowRoot(true);

    commitMessageField.textProperty().addListener((obs, oldVal, newVal) -> {
      updateActionButtons();
      if (updatingCommitMessageField || project == null) {
        return;
      }
      settings.getLastCommitMessages().put(project.getFolder().getAbsolutePath(), newVal);
      settings.save();
    });


    updateActionButtons();
    showBranchStatus(null);

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
    currentChangedFiles = List.of();
    setTreeRoot(null);
    showBranchStatus(null);
    updateActionButtons();
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    refresh();
  }

  @Override
  public void gitStatusChanged(@NonNull GitStatusChangedEvent event) {
    refresh();
  }

  // -------------------------------------------------------------------------
  // Refresh
  // -------------------------------------------------------------------------

  @FXML
  private void onRefresh() {
    refresh();
  }

  private void refresh() {
    GitService gitService = Studio.getGitService();
    if (gitService == null || project == null) {
      currentChangedFiles = List.of();
      setTreeRoot(null);
      showBranchStatus(null);
      updateActionButtons();
      return;
    }
    File projectFolder = project.getFolder();
    JFXFuture.supplyAsync(() -> {
          try {
            return new RefreshResult(gitService.getChangedProjectFiles(projectFolder), gitService.getBranchStatus());
          }
          catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
          }
        })
        .thenAcceptLater(result -> {
          showBranchStatus(result.branchStatus());
          populateTree(result.changedFiles());
        })
        .onErrorLater(ex -> {
          log.error("Failed to refresh git status for '{}'", projectFolder, ex);
          WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_refresh_failed"), ex.getMessage());
        });
  }

  private record RefreshResult(List<GitChangedFile> changedFiles, GitBranchStatus branchStatus) {
  }

  /** Updates the branch bar above the changes tree; {@code null} hides it (no project/repository). */
  private void showBranchStatus(GitBranchStatus status) {
    currentBranchStatus = status;
    updateActionButtons();
    branchBar.setVisible(status != null);
    branchBar.setManaged(status != null);
    if (status == null) {
      return;
    }
    branchLabel.setText(status.detached()
        ? StudioBundle.get("versioncontrol_detached_head", status.branch())
        : status.branch());
    branchLabel.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("versioncontrol_current_branch", branchLabel.getText())));

    Integer ahead = status.aheadCount();
    if (ahead == null) {
      unpushedLabel.setText(status.detached() ? "" : StudioBundle.get("versioncontrol_no_upstream"));
      unpushedLabel.setGraphic(null);
      unpushedLabel.setTooltip(status.detached() ? null : WidgetFactory.createTooltip(StudioBundle.get("versioncontrol_no_upstream_tooltip")));
    }
    else {
      unpushedLabel.setText(String.valueOf(ahead));
      unpushedLabel.setGraphic(WidgetFactory.createIcon(Icons.ARROW_UP));
      unpushedLabel.setTooltip(WidgetFactory.createTooltip(StudioBundle.get("versioncontrol_unpushed_commits", ahead)));
    }
    unpushedLabel.getStyleClass().remove("versioncontrol-unpushed-pending");
    if (ahead != null && ahead > 0) {
      unpushedLabel.getStyleClass().add("versioncontrol-unpushed-pending");
    }
  }

  private void populateTree(List<GitChangedFile> changedFiles) {
    this.currentChangedFiles = changedFiles;
    if (changedFiles.isEmpty()) {
      // buildTree() would otherwise still produce a root folder node with no children - a tree
      // consisting only of folders, with no actual changed files - which should show the
      // "no changes" placeholder instead of an empty tree.
      setTreeRoot(null);
    }
    else {
      TreeItem<VersioncontrolTreeNode> root = buildTree(changedFiles);
      setExpandedRecursive(root, true);
      setTreeRoot(root);
    }
    updateActionButtons();
  }

  @FXML
  private void onExpandAll() {
    setExpandedRecursive(changesTree.getRoot(), true);
  }

  @FXML
  private void onCollapseAll() {
    setExpandedRecursive(changesTree.getRoot(), false);
  }

  /**
   * Sets the changes tree's root and toggles {@link #noChangesLabel} to fill in for
   * {@link TreeView}'s lack of a built-in "placeholder" property (unlike TableView/TreeTableView).
   */
  private void setTreeRoot(TreeItem<VersioncontrolTreeNode> root) {
    changesTree.setRoot(root);
    noChangesLabel.setVisible(root == null);
    noChangesLabel.setManaged(root == null);
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

  private void setExpandedRecursive(TreeItem<VersioncontrolTreeNode> item, boolean expanded) {
    if (item == null) {
      return;
    }
    item.setExpanded(expanded);
    for (TreeItem<VersioncontrolTreeNode> child : item.getChildren()) {
      setExpandedRecursive(child, expanded);
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
    if (pushButton != null) {
      pushButton.setDisable(currentBranchStatus == null || !currentBranchStatus.canPush(true));
    }
  }

  @FXML
  private void onRevert() {
    GitService gitService = Studio.getGitService();
    if (gitService == null) {
      return;
    }
    List<GitChangedFile> files = getCheckedFiles();
    if (files.isEmpty()) {
      return;
    }
    Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(getStage(),
        StudioBundle.get("confirm_revert_changes", files.size()), null, null, StudioBundle.get("versioncontrol_revert"));
    if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
      return;
    }
    GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_reverting"),
        () -> gitService.revert(files));
    ProgressResultModel result = ProgressDialog.createProgressDialog(getStage(), progressModel);
    if (result.isSuccess()) {
      onRevertCompleted();
    }
    else if (!result.isCancelled()) {
      log.error("Failed to revert changes");
    }
  }

  /**
   * A revert isn't limited to the file(s) actually checked - the on-disk state it restores can
   * differ from what any single file's status suggested - so rather than patching up just the
   * reverted files' project items, {@link #projectRefreshCallback} (wired to {@link
   * de.a12.studio.ui.RootController#reloadProject()}) reloads the whole project from disk and
   * rebuilds every open tab/detached window's editor content from it, closing any tab whose file
   * the revert deleted. The tail {@link StudioEventManager#fireGitStatusChangedEvent} refreshes
   * this panel itself (via {@link #gitStatusChanged}) as well as any open editor's Commit/Revert
   * toolbar buttons.
   */
  private void onRevertCompleted() {
    if (projectRefreshCallback != null) {
      projectRefreshCallback.run();
    }
    StudioEventManager.getInstance().fireGitStatusChangedEvent();
  }

  @FXML
  private void onCommit() {
    GitService gitService = Studio.getGitService();
    if (gitService == null) {
      return;
    }
    List<GitChangedFile> files = getCheckedFiles();
    String message = commitMessageField.getText();
    if (files.isEmpty() || message == null || message.isBlank()) {
      return;
    }
    String trimmedMessage = message.trim();
    GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_committing"),
        () -> gitService.stageAndCommit(files, trimmedMessage));
    ProgressResultModel result = ProgressDialog.createProgressDialog(getStage(), progressModel);
    if (result.isSuccess()) {
      StudioEventManager.getInstance().fireGitStatusChangedEvent();
    }
    else if (!result.isCancelled()) {
      log.error("Failed to commit");
    }
  }

  @FXML
  private void onPush() {
    GitService gitService = Studio.getGitService();
    if (gitService == null || currentBranchStatus == null) {
      return;
    }
    if (!currentBranchStatus.canPush(true)) {
      return;
    }
    // Force is a one-off decision made per push; pre-checked only when a normal push has nothing to send
    // (e.g. after a local reset), where a force push is the only way to proceed.
    ConfirmationResult confirmation = WidgetFactory.showConfirmationWithCheckbox(getStage(),
        StudioBundle.get("confirm_push", currentBranchStatus.branch()), StudioBundle.get("versioncontrol_push"),
        StudioBundle.get("versioncontrol_force_push_tooltip"), null, StudioBundle.get("versioncontrol_force_push"),
        !currentBranchStatus.canPush(false));
    if (!confirmation.isOkClicked()) {
      return;
    }
    boolean force = confirmation.isChecked();
    if (!currentBranchStatus.canPush(force)) {
      WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_nothing_to_push"), null, null);
      return;
    }
    GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_pushing"),
        () -> gitService.push(force));
    ProgressResultModel result = ProgressDialog.createProgressDialog(getStage(), progressModel);
    if (!result.isSuccess() && !result.isCancelled()) {
      log.error("Failed to push");
    }
    // Ahead/behind counts change on success, and a rejected push may still have updated nothing - refresh either way.
    StudioEventManager.getInstance().fireGitStatusChangedEvent();
  }

  private Stage getStage() {
    return (Stage) changesTree.getScene().getWindow();
  }
}
