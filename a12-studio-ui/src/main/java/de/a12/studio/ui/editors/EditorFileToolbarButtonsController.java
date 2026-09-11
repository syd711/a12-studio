package de.a12.studio.ui.editors;

import de.a12.studio.models.projects.Project;
import de.a12.studio.models.projects.ProjectItem;
import de.a12.studio.models.projects.settings.VersionControlSettings;
import de.a12.studio.ui.Studio;
import de.a12.studio.ui.bookmarks.BookmarkService;
import de.a12.studio.ui.events.BookmarksChangedEvent;
import de.a12.studio.ui.events.GitStatusChangedEvent;
import de.a12.studio.ui.events.ModelSaveEvent;
import de.a12.studio.ui.events.SettingsChangedEvent;
import de.a12.studio.ui.events.StudioEventListener;
import de.a12.studio.ui.events.StudioEventManager;
import de.a12.studio.ui.events.TabSelectionChangedEvent;
import de.a12.studio.ui.previewapp.PreviewAppDeployer;
import de.a12.studio.ui.util.JFXFuture;
import de.a12.studio.ui.util.StudioBundle;
import de.a12.studio.ui.util.SystemUtil;
import de.a12.studio.ui.util.WidgetFactory;
import de.a12.studio.ui.versioncontrol.ChangeStatus;
import de.a12.studio.ui.versioncontrol.GitChangedFile;
import de.a12.studio.ui.versioncontrol.GitService;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.Separator;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jspecify.annotations.NonNull;
import org.kordamp.ikonli.javafx.FontIcon;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.function.Supplier;

/**
 * Controller for the reusable "Edit File" / "Open Model Folder" / "Bookmark" toolbar buttons,
 * included via {@code fx:include} in every editor toolbar.
 *
 * <p>After loading, call {@link #setFileSupplier(Supplier)} so the component knows
 * which file to act on — typically {@code () -> projectItem.getFile()}.
 */
@Slf4j
public class EditorFileToolbarButtonsController implements Initializable, StudioEventListener {

  @FXML
  private ToggleButton bookmarkButton;

  @FXML
  private Separator versioncontrolSeparator;

  @FXML
  private Button commitButton;

  @FXML
  private Button revertButton;

  @FXML
  private Separator deploySeparator;

  @FXML
  private StackPane deployModelContainer;

  @FXML
  private Button deployModelBtn;

  @FXML
  private Tooltip deployModelBtnTooltip;

  @FXML
  private ProgressIndicator deployModelSpinner;

  private Supplier<File> fileSupplier;
  private Supplier<ProjectItem> projectItemSupplier;

  /**
   * Whether {@code projectItemSupplier}'s model has been saved since it was last deployed via
   * {@link #onDeployModel}, i.e. whether the Deploy button has something new to upload. Reset to
   * {@code true} on every {@link #modelSaved} for this item, and back to {@code false} once a
   * deploy started from this button finishes.
   */
  private boolean hasPendingChanges;

  /**
   * The current projectItem's outstanding git change, as last computed by {@link
   * #updateVersioncontrolButtons()}; {@code null} if it has none (or Version Control isn't
   * available). Backs both the Commit/Revert buttons' enabled state and what {@link #onCommit}/
   * {@link #onRevert} act on.
   */
  private GitChangedFile currentChangedFile;

  /**
   * Provide the file this component should open/edit.
   * Call this after the owning controller's {@code projectItem} is available.
   */
  public void setFileSupplier(Supplier<File> fileSupplier) {
    this.fileSupplier = fileSupplier;
  }

  /**
   * Provide the current ProjectItem so the bookmark button can reflect and toggle bookmark state,
   * and the Deploy button can reflect this model's pending-changes/exclusion state.
   */
  public void setProjectItemSupplier(Supplier<ProjectItem> projectItemSupplier) {
    this.projectItemSupplier = projectItemSupplier;
    updateBookmarkButton();
    updateDeployButton();
    updateVersioncontrolButtons();
  }

  @Override
  public void initialize(URL url, ResourceBundle resourceBundle) {
    StudioEventManager.getInstance().addListener(this);
  }

  @Override
  public void bookmarksChanged(@NonNull BookmarksChangedEvent event) {
    updateBookmarkButton();
  }

  @Override
  public void tabSelectionChanged(@NonNull TabSelectionChangedEvent event) {
    updateBookmarkButton();
    updateDeployButton();
    updateVersioncontrolButtons();
  }

  @Override
  public void modelSaved(@NonNull ModelSaveEvent event) {
    ProjectItem item = projectItemSupplier != null ? projectItemSupplier.get() : null;
    if (item != null && event.getItem().equals(item)) {
      hasPendingChanges = true;
      updateDeployButton();
      updateVersioncontrolButtons();
    }
  }

