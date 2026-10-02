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
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;
import javafx.scene.control.SplitMenuButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

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
  private SplitMenuButton pullButton;

  @FXML
  private Button stashButton;

  @FXML
  private Button stashPopButton;

  @FXML
  private Tooltip stashPopTooltip;

  @FXML
  private Button collapseProjectViewButton;

  @FXML
  private TextArea commitMessageField;

  @FXML
  private Button commitButton;

  @FXML
  private VersioncontrolHistoryPanelController historyPanelController;

  /** The history panel's root (injected from the {@code fx:include}'s fx:id). */
  @FXML
  private Node historyPanel;

  @FXML
  private SplitPane historySplitPane;

  private static final int HISTORY_LIMIT = 200;
  private static final double HISTORY_DIVIDER_POSITION = 0.65;
  /** Keeps the outside-project tree's folder nodes apart from the project tree's in {@link #checkedByPath}. */
  private static final String OUTSIDE_FOLDER_KEY_PREFIX = "<repository>/";

  private final Map<String, SimpleBooleanProperty> checkedByPath = new HashMap<>();
  private final VersionControlSettings settings = VersionControlSettings.load();

  private Project project;
  private List<GitChangedFile> currentChangedFiles = List.of();
  private List<String> currentStashes = List.of();
  private GitBranchStatus currentBranchStatus;
  /** File whose history is shown; {@code null} shows the history of the whole project. */
  private File historyScope;
  private boolean updatingCommitMessageField = false;

  private Consumer<Boolean> historyVisibilityCallback;
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

  /** Notified whenever the history panel is shown or hidden (by {@link #setHistoryVisible} or its collapse button). */
  public void setHistoryVisibilityCallback(Consumer<Boolean> callback) {
    this.historyVisibilityCallback = callback;
  }

  public boolean isHistoryVisible() {
    return historySplitPane.getItems().contains(historyPanel);
  }

  /** Shows or hides the version history below the changes tree; hiding gives the tree the full height. */
  public void setHistoryVisible(boolean visible) {
    if (visible == isHistoryVisible()) {
      return;
    }
    if (visible) {
      historySplitPane.getItems().add(historyPanel);
      historySplitPane.setDividerPositions(HISTORY_DIVIDER_POSITION);
    }
    else {
      historySplitPane.getItems().remove(historyPanel);
    }
    if (historyVisibilityCallback != null) {
      historyVisibilityCallback.accept(visible);
    }
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
      private final ContextMenu fileMenu = createFileContextMenu(this::getItem);

      {
        nameLabel.getStyleClass().add("tree-cell-name-label");
        checkBox.setFocusTraversable(false);
        graphic.setAlignment(Pos.CENTER_LEFT);
        checkBox.selectedProperty().addListener((obs, oldVal, newVal) -> {
          if (isEmpty() || getTreeItem() == null) {
            return;
          }
          setCheckedRecursive(getTreeItem(), newVal);
          if (newVal && getTreeItem().getParent() == changesTree.getRoot()) {
            // Checking one of the two top-level nodes (project / rest of the repository) clears the other one.
            for (TreeItem<VersioncontrolTreeNode> otherRoot : changesTree.getRoot().getChildren()) {
              if (otherRoot != getTreeItem()) {
                setCheckedRecursive(otherRoot, false);
              }
            }
          }
          updateActionButtons();
        });
      }

      @Override
      protected void updateItem(VersioncontrolTreeNode node, boolean empty) {
        super.updateItem(node, empty);
        if (empty || node == null) {
          setGraphic(null);
          setContextMenu(null);
          return;
        }
        setContextMenu(node.isFolder() ? null : fileMenu);
        SimpleBooleanProperty prop = checkedByPath.computeIfAbsent(node.getRelativePath(), p -> new SimpleBooleanProperty(false));
        checkBox.selectedProperty().unbind();
        checkBox.setSelected(prop.get());
        prop.addListener((o, ov, nv) -> checkBox.setSelected(nv));

        nameLabel.setText(node.getDisplayName());
        nameLabel.getStyleClass().removeAll("git-new", "git-changed");
        if (!node.isFolder()) {
          ChangeStatus status = node.getChangedFile().status();
          if (status == ChangeStatus.NEW) {
            nameLabel.getStyleClass().add("git-new");
          }
          else if (status == ChangeStatus.MODIFIED) {
            nameLabel.getStyleClass().add("git-changed");
          }
        }
        if (node.isFolder()) {
          graphic.getChildren().setAll(checkBox,
              WidgetFactory.createIcon(getTreeItem().getParent() == changesTree.getRoot() ? Icons.FOLDER : Icons.FOLDER_OUTLINE),
              nameLabel);
        }
        else {
          graphic.getChildren().setAll(checkBox, nameLabel);
        }
        setGraphic(graphic);
      }
    });

    changesTree.setShowRoot(false);

    commitMessageField.textProperty().addListener((obs, oldVal, newVal) -> {
      updateActionButtons();
      if (updatingCommitMessageField || project == null) {
        return;
      }
      settings.getLastCommitMessages().put(project.getFolder().getAbsolutePath(), newVal);
      settings.save();
    });

    historyPanelController.setOnClearScope(this::onClearHistoryScope);
    historyPanelController.setOnCollapse(() -> setHistoryVisible(false));
    historyPanelController.setOnRestore((file, commit) -> {
      GitService gitService = Studio.getGitService();
      if (gitService != null) {
        VersionControlActions.restoreVersion(getStage(), gitService, file, commit);
      }
    });

    updateActionButtons();
    showBranchStatus(null);
    updateHistoryScopeUi();

    StudioEventManager.getInstance().addListener(this);
  }

  /** Commit / revert / history entries for a single changed file, shown on right-click in the changes tree. */
  private ContextMenu createFileContextMenu(Supplier<VersioncontrolTreeNode> nodeSupplier) {
    MenuItem commit = new MenuItem(StudioBundle.get("versioncontrol_tree.commit"));
    commit.setGraphic(WidgetFactory.createIcon(Icons.GIT_COMMIT));
    commit.setOnAction(e -> withSelectedFile(nodeSupplier, file -> {
      GitService gitService = Studio.getGitService();
      if (gitService != null && project != null) {
        VersionControlActions.commit(getStage(), project, gitService, file);
      }
    }));
    MenuItem revert = new MenuItem(StudioBundle.get("versioncontrol_tree.revert"));
    revert.setGraphic(WidgetFactory.createIcon(Icons.UNDO));
    revert.setOnAction(e -> withSelectedFile(nodeSupplier, file -> {
      GitService gitService = Studio.getGitService();
      if (gitService != null) {
        VersionControlActions.revert(getStage(), gitService, file);
      }
    }));
    MenuItem history = new MenuItem(StudioBundle.get("versioncontrol_tree.show_history"));
    history.setGraphic(WidgetFactory.createIcon(Icons.HISTORY));
    history.setOnAction(e -> withSelectedFile(nodeSupplier, file -> showFileHistory(file.file())));
    return new ContextMenu(commit, revert, new SeparatorMenuItem(), history);
  }

  private void withSelectedFile(Supplier<VersioncontrolTreeNode> nodeSupplier, Consumer<GitChangedFile> action) {
    VersioncontrolTreeNode node = nodeSupplier.get();
    if (node != null && !node.isFolder()) {
      action.accept(node.getChangedFile());
    }
  }

  private void showFileHistory(File file) {
    historyScope = file;
    updateHistoryScopeUi();
    refresh();
  }

  private void onClearHistoryScope() {
    historyScope = null;
    updateHistoryScopeUi();
    refresh();
  }

  private void updateHistoryScopeUi() {
    historyPanelController.setScope(historyScope);
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
    historyScope = null;
    updateHistoryScopeUi();
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
    historyScope = null;
    updateHistoryScopeUi();
    currentChangedFiles = List.of();
    currentStashes = List.of();
    historyPanelController.clear();
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
      currentStashes = List.of();
      historyPanelController.clear();
      setTreeRoot(null);
      showBranchStatus(null);
      updateActionButtons();
      return;
    }
    File projectFolder = project.getFolder();
    File scope = historyScope != null ? historyScope : projectFolder;
    JFXFuture.supplyAsync(() -> {
          try {
            return new RefreshResult(gitService.getChangedProjectFiles(projectFolder),
                gitService.getChangedFilesOutsideProject(projectFolder), gitService.getBranchStatus(),
                gitService.getStashes(), gitService.getHistory(scope, HISTORY_LIMIT));
          }
          catch (GitAPIException | IOException e) {
            throw new RuntimeException(e);
          }
        })
        .thenAcceptLater(result -> {
          currentStashes = result.stashes();
          historyPanelController.setItems(result.history());
          showBranchStatus(result.branchStatus());
          populateTree(result.changedFiles(), result.outsideFiles());
        })
        .onErrorLater(ex -> {
          log.error("Failed to refresh git status for '{}'", projectFolder, ex);
          WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_refresh_failed"), ex.getMessage());
        });
  }

  private record RefreshResult(List<GitChangedFile> changedFiles, List<GitChangedFile> outsideFiles,
                               GitBranchStatus branchStatus, List<String> stashes, List<GitCommitInfo> history) {
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

  private void populateTree(List<GitChangedFile> changedFiles, List<GitChangedFile> outsideFiles) {
    List<GitChangedFile> all = new ArrayList<>(changedFiles);
    all.addAll(outsideFiles);
    this.currentChangedFiles = all;
    if (all.isEmpty()) {
      // A tree consisting only of folders, with no actual changed files, should show the
      // "no changes" placeholder instead of an empty tree.
      setTreeRoot(null);
    }
    else {
      // The two top-level nodes (project / rest of the repository) hang off an invisible root.
      TreeItem<VersioncontrolTreeNode> hiddenRoot = new TreeItem<>();
      if (!changedFiles.isEmpty()) {
        String rootLabel = project != null ? project.getFolder().getName() : StudioBundle.get("versioncontrol");
        hiddenRoot.getChildren().add(buildTree(rootLabel, "", changedFiles, GitChangedFile::relativePath));
      }
      if (!outsideFiles.isEmpty()) {
        GitService gitService = Studio.getGitService();
        Function<GitChangedFile, String> repoPath = file -> gitService.repositoryRelativePath(file.file());
        hiddenRoot.getChildren().add(buildTree(StudioBundle.get("versioncontrol_outside_project"), OUTSIDE_FOLDER_KEY_PREFIX,
            outsideFiles, repoPath));
      }
      setExpandedRecursive(hiddenRoot, true);
      setTreeRoot(hiddenRoot);
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

  /**
   * @param folderKeyPrefix prefix for the folder nodes' keys in {@link #checkedByPath}, keeping the
   *                        project's and the outside-project tree's folders apart
   * @param treePath        the path (forward-slash separated) that decides where a file sits in the tree
   */
  private TreeItem<VersioncontrolTreeNode> buildTree(String rootLabel, String folderKeyPrefix, List<GitChangedFile> changedFiles,
                                                     Function<GitChangedFile, String> treePath) {
    TreeItem<VersioncontrolTreeNode> root = new TreeItem<>(VersioncontrolTreeNode.folder(folderKeyPrefix, rootLabel));
    Map<String, TreeItem<VersioncontrolTreeNode>> foldersByPath = new HashMap<>();
    foldersByPath.put("", root);

    List<GitChangedFile> sorted = new ArrayList<>(changedFiles);
    sorted.sort(Comparator.comparing(treePath));

    for (GitChangedFile file : sorted) {
      String[] segments = treePath.apply(file).split("/");
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
          folderItem = new TreeItem<>(VersioncontrolTreeNode.folder(folderKeyPrefix + folderPath, segments[i]));
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
    if (pullButton != null) {
      pullButton.setDisable(currentBranchStatus == null || currentBranchStatus.detached() || !currentBranchStatus.hasRemote());
    }
    if (stashButton != null) {
      stashButton.setDisable(project == null || currentChangedFiles.isEmpty());
    }
    if (stashPopButton != null) {
      stashPopButton.setDisable(currentStashes.isEmpty());
      stashPopTooltip.setText(currentStashes.isEmpty()
          ? StudioBundle.get("versioncontrol_stash_pop")
          : StudioBundle.get("versioncontrol_stash_pop_tooltip", currentStashes.size(), currentStashes.get(0)));
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
      onFilesChangedOnDisk();
    }
    else if (!result.isCancelled()) {
      log.error("Failed to revert changes");
    }
  }

  /**
   * A revert, pull, stash or stash pop isn't limited to the file(s) the user had in mind - the
   * on-disk state it leaves can differ from what any single file's status suggested - so rather
   * than patching up individual project items, {@link #projectRefreshCallback} (wired to {@link
   * de.a12.studio.ui.RootController#reloadProject()}) reloads the whole project from disk and
   * rebuilds every open tab/detached window's editor content from it, closing any tab whose file
   * the operation deleted. The tail {@link StudioEventManager#fireGitStatusChangedEvent} refreshes
   * this panel itself (via {@link #gitStatusChanged}) as well as any open editor's Commit/Revert
   * toolbar buttons.
   */
  private void onFilesChangedOnDisk() {
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

  // -------------------------------------------------------------------------
  // Pull
  // -------------------------------------------------------------------------

  /** The button's main action follows the repository's {@code pull.rebase} setting; the menu offers both explicitly. */
  @FXML
  private void onPull() {
    GitService gitService = Studio.getGitService();
    if (gitService != null) {
      pull(gitService, gitService.prefersRebase());
    }
  }

  @FXML
  private void onPullRebase() {
    GitService gitService = Studio.getGitService();
    if (gitService != null) {
      pull(gitService, true);
    }
  }

  @FXML
  private void onPullMerge() {
    GitService gitService = Studio.getGitService();
    if (gitService != null) {
      pull(gitService, false);
    }
  }

  private void pull(GitService gitService, boolean rebase) {
    if (currentBranchStatus == null || currentBranchStatus.detached() || !currentBranchStatus.hasRemote()) {
      return;
    }
    try {
      if (!gitService.canPull()) {
        WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_pull_no_upstream", currentBranchStatus.branch()));
        return;
      }
      // A rebase refuses to start on a dirty working tree; offer to carry the changes across it instead of failing.
      boolean autostash = false;
      if (rebase && gitService.hasUncommittedChanges()) {
        Optional<ButtonType> confirmation = WidgetFactory.showConfirmation(getStage(),
            StudioBundle.get("versioncontrol_pull_autostash"), StudioBundle.get("versioncontrol_pull_autostash_help"), null,
            StudioBundle.get("versioncontrol_pull_autostash_ok"));
        if (confirmation.isEmpty() || confirmation.get() != ButtonType.OK) {
          return;
        }
        autostash = true;
      }
      if (!ensureIdentity(gitService)) {
        return;
      }
      boolean stashFirst = autostash;
      GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_pulling"),
          () -> gitService.pull(rebase, stashFirst));
      ProgressResultModel result = ProgressDialog.createProgressDialog(getStage(), progressModel);
      if (!result.isSuccess() && !result.isCancelled()) {
        log.error("Failed to pull");
        offerAbortIfUnfinished(gitService);
      }
      // Even a failed pull may have changed files (conflict markers, a partly applied rebase).
      onFilesChangedOnDisk();
    }
    catch (GitAPIException | IOException e) {
      log.error("Failed to prepare pull", e);
      WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_git_failed", "pull", e.getMessage()));
    }
  }

  /** A pull that hit conflicts stays unfinished; let the user resolve them or go back to where they started. */
  private void offerAbortIfUnfinished(GitService gitService) {
    if (!gitService.isOperationInProgress()) {
      return;
    }
    Optional<ButtonType> choice = WidgetFactory.showAlertOption(getStage(), StudioBundle.get("versioncontrol_pull_conflicts"),
        StudioBundle.get("versioncontrol_pull_abort"), StudioBundle.get("versioncontrol_pull_resolve"),
        StudioBundle.get("versioncontrol_pull_conflicts_help"), null);
    if (choice.isPresent() && choice.get() == ButtonType.APPLY) {
      try {
        gitService.abortOperation();
      }
      catch (GitAPIException e) {
        log.error("Failed to abort unfinished pull", e);
        WidgetFactory.showAlert(getStage(), e.getMessage());
      }
    }
  }

  // -------------------------------------------------------------------------
  // Stash
  // -------------------------------------------------------------------------

  @FXML
  private void onStash() {
    GitService gitService = Studio.getGitService();
    if (gitService == null || currentChangedFiles.isEmpty() || !ensureIdentity(gitService)) {
      return;
    }
    String message = WidgetFactory.showInputDialog(getStage(), StudioBundle.get("versioncontrol_stash"),
        StudioBundle.get("versioncontrol_stash_message_title"), StudioBundle.get("versioncontrol_stash_description"),
        StudioBundle.get("versioncontrol_stash_message_help"), "");
    if (message == null) {
      return;
    }
    GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_stashing"),
        () -> gitService.stash(message));
    ProgressResultModel result = ProgressDialog.createProgressDialog(getStage(), progressModel);
    if (!result.isSuccess() && !result.isCancelled()) {
      log.error("Failed to stash");
    }
    onFilesChangedOnDisk();
  }

  @FXML
  private void onStashPop() {
    GitService gitService = Studio.getGitService();
    if (gitService == null || currentStashes.isEmpty() || !ensureIdentity(gitService)) {
      return;
    }
    GitOperationProgressModel progressModel = new GitOperationProgressModel(StudioBundle.get("versioncontrol_stash_popping"),
        gitService::stashPop);
    ProgressResultModel result = ProgressDialog.createProgressDialog(getStage(), progressModel);
    if (!result.isSuccess() && !result.isCancelled()) {
      log.error("Failed to pop stash");
    }
    onFilesChangedOnDisk();
  }

  // -------------------------------------------------------------------------
  // Git settings
  // -------------------------------------------------------------------------

  /**
   * Merging, rebasing and stashing create commits, which git refuses to do without {@code user.name}/{@code user.email}.
   * If either is missing, asks for them - for this repository only, or for every repository - and writes them.
   *
   * @return whether an identity is configured now
   */
  private boolean ensureIdentity(GitService gitService) {
    if (gitService.hasIdentity()) {
      return true;
    }
    ConfirmationResult confirmation = WidgetFactory.showConfirmationWithCheckbox(getStage(),
        StudioBundle.get("versioncontrol_identity_missing"), StudioBundle.get("versioncontrol_identity_configure"),
        StudioBundle.get("versioncontrol_identity_help"), null, StudioBundle.get("versioncontrol_identity_global"), false);
    if (!confirmation.isOkClicked()) {
      return false;
    }
    String name = WidgetFactory.showInputDialog(getStage(), StudioBundle.get("versioncontrol_identity_title"),
        StudioBundle.get("versioncontrol_identity_name"), StudioBundle.get("versioncontrol_identity_name_description"), null,
        System.getProperty("user.name", ""));
    if (name == null || name.isBlank()) {
      return false;
    }
    String email = WidgetFactory.showInputDialog(getStage(), StudioBundle.get("versioncontrol_identity_title"),
        StudioBundle.get("versioncontrol_identity_email"), StudioBundle.get("versioncontrol_identity_email_description"), null, "");
    if (email == null || email.isBlank()) {
      return false;
    }
    try {
      gitService.setIdentity(name.trim(), email.trim(), confirmation.isChecked());
      return true;
    }
    catch (GitAPIException e) {
      log.error("Failed to store git identity", e);
      WidgetFactory.showAlert(getStage(), e.getMessage());
      return false;
    }
  }

  private Stage getStage() {
    return (Stage) changesTree.getScene().getWindow();
  }
}