  @Override
  public void settingsChanged(@NonNull SettingsChangedEvent event) {
    // The deployment exclusion list lives in project settings - re-check it live rather than only
    // when the tab is reselected. Version Control support can likewise be toggled in Preferences
    // at any time.
    updateDeployButton();
    updateVersioncontrolButtons();
  }

  /**
   * Fired after a commit or revert completes anywhere - the Versioncontrol panel, or this same
   * button on another open tab - so this toolbar's own Commit/Revert state stays in sync even
   * when the change wasn't made from here.
   */
  @Override
  public void gitStatusChanged(@NonNull GitStatusChangedEvent event) {
    updateVersioncontrolButtons();
  }

  private void updateBookmarkButton() {
    if (bookmarkButton == null) return;
    ProjectItem item = projectItemSupplier != null ? projectItemSupplier.get() : null;
    boolean isBookmarked = item != null && BookmarkService.getInstance().isBookmarked(item);
    bookmarkButton.setSelected(isBookmarked);
    // Swap icon to filled/outline depending on state
    FontIcon icon = (FontIcon) bookmarkButton.getGraphic();
    if (icon != null) {
      icon.setIconLiteral(isBookmarked ? "mdi2b-bookmark" : "mdi2b-bookmark-outline");
    }
  }

  @FXML
  private void onToggleBookmark(ActionEvent e) {
    if (projectItemSupplier == null) return;
    ProjectItem item = projectItemSupplier.get();
    if (item != null) {
      BookmarkService.getInstance().toggle(item);
      // bookmarksChanged event fires via BookmarkService → updateBookmarkButton called
    }
  }

  @FXML
  private void onFileEdit(ActionEvent e) {
    if (fileSupplier != null) {
      SystemUtil.editFile(fileSupplier.get());
    }
  }

  @FXML
  private void onFileOpen(ActionEvent e) {
    if (fileSupplier != null) {
      SystemUtil.openFile(fileSupplier.get());
    }
  }

  @FXML
  private void onDeployModel(ActionEvent e) {
    if (projectItemSupplier == null) {
      return;
    }
    ProjectItem item = projectItemSupplier.get();
    Project project = Studio.getCurrentProject();
    if (item == null || project == null) {
      return;
    }
    deployModelBtn.setDisable(true);
    deployModelSpinner.setVisible(true);
    PreviewAppDeployer.deploySingle(project, item, () -> {
      deployModelSpinner.setVisible(false);
      hasPendingChanges = false;
      updateDeployButton();
    });
  }

  private void updateDeployButton() {
    if (deployModelBtn == null) {
      return;
    }
    ProjectItem item = projectItemSupplier != null ? projectItemSupplier.get() : null;
    Project project = Studio.getCurrentProject();
    boolean excluded = item != null && project != null && PreviewAppDeployer.isDeploymentExcluded(item, project);

    boolean previewEnabled = project != null && project.getSettings().getProjectRootSettings().getPreviewApp().isEnabled();
    if (deployModelContainer != null) {
      deployModelContainer.setVisible(previewEnabled);
      deployModelContainer.setManaged(previewEnabled);
    }
    if (deploySeparator != null) {
      deploySeparator.setVisible(previewEnabled);
      deploySeparator.setManaged(previewEnabled);
    }

    if (deployModelBtnTooltip != null) {
      deployModelBtnTooltip.setText(excluded
          ? StudioBundle.get("deploy_model_excluded_tooltip")
          : StudioBundle.get("deploy_model"));
    }
    deployModelBtn.setDisable(item == null || excluded || !hasPendingChanges);
  }

  // -------------------------------------------------------------------------
  // Commit / revert
  // -------------------------------------------------------------------------

  /**
   * Shows/hides the Commit/Revert buttons (and their separator) depending on whether Version
   * Control is available for the current project at all, then - if it is - asynchronously checks
   * whether {@code projectItemSupplier}'s file has an outstanding git change and enables/disables
   * the buttons accordingly. Mirrors {@link GitService#isGitRepository}/{@link
   * VersionControlSettings#isEnabled} being used the same way to gate the Versioncontrol
   * activity-bar toggle in {@code RootController}.
   */
  private void updateVersioncontrolButtons() {
    if (commitButton == null || revertButton == null) {
      return;
    }
    ProjectItem item = projectItemSupplier != null ? projectItemSupplier.get() : null;
    Project project = Studio.getCurrentProject();
    GitService gitService = Studio.getGitService();
    boolean available = item != null && project != null && gitService != null && VersionControlSettings.load().isEnabled();

    if (versioncontrolSeparator != null) {
      versioncontrolSeparator.setVisible(available);
      versioncontrolSeparator.setManaged(available);
    }
    commitButton.setVisible(available);
    commitButton.setManaged(available);
    revertButton.setVisible(available);
    revertButton.setManaged(available);

    if (!available) {
      currentChangedFile = null;
      commitButton.setDisable(true);
      revertButton.setDisable(true);
      return;
    }

    File file = item.getFile();
    File projectFolder = project.getFolder();
    JFXFuture.supplyAsync(() -> {
          try {
            return gitService.getChangedFile(projectFolder, file);
          }
          catch (GitAPIException ex) {
            throw new RuntimeException(ex);
          }
        })
        .thenAcceptLater(changedFile -> {
          currentChangedFile = changedFile.orElse(null);
          commitButton.setDisable(currentChangedFile == null);
          revertButton.setDisable(currentChangedFile == null);
        })
        .onErrorLater(ex -> log.error("Failed to check git status for '{}'", file, ex));
  }

  @FXML
  private void onCommit(ActionEvent e) {
    GitChangedFile file = currentChangedFile;
    GitService gitService = Studio.getGitService();
    Project project = Studio.getCurrentProject();
    if (file == null || gitService == null || project == null) {
      return;
    }

    VersionControlSettings settings = VersionControlSettings.load();
    String defaultMessage = settings.getLastCommitMessages().getOrDefault(project.getFolder().getAbsolutePath(), "");
    String message = WidgetFactory.showTextAreaInputDialog(getStage(), StudioBundle.get("versioncontrol_commit"),
        StudioBundle.get("versioncontrol_commit_message_prompt"), StudioBundle.get("commit_file_description", file.relativePath()),
        defaultMessage);
    if (message == null || message.isBlank()) {
      return;
    }
    String trimmedMessage = message.trim();
    settings.getLastCommitMessages().put(project.getFolder().getAbsolutePath(), trimmedMessage);
    settings.save();

    JFXFuture.runAsync(() -> {
          try {
            gitService.stageAndCommit(List.of(file), trimmedMessage);
          }
          catch (GitAPIException ex) {
            throw new RuntimeException(ex);
          }
        })
        .thenLater(() -> StudioEventManager.getInstance().fireGitStatusChangedEvent())
        .onErrorLater(ex -> {
          log.error("Failed to commit '{}'", file.file(), ex);
          WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_commit_failed"), ex.getMessage());
        });
  }

  @FXML
  private void onRevert(ActionEvent e) {
    GitChangedFile file = currentChangedFile;
    GitService gitService = Studio.getGitService();
    ProjectItem item = projectItemSupplier != null ? projectItemSupplier.get() : null;
    if (file == null || gitService == null || item == null) {
      return;
    }

    Optional<ButtonType> result = WidgetFactory.showConfirmation(getStage(),
        StudioBundle.get("confirm_revert_file", file.relativePath()), null, null, StudioBundle.get("versioncontrol_revert"));
    if (result.isEmpty() || result.get() != ButtonType.OK) {
      return;
    }

    JFXFuture.runAsync(() -> {
          try {
            gitService.revert(List.of(file));
          }
          catch (GitAPIException ex) {
            throw new RuntimeException(ex);
          }
        })
        .thenLater(() -> onRevertCompleted(item, file))
        .onErrorLater(ex -> {
          log.error("Failed to revert '{}'", file.file(), ex);
          WidgetFactory.showAlert(getStage(), StudioBundle.get("versioncontrol_revert_failed"), ex.getMessage());
        });
  }

  /**
   * Mirrors {@link de.a12.studio.ui.versioncontrol.VersioncontrolPanelController}'s own
   * post-revert handling: a {@link ChangeStatus#NEW} file was deleted by the revert, so it's
   * treated like any other deletion (closes this tab); every other file was checked out from
   * HEAD, so {@link ProjectItem#reload()} picks up its new content and {@link
   * StudioEventManager#fireModelRevertedEvent} lets this tab's editor rebuild from it.
   */
  private void onRevertCompleted(@NonNull ProjectItem item, @NonNull GitChangedFile file) {
    if (file.status() == ChangeStatus.NEW) {
      StudioEventManager.getInstance().fireModelDeletedEvent(item);
    }
    else {
      item.reload();
      StudioEventManager.getInstance().fireModelRevertedEvent(item);
    }
    StudioEventManager.getInstance().fireGitStatusChangedEvent();
  }

  private Stage getStage() {
    return (Stage) commitButton.getScene().getWindow();
  }
}
